package wms.sandeliukas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wms.sandeliukas.model.Product;
import wms.sandeliukas.model.Purchase;
import wms.sandeliukas.model.ShoppingCartItem;
import wms.sandeliukas.model.User;
import wms.sandeliukas.repositories.ProductRepository;
import wms.sandeliukas.repositories.PurchaseRepository;
import wms.sandeliukas.repositories.ShoppingCartItemRepository;
import wms.sandeliukas.repositories.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartListService {

    private final ShoppingCartItemRepository shoppingCartItemRepository;
    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PaymentApiService paymentApiService;

    public CartListService(ShoppingCartItemRepository shoppingCartItemRepository,
                           PurchaseRepository purchaseRepository,
                           UserRepository userRepository,
                           ProductRepository productRepository,
                           PaymentApiService paymentApiService) {
        this.shoppingCartItemRepository = shoppingCartItemRepository;
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.paymentApiService = paymentApiService;
    }

    public List<ShoppingCartItem> selectCartItemsNotBought(String buyerEmail) {
        return shoppingCartItemRepository.findByBuyerEmailAndBoughtFalseAndPurchaseIsNull(buyerEmail);
    }

    @Transactional
    public void deleteSelectedItem(String buyerEmail, Integer cartItemId) {
        ShoppingCartItem item = shoppingCartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Krepšelio prekė nerasta"));

        if (!item.getBuyer().getEmail().equals(buyerEmail)) {
            throw new RuntimeException("Negalima trinti kito vartotojo krepšelio prekės");
        }

        shoppingCartItemRepository.delete(item);
    }

    @Transactional
    public void setNewAmountForItemInCart(String buyerEmail, Integer cartItemId, Integer newAmount) {
        ShoppingCartItem item = shoppingCartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Krepšelio prekė nerasta"));

        if (!item.getBuyer().getEmail().equals(buyerEmail)) {
            throw new RuntimeException("Negalima keisti kito vartotojo krepšelio prekės");
        }

        item.checkIfNewAmountIsValid(newAmount);

        item.setQuantity(newAmount);
        shoppingCartItemRepository.save(item);
    }

    public List<ShoppingCartItem> getCartItemsNotBought(String buyerEmail) {
        return shoppingCartItemRepository.findByBuyerEmailAndBoughtFalseAndPurchaseIsNull(buyerEmail);
    }

    public double calculateCartTotal(String buyerEmail) {
        List<ShoppingCartItem> items = selectCartItemsNotBought(buyerEmail);
        double total = 0;
        for (ShoppingCartItem item : items) {
            total += item.getQuantity() * item.getProduct().getSupplierPrice();
        }
        return total;
    }

    @Transactional
    public Purchase reserveNewPurchaseWithCartItemsAndDate(String buyerEmail) {
        User buyer = userRepository.findById(buyerEmail)
                .orElseThrow(() -> new RuntimeException("Pirkėjas nerastas"));

        List<ShoppingCartItem> cartItems = getCartItemsNotBought(buyerEmail);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Krepšelis tuščias");
        }

        for (ShoppingCartItem item : cartItems) {
            Product product = item.getProduct();
            if (product.getInitialStock() < item.getQuantity()) {
                throw new RuntimeException("Nepakanka likučio prekei: " + product.getName());
            }
        }

        Purchase savedPurchase = createReservedPurchase(buyer);
        assignCartItemsToReservedPurchase(cartItems, savedPurchase);

        return savedPurchase;
    }

    public List<Purchase> requestUsersPurchaseData(String buyerEmail) {
        return purchaseRepository.findByBuyerEmailAndPaymentStatus(buyerEmail, 1);
    }

    @Transactional
    public PaymentOutcome confirmPayment(String buyerEmail, Integer purchaseId) {
        userRepository.findById(buyerEmail)
                .orElseThrow(() -> new RuntimeException("Vartotojas nerastas"));

        Purchase purchase = purchaseRepository.findByIdAndBuyerEmail(purchaseId, buyerEmail)
                .orElseThrow(() -> new RuntimeException("Pirkimas nerastas"));

        if (isReservationExpired(purchase)) {
            requestUsersPurchaseDataDeletion(purchase);
            return reservationWindowViolationWarning();
        }

        double total = calculatePurchaseTotal(purchase);
        PaymentApiService.PaymentResult apiResult = requestPurchaseService(purchase, total);

        PaymentOutcome outcome = evaluatePurchaseSuccessData(apiResult);
        if (!outcome.isSuccess()) {
            requestUsersPurchaseDataDeletion(purchase);
            return outcome;
        }

        requestToSetItemsAsBought(purchase);
        requestToSetPurchaseAsDone(purchase);

        return outcome;
    }

    private Purchase createReservedPurchase(User buyer) {
        Integer maxId = purchaseRepository.findMaxId();
        Integer newPurchaseId = (maxId == null ? 0 : maxId) + 1;

        Purchase purchase = new Purchase();
        purchase.setId(newPurchaseId);
        purchase.setBuyer(buyer);
        purchase.setReservationDate(LocalDateTime.now());
        purchase.setPaymentDate(null);
        purchase.setPickupDate(null);
        purchase.setPaymentStatus(1);
        purchase.setStatus(1);

        return purchaseRepository.save(purchase);
    }

    private void assignCartItemsToReservedPurchase(List<ShoppingCartItem> cartItems, Purchase purchase) {
        for (ShoppingCartItem item : cartItems) {
            Product product = item.getProduct();
            product.setInitialStock(product.getInitialStock() - item.getQuantity());
            productRepository.save(product);

            item.setPurchase(purchase);
            item.setBought(true);
            shoppingCartItemRepository.save(item);
        }
    }

    private PaymentApiService.PaymentResult requestPurchaseService(Purchase purchase, double total) {
        return paymentApiService.requestServicePurchase(purchase.getId(), purchase.getBuyer().getEmail(), total);
    }

    private PaymentOutcome evaluatePurchaseSuccessData(PaymentApiService.PaymentResult result) {
        if (result.isSuccessful()) {
            return PaymentOutcome.success("Mokėjimas sėkmingas! " + result.getMessage());
        }
        return PaymentOutcome.failed("Mokėjimas nepavyko: " + result.getMessage());
    }

    private PaymentOutcome reservationWindowViolationWarning() {
        return PaymentOutcome.expired("Rezervavimo laikas pasibaigė (20 min.). Pirkimas atšauktas, prekės grąžintos į krepšelį.");
    }

    private boolean isReservationExpired(Purchase purchase) {
        if (purchase.getReservationDate() == null) return true;
        return LocalDateTime.now().isAfter(purchase.getReservationDate().plusMinutes(20));
    }

    private void requestUsersPurchaseDataDeletion(Purchase purchase) {
        List<ShoppingCartItem> items = shoppingCartItemRepository.findByPurchaseId(purchase.getId());

        for (ShoppingCartItem item : items) {
            Product product = item.getProduct();
            product.setInitialStock(product.getInitialStock() + item.getQuantity());
            productRepository.save(product);

            item.setBought(false);
            item.setPurchase(null);
            shoppingCartItemRepository.save(item);
        }

        purchaseRepository.delete(purchase);
    }

    private void requestToSetItemsAsBought(Purchase purchase) {
    }

    private void requestToSetPurchaseAsDone(Purchase purchase) {
        purchase.setPaymentStatus(2);
        purchase.setStatus(2);
        purchase.setPaymentDate(LocalDate.now());
        purchaseRepository.save(purchase);
    }

    private double calculatePurchaseTotal(Purchase purchase) {
        List<ShoppingCartItem> items = shoppingCartItemRepository.findByPurchaseId(purchase.getId());
        double total = 0;
        for (ShoppingCartItem item : items) {
            total += item.getQuantity() * item.getProduct().getSupplierPrice();
        }
        return total;
    }

    public static class PaymentOutcome {
        public enum Type { SUCCESS, FAILED, EXPIRED }

        private final Type type;
        private final String message;

        private PaymentOutcome(Type type, String message) {
            this.type = type;
            this.message = message;
        }

        public static PaymentOutcome success(String message) { return new PaymentOutcome(Type.SUCCESS, message); }
        public static PaymentOutcome failed(String message)  { return new PaymentOutcome(Type.FAILED,  message); }
        public static PaymentOutcome expired(String message) { return new PaymentOutcome(Type.EXPIRED, message); }

        public Type getType()      { return type; }
        public String getMessage() { return message; }
        public boolean isSuccess() { return type == Type.SUCCESS; }
    }
}

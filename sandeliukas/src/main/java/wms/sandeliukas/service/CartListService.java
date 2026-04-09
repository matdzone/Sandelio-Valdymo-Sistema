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
import java.util.List;

@Service
public class CartListService {

    private final ShoppingCartItemRepository shoppingCartItemRepository;
    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartListService(ShoppingCartItemRepository shoppingCartItemRepository,
                           PurchaseRepository purchaseRepository,
                           UserRepository userRepository,
                           ProductRepository productRepository) {
        this.shoppingCartItemRepository = shoppingCartItemRepository;
        this.purchaseRepository = purchaseRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    // 5. SelectCartItemsNotBought
    public List<ShoppingCartItem> selectCartItemsNotBought(String buyerEmail) {
        return shoppingCartItemRepository.findByBuyerEmailAndBoughtFalse(buyerEmail);
    }

    // 3. DeleteSelectedItem
    @Transactional
    public void deleteSelectedItem(String buyerEmail, Integer cartItemId) {
        ShoppingCartItem item = shoppingCartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Krepšelio prekė nerasta"));

        if (!item.getBuyer().getEmail().equals(buyerEmail)) {
            throw new RuntimeException("Negalima trinti kito vartotojo krepšelio prekės");
        }

        shoppingCartItemRepository.delete(item);
    }

    // 9. SetNewAmountForItemInCart
    @Transactional
    public void setNewAmountForItemInCart(String buyerEmail, Integer cartItemId, Integer newAmount) {
        ShoppingCartItem item = shoppingCartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Krepšelio prekė nerasta"));

        if (!item.getBuyer().getEmail().equals(buyerEmail)) {
            throw new RuntimeException("Negalima keisti kito vartotojo krepšelio prekės");
        }

        if (newAmount == null || newAmount < 1) {
            throw new RuntimeException("Naujas kiekis turi būti bent 1");
        }

        Product product = item.getProduct();
        if (product.getInitialStock() < newAmount) {
            throw new RuntimeException("Sandėlyje nepakanka prekės kiekio");
        }

        item.setQuantity(newAmount);
        shoppingCartItemRepository.save(item);
    }

    // 1. GetCartItemsNotBought
    public List<ShoppingCartItem> getCartItemsNotBought(String buyerEmail) {
        return shoppingCartItemRepository.findByBuyerEmailAndBoughtFalseAndPurchaseIsNull(buyerEmail);
    }
    @Transactional
    public double calculateCartTotal(String buyerEmail) {
        List<ShoppingCartItem> items = selectCartItemsNotBought(buyerEmail);

        double total = 0;

        for (ShoppingCartItem item : items) {
            total += item.getQuantity() * item.getProduct().getSupplierPrice();
        }

        return total;
    }
    // 3. ReserveNewPurchaseWithCartItemsAndDate
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

        Integer maxId = purchaseRepository.findMaxId();
        Integer newPurchaseId = maxId + 1;

        Purchase purchase = new Purchase();
        purchase.setId(newPurchaseId);
        purchase.setBuyer(buyer);

        // pagal tavo veiklos diagramą: įrašyti rezervavimo laiką
        purchase.setReservationDate(LocalDate.now());

        // mokėjimo dar nerealizuojam
        purchase.setPaymentDate(null);
        purchase.setPickupDate(null);

        // PaymentStatus: Reserved
        purchase.setPaymentStatus(1);

        // PurchaseStatus: Ordered
        purchase.setStatus(1);

        Purchase savedPurchase = purchaseRepository.save(purchase);

        for (ShoppingCartItem item : cartItems) {
            Product product = item.getProduct();

            product.setInitialStock(product.getInitialStock() - item.getQuantity());
            productRepository.save(product);

            item.setPurchase(savedPurchase);
            item.setBought(true);
            shoppingCartItemRepository.save(item);
        }

        return savedPurchase;
    }
}
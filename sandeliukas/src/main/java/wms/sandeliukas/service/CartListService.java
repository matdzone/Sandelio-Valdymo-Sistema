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

    // Sekų diagrama "Peržiūrėti prekių krepšelį", žinutė 5: selectCartItemsNotBought
    // Naudojama: CartListController.requestCartItemList()
    public List<ShoppingCartItem> selectCartItemsNotBought(String buyerEmail) {
        return shoppingCartItemRepository.findByBuyerEmailAndBoughtFalse(buyerEmail);
    }

    // Sekų diagrama "Valdyti krepšelyje esančias prekes", žinutė 3: deleteSelectedItem
    // Naudojama: CartListController.requestDeleteItem()
    @Transactional
    public void deleteSelectedItem(String buyerEmail, Integer cartItemId) {
        ShoppingCartItem item = shoppingCartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Krepšelio prekė nerasta"));

        if (!item.getBuyer().getEmail().equals(buyerEmail)) {
            throw new RuntimeException("Negalima trinti kito vartotojo krepšelio prekės");
        }

        shoppingCartItemRepository.delete(item);
    }

    // Sekų diagrama "Valdyti krepšelyje esančias prekes", žinutė 9: setNewAmountForItemInCart
    // Naudojama: CartListController.requestNewAmount()
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

    // Sekų diagrama "Rezervuoti pirkimą", žinutė 1: getCartItemsNotBought
    // Naudojama viduje reserveNewPurchaseWithCartItemsAndDate()
    // Skirtumas nuo selectCartItemsNotBought: grąžina tik tas prekes, kurios dar nepriskirtos jokiam pirkimui
    public List<ShoppingCartItem> getCartItemsNotBought(String buyerEmail) {
        return shoppingCartItemRepository.findByBuyerEmailAndBoughtFalseAndPurchaseIsNull(buyerEmail);
    }

    // Pagalbinis metodas krepšelio sumos skaičiavimui (naudojamas rodinyje)
    public double calculateCartTotal(String buyerEmail) {
        List<ShoppingCartItem> items = selectCartItemsNotBought(buyerEmail);
        double total = 0;
        for (ShoppingCartItem item : items) {
            total += item.getQuantity() * item.getProduct().getSupplierPrice();
        }
        return total;
    }

    // Sekų diagrama "Rezervuoti pirkimą", žinutė 3: reserveNewPurchaseWithCartItemsAndDate
    // Veiklos diagrama: sukurti pirkimo įrašą ir prie jo priskirti norimas prekes → įrašyti rezervavimo laiką
    // Naudojama: CartListController.requestPurchase()
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

    private Purchase createReservedPurchase(User buyer) {
        Integer maxId = purchaseRepository.findMaxId();
        Integer newPurchaseId = (maxId == null ? 0 : maxId) + 1;

        Purchase purchase = new Purchase();
        purchase.setId(newPurchaseId);
        purchase.setBuyer(buyer);
        purchase.setReservationDate(LocalDate.now());
        purchase.setPaymentDate(null);
        purchase.setPickupDate(null);

        // paymentStatus = 1 -> Rezervuotas
        purchase.setPaymentStatus(1);

        // status = 1 -> Užsakytas
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
}
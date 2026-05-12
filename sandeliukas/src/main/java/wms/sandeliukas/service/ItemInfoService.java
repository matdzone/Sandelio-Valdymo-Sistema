package wms.sandeliukas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wms.sandeliukas.model.Comment;
import wms.sandeliukas.model.Product;
import wms.sandeliukas.model.ShoppingCartItem;
import wms.sandeliukas.model.User;
import wms.sandeliukas.repositories.CommentRepository;
import wms.sandeliukas.repositories.ProductRepository;
import wms.sandeliukas.repositories.ShoppingCartItemRepository;
import wms.sandeliukas.repositories.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Service
public class ItemInfoService {

    private final ProductRepository productRepository;
    private final ShoppingCartItemRepository shoppingCartItemRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public ItemInfoService(ProductRepository productRepository,
                           ShoppingCartItemRepository shoppingCartItemRepository,
                           CommentRepository commentRepository,
                           UserRepository userRepository) {
        this.productRepository = productRepository;
        this.shoppingCartItemRepository = shoppingCartItemRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    public Product requestSelectedItemInformation(Integer productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Prekė nerasta"));
    }

    public boolean requestIsItemInCart(String buyerEmail, Integer productId) {
        return shoppingCartItemRepository.existsByBuyerEmailAndProductIdAndBoughtFalse(buyerEmail, productId);
    }

    public List<Comment> requestItemComments(Integer productId) {
        return commentRepository.findByProductId(productId);
    }

    @Transactional
    public void addItemToCartRequest(String buyerEmail, Integer productId, Integer quantity) {
        Product product = requestSelectedItemInformation(productId);

        if (quantity == null || quantity < 1) {
            throw new RuntimeException("Kiekis turi būti bent 1");
        }

        if (product.getInitialStock() < quantity) {
            throw new RuntimeException("Sandėlyje tėra " + product.getInitialStock() + " vnt.");
        }

        if (shoppingCartItemRepository.existsByBuyerEmailAndProductIdAndBoughtFalse(buyerEmail, productId)) {
            throw new RuntimeException("Ši prekė jau yra jūsų krepšelyje");
        }

        User buyer = userRepository.findById(buyerEmail)
                .orElseThrow(() -> new RuntimeException("Vartotojas nerastas"));

        Integer maxId = shoppingCartItemRepository.findMaxId();
        Integer newId = (maxId == null ? 0 : maxId) + 1;

        ShoppingCartItem item = new ShoppingCartItem();
        item.setId(newId);
        item.setBuyer(buyer);
        item.setProduct(product);
        item.setQuantity(quantity);
        item.setBought(false);
        item.setPurchase(null);

        shoppingCartItemRepository.save(item);
    }

    @Transactional
    public void recordNewComment(String buyerEmail, Integer productId, String text, Integer rating) {
        if (commentRepository.findByProductIdAndBuyerEmail(productId, buyerEmail).isPresent()) {
            throw new RuntimeException("Jūs jau esate palikęs komentarą šiai prekei");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Prekė nerasta"));

        User buyer = userRepository.findById(buyerEmail)
                .orElseThrow(() -> new RuntimeException("Vartotojas nerastas"));

        Integer maxId = commentRepository.findMaxId();
        Integer newId = (maxId == null ? 0 : maxId) + 1;

        Comment comment = new Comment();
        comment.setId(newId);
        comment.setText(text);
        comment.setRating(rating);
        comment.setCreationDate(LocalDate.now());
        comment.setProduct(product);
        comment.setBuyer(buyer);

        commentRepository.save(comment);
    }

    @Transactional
    public void editCommentInformation(String buyerEmail, Integer commentId, String newText, Integer newRating) {
        Comment comment = commentRepository.findByIdAndBuyerEmail(commentId, buyerEmail)
                .orElseThrow(() -> new RuntimeException("Komentaras nerastas arba neturite teisės jo redaguoti"));

        comment.setText(newText);
        comment.setRating(newRating);
        commentRepository.save(comment);
    }

    @Transactional
    public void deleteOldComment(String buyerEmail, Integer commentId) {
        Comment comment = commentRepository.findByIdAndBuyerEmail(commentId, buyerEmail)
                .orElseThrow(() -> new RuntimeException("Komentaras nerastas arba neturite teisės jo trinti"));

        commentRepository.delete(comment);
    }

}

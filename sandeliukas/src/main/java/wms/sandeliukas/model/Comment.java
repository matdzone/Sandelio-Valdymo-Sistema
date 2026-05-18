package wms.sandeliukas.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Comment")
public class Comment {

    @Id
    @Column(name = "id")
    private Integer id;

    @Column(name = "text")
    private String text;

    @Column(name = "creationDate")
    private LocalDate creationDate;

    @Column(name = "rating")
    private Integer rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_Product")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fk_Buyer")
    private User buyer;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public LocalDate getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }

    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }

    // ── Domain metodai (rich domain model) ───────────────────────────────────

    /**
     * Patikrina, ar komentaro duomenys teisingi.
     * Išmeta RuntimeException, jei tekstas tuščias, per ilgas arba įvertinimas neteisingas.
     */
    public static void validateData(String text, Integer rating) {
        if (text == null || text.isBlank()) {
            throw new RuntimeException("Komentaro tekstas negali būti tuščias");
        }
        if (text.length() > 2000) {
            throw new RuntimeException("Komentaro tekstas negali viršyti 2000 simbolių");
        }
        if (rating == null || rating < 1 || rating > 5) {
            throw new RuntimeException("Įvertinimas turi būti nuo 1 iki 5");
        }
    }
}

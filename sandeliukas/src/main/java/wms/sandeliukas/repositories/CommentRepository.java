package wms.sandeliukas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import wms.sandeliukas.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    @Query("select c from Comment c join fetch c.buyer where c.product.id = :productId")
    List<Comment> findByProductId(Integer productId);

    Optional<Comment> findByProductIdAndBuyerEmail(Integer productId, String buyerEmail);

    Optional<Comment> findByIdAndBuyerEmail(Integer id, String buyerEmail);

    @Query("select coalesce(max(c.id), 0) from Comment c")
    Integer findMaxId();
}

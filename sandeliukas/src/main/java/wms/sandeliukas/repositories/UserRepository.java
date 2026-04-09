package wms.sandeliukas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import wms.sandeliukas.model.User;

public interface UserRepository extends JpaRepository<User, String> {
}
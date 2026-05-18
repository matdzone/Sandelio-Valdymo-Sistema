package wms.sandeliukas.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wms.sandeliukas.model.User;
import wms.sandeliukas.repositories.UserRepository;

import java.time.LocalDate;

@Service
public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User register(String firstName, String lastName, String email, String password, String repeatedPassword) {
        checkFormRequest(firstName, lastName, email, password, repeatedPassword);

        String normalizedEmail = email.trim();
        if (userRepository.existsById(normalizedEmail)) {
            throw new RuntimeException("Vartotojas su tokiu el. paštu jau egzistuoja");
        }

        User user = new User();
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(password);
        user.setRegistrationDate(LocalDate.now());
        user.setShowSystemNotifications(true);
        user.setShowMessageNotifications(true);
        user.setResting(false);
        user.setRole(3);
        user.setStatus(1);

        return userRepository.save(user);
    }

    public User checkData(String email, String password) {
        if (isBlank(email) || isBlank(password)) {
            throw new RuntimeException("Įveskite el. paštą ir slaptažodį");
        }

        User user = userRepository.findById(email.trim())
                .orElseThrow(() -> new RuntimeException("Neteisingas el. paštas arba slaptažodis"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Neteisingas el. paštas arba slaptažodis");
        }

        return user;
    }

    private void checkFormRequest(String firstName, String lastName, String email, String password, String repeatedPassword) {
        if (isBlank(firstName) || isBlank(lastName) || isBlank(email) || isBlank(password) || isBlank(repeatedPassword)) {
            throw new RuntimeException("Užpildykite visus registracijos laukus");
        }

        if (!email.contains("@")) {
            throw new RuntimeException("Įveskite teisingą el. pašto adresą");
        }

        if (password.length() < 4) {
            throw new RuntimeException("Slaptažodis turi būti bent 4 simbolių");
        }

        if (!password.equals(repeatedPassword)) {
            throw new RuntimeException("Slaptažodžiai nesutampa");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

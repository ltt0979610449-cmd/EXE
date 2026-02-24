package swd.coiviet.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import swd.coiviet.enums.Gender;
import swd.coiviet.enums.Role;
import swd.coiviet.enums.Status;
import swd.coiviet.model.LearnCategory;
import swd.coiviet.model.User;
import swd.coiviet.repository.LearnCategoryRepository;
import swd.coiviet.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final LearnCategoryRepository learnCategoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final String initialAdminPassword;

    public DataInitializer(UserRepository userRepository,
                           LearnCategoryRepository learnCategoryRepository,
                           PasswordEncoder passwordEncoder,
                           @Value("${initial.admin.password}") String initialAdminPassword) {
        this.userRepository = userRepository;
        this.learnCategoryRepository = learnCategoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.initialAdminPassword = initialAdminPassword;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Không cần tạo entity Role, chỉ cần kiểm tra và tạo user admin với role là enum
        if (!userRepository.findByUsername("admin").isPresent()) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setEmail("admin@example.com");
            adminUser.setPasswordHash(passwordEncoder.encode(initialAdminPassword));
            adminUser.setFullName("Admin User");
            adminUser.setPhone("0123456789");
            adminUser.setAvatarUrl(null);
            adminUser.setDateOfBirth(LocalDate.of(1990, 1, 1));
            adminUser.setGender(Gender.MALE);
            adminUser.setRole(Role.ADMIN);
            adminUser.setStatus(Status.ACTIVE);
            userRepository.save(adminUser);
            System.out.println("Admin account created with username: admin and password: " + initialAdminPassword);
        } else {
            System.out.println("Admin account already exists.");
        }

        // Fix users có role null (tạo trước khi có fix) - gán CUSTOMER
        userRepository.findAll().stream()
                .filter(u -> u.getRole() == null)
                .forEach(u -> {
                    u.setRole(Role.CUSTOMER);
                    userRepository.save(u);
                    System.out.println("Fixed user " + u.getUsername() + " - set role to CUSTOMER");
                });

        if (learnCategoryRepository.count() == 0) {
            List<LearnCategory> categories = List.of(
                    LearnCategory.builder().name("Tất cả").slug("tat-ca").orderIndex(0).isActive(true).build(),
                    LearnCategory.builder().name("Cồng chiêng").slug("cong-chieng").orderIndex(1).isActive(true).build(),
                    LearnCategory.builder().name("Lễ hội").slug("le-hoi").orderIndex(2).isActive(true).build(),
                    LearnCategory.builder().name("Ẩm thực").slug("am-thuc").orderIndex(3).isActive(true).build(),
                    LearnCategory.builder().name("Trang phục").slug("trang-phuc").orderIndex(4).isActive(true).build(),
                    LearnCategory.builder().name("Truyền thuyết").slug("truyen-thuyet").orderIndex(5).isActive(true).build()
            );
            learnCategoryRepository.saveAll(categories);
            System.out.println("Learn categories initialized.");
        }
    }
}

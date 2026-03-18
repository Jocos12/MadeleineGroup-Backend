package rw.madeleinegroup.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rw.madeleinegroup.entity.Branch;
import rw.madeleinegroup.entity.Role;
import rw.madeleinegroup.entity.User;
import rw.madeleinegroup.repository.BranchRepository;
import rw.madeleinegroup.repository.UserRepository;

@Component
public class SeedData implements CommandLineRunner {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedData(UserRepository userRepository, BranchRepository branchRepository,
                   PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.branchRepository = branchRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (branchRepository.count() == 0) {
            branchRepository.save(Branch.builder().code("GARDEN").name("Madeleine Garden").description("Premium event venue").build());
            branchRepository.save(Branch.builder().code("DECOR").name("Madeleine Decor").description("Event decoration").build());
            branchRepository.save(Branch.builder().code("COLLECTION").name("Madeleine Collection").description("Fashion & styling").build());
            branchRepository.save(Branch.builder().code("STUDIO").name("Madeleine Studio").description("Photography & videography").build());
            branchRepository.save(Branch.builder().code("CATERING").name("Madeleine Catering").description("Culinary excellence").build());
            branchRepository.save(Branch.builder().code("PROTOCOL").name("Madeleine Protocol").description("Event coordination").build());
        }
        if (userRepository.count() == 0) {
            User ceo = new User();
            ceo.setEmail("ceo@madeleinegroup.rw");
            ceo.setPassword(passwordEncoder.encode("password123"));
            ceo.setFullName("CEO Madeleine");
            ceo.setRole(Role.CEO);
            ceo.setEnabled(true);
            userRepository.save(ceo);

            User admin = new User();
            admin.setEmail("admin@madeleinegroup.rw");
            admin.setPassword(passwordEncoder.encode("password123"));
            admin.setFullName("Admin User");
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);

            User client = new User();
            client.setEmail("client@test.com");
            client.setPassword(passwordEncoder.encode("password123"));
            client.setFullName("Test Client");
            client.setRole(Role.CLIENT);
            client.setEnabled(true);
            userRepository.save(client);
        }
        if (userRepository.findByEmail("asblkalemie@gmail.com").isEmpty()) {
            User ceoMain = new User();
            ceoMain.setEmail("asblkalemie@gmail.com");
            ceoMain.setPassword(passwordEncoder.encode("Madeleine2024"));
            ceoMain.setFullName("CEO Madeleine");
            ceoMain.setRole(Role.CEO);
            ceoMain.setEnabled(true);
            userRepository.save(ceoMain);
        }
    }
}

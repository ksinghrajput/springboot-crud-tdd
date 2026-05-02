package config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import model.Role;
import model.User;
import repository.UserRepository;

@Component
public class AdminSeeder implements CommandLineRunner {

	private static final String ADMIN_EMAIL = "admin@demo.com";
	private static final String ADMIN_PASSWORD = "admin123";

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		if (userRepository.findByEmail(ADMIN_EMAIL).isPresent()) {
			return;
		}
		User admin = new User();
		admin.setName("Admin");
		admin.setEmail(ADMIN_EMAIL);
		admin.setAge(30);
		admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
		admin.setRole(Role.ADMIN);
		userRepository.save(admin);
		System.out.println("Seeded admin user: " + ADMIN_EMAIL + " / " + ADMIN_PASSWORD);
	}
}

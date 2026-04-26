package service;

import java.time.Instant;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import dto.AuthResponse;
import dto.LoginRequest;
import dto.RegisterRequest;
import model.Role;
import model.User;
import repository.UserRepository;
import security.JwtService;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AuthService(UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			AuthenticationManager authenticationManager,
			JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
	}

	public User register(RegisterRequest req) {
		if (userRepository.findByEmail(req.getEmail()).isPresent()) {
			throw new DataIntegrityViolationException("Email already registered: " + req.getEmail());
		}
		User user = new User();
		user.setName(req.getName());
		user.setEmail(req.getEmail());
		user.setAge(req.getAge());
		user.setPassword(passwordEncoder.encode(req.getPassword()));
		user.setRole(Role.USER);
		return userRepository.save(user);
	}

	public AuthResponse login(LoginRequest req) {
		Authentication auth = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));

		User user = userRepository.findByEmail(auth.getName())
				.orElseThrow(() -> new IllegalStateException("Authenticated user not found in DB"));

		String token = jwtService.generateToken(user);
		Instant expiresAt = Instant.now().plusMillis(jwtService.getExpirationMs());
		return new AuthResponse(token, expiresAt, user.getEmail(), user.getRole().name());
	}
}

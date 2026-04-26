package controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dto.AuthResponse;
import dto.LoginRequest;
import dto.RegisterRequest;
import jakarta.validation.Valid;
import model.User;
import service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest req) {
		User saved = authService.register(req);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	@PostMapping("/login")
	public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
		return ResponseEntity.ok(authService.login(req));
	}
}

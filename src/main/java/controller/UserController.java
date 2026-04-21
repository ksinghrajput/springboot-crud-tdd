package controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import model.User;
import service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

	@Autowired
	private UserService userService;

	@GetMapping
	public List<User> getAllUsers() {
		return userService.getAllUsers();

	}

	@GetMapping("/{id}")
	public Optional<User> getUserById(@PathVariable long id) {
		return userService.getUserById(id);

	}

	@GetMapping("/email/{email}")
	public Optional<User> getUserByEmail(@PathVariable String email) {
		return userService.getUserByEmail(email);
	}

	@PostMapping("/")
	public ResponseEntity<User> createUser(@RequestBody User user) {
		User saved = userService.createUser(user);
		return ResponseEntity.status(HttpStatus.CREATED).body(saved);
	}

	@PutMapping("/{id}")
	public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User newUser) {
		User updated = userService.updateUser(id, newUser);
		return ResponseEntity.ok(updated);

	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteUser(@PathVariable long id) {
		userService.deleteUser(id);
		return ResponseEntity.noContent().build();
	}

}
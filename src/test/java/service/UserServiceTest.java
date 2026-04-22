package service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import exception.ResourceNotFoundException;
import model.User;
import repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@InjectMocks
	private UserService userService;

	private User sampleUser;

	@BeforeEach
	void setUp() {
		sampleUser = new User(1L, "Kishan", "kishan@example.com", 25);
	}

	@Test
	@DisplayName("getAllUsers returns every user from the repository")
	void getAllUsers_returnsList() {
		User second = new User(2L, "Asha", "asha@example.com", 30);
		when(userRepository.findAll()).thenReturn(Arrays.asList(sampleUser, second));

		List<User> result = userService.getAllUsers();

		assertEquals(2, result.size());
		assertEquals("Kishan", result.get(0).getName());
		verify(userRepository).findAll();
	}

	@Test
	@DisplayName("getUserById returns the user when it exists")
	void getUserById_whenFound_returnsUser() {
		when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

		Optional<User> result = userService.getUserById(1L);

		assertTrue(result.isPresent());
		assertAll(
				() -> assertEquals(1L, result.get().getId()),
				() -> assertEquals("Kishan", result.get().getName()),
				() -> assertEquals("kishan@example.com", result.get().getEmail()),
				() -> assertEquals(25, result.get().getAge()));
		verify(userRepository).findById(1L);
	}

	@Test
	@DisplayName("getUserById returns empty Optional when user is missing")
	void getUserById_whenMissing_returnsEmpty() {
		when(userRepository.findById(99L)).thenReturn(Optional.empty());

		Optional<User> result = userService.getUserById(99L);

		assertFalse(result.isPresent());
	}

	@Test
	@DisplayName("getUserByEmail returns the user when email matches")
	void getUserByEmail_whenFound_returnsUser() {
		when(userRepository.findByEmail("kishan@example.com")).thenReturn(Optional.of(sampleUser));

		Optional<User> result = userService.getUserByEmail("kishan@example.com");

		assertTrue(result.isPresent());
		assertEquals("Kishan", result.get().getName());
	}

	@Test
	@DisplayName("createUser saves and returns the persisted user")
	void createUser_savesAndReturnsUser() {
		User toSave = new User(null, "New", "new@example.com", 20);
		User saved = new User(5L, "New", "new@example.com", 20);
		when(userRepository.save(toSave)).thenReturn(saved);

		User result = userService.createUser(toSave);

		assertEquals(5L, result.getId());
		verify(userRepository).save(toSave);
	}

	@Test
	@DisplayName("updateUser overwrites fields of an existing user and saves it")
	void updateUser_whenFound_updatesFields() {
		User updates = new User(null, "Kishan Updated", "kishan.new@example.com", 26);
		when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
		when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

		User result = userService.updateUser(1L, updates);

		assertAll(
				() -> assertEquals("Kishan Updated", result.getName()),
				() -> assertEquals("kishan.new@example.com", result.getEmail()),
				() -> assertEquals(26, result.getAge()));
		verify(userRepository).findById(1L);
		verify(userRepository).save(sampleUser);
	}

	@Test
	@DisplayName("updateUser throws ResourceNotFoundException when id is unknown")
	void updateUser_whenMissing_throws() {
		User updates = new User(null, "X", "x@example.com", 1);
		when(userRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(99L, updates));

		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	@DisplayName("deleteUser deletes when the id exists")
	void deleteUser_whenExists_deletes() {
		when(userRepository.existsById(1L)).thenReturn(true);

		userService.deleteUser(1L);

		verify(userRepository, times(1)).deleteById(1L);
	}

	@Test
	@DisplayName("deleteUser throws ResourceNotFoundException when id does not exist")
	void deleteUser_whenMissing_throws() {
		when(userRepository.existsById(99L)).thenReturn(false);

		assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99L));

		verify(userRepository, never()).deleteById(99L);
	}
}

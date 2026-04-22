package repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;

import model.User;

@DataJpaTest
@ContextConfiguration(classes = UserRepositoryTest.TestConfig.class)
class UserRepositoryTest {

	@SpringBootConfiguration
	@EntityScan("model")
	@EnableJpaRepositories("repository")
	static class TestConfig {
	}

	@Autowired
	private TestEntityManager em;

	@Autowired
	private UserRepository userRepository;

	@Test
	@DisplayName("save persists the user and generates an id")
	void save_assignsId() {
		User user = new User(null, "Kishan", "kishan@example.com", 25);

		User saved = userRepository.save(user);

		assertNotNull(saved.getId(), "id should be auto-generated");
		assertEquals("Kishan", saved.getName());
	}

	@Test
	@DisplayName("findById returns the user previously persisted via TestEntityManager")
	void findById_returnsPersistedUser() {
		User persisted = em.persistAndFlush(new User(null, "Asha", "asha@example.com", 30));

		Optional<User> found = userRepository.findById(persisted.getId());

		assertTrue(found.isPresent());
		assertEquals("Asha", found.get().getName());
	}

	@Test
	@DisplayName("findByEmail returns the matching user")
	void findByEmail_whenPresent_returnsUser() {
		em.persistAndFlush(new User(null, "Ram", "ram@example.com", 40));

		Optional<User> found = userRepository.findByEmail("ram@example.com");

		assertTrue(found.isPresent());
		assertEquals("Ram", found.get().getName());
	}

	@Test
	@DisplayName("findByEmail returns empty when no match")
	void findByEmail_whenMissing_returnsEmpty() {
		Optional<User> found = userRepository.findByEmail("nobody@example.com");

		assertFalse(found.isPresent());
	}

	@Test
	@DisplayName("findByNameContaining returns all users whose name contains the keyword")
	void findByNameContaining_returnsMatches() {
		em.persistAndFlush(new User(null, "Kishan Singh", "k1@example.com", 25));
		em.persistAndFlush(new User(null, "Kishore",      "k2@example.com", 28));
		em.persistAndFlush(new User(null, "Asha",         "a@example.com",  30));

		List<User> matches = userRepository.findByNameContaining("Kish");

		assertEquals(2, matches.size());
	}

	@Test
	@DisplayName("deleteById removes the user from the database")
	void deleteById_removesUser() {
		User persisted = em.persistAndFlush(new User(null, "ToDelete", "del@example.com", 22));
		Long id = persisted.getId();

		userRepository.deleteById(id);
		em.flush();

		assertFalse(userRepository.findById(id).isPresent());
	}

	@Test
	@DisplayName("saving two users with the same email violates the unique constraint")
	void uniqueEmail_violation_throws() {
		em.persistAndFlush(new User(null, "A", "dup@example.com", 20));

		User duplicate = new User(null, "B", "dup@example.com", 21);

		assertThrows(DataIntegrityViolationException.class,
				() -> userRepository.saveAndFlush(duplicate));
	}
}

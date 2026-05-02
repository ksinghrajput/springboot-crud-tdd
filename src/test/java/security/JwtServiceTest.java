package security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import model.Role;
import model.User;

class JwtServiceTest {

	private static final String SECRET = "this-is-a-very-long-test-secret-key-for-hs256-32b";
	private JwtService service;
	private User user;

	@BeforeEach
	void setUp() {
		service = new JwtService(SECRET, 60_000L);
		user = new User(7L, "Kishan", "kishan@example.com", 25, "hashedpw", Role.USER);
	}

	@Test
	@DisplayName("generated token round-trips and carries expected claims")
	void roundTrip() {
		String token = service.generateToken(user);
		Claims claims = service.parse(token);

		assertEquals("kishan@example.com", claims.getSubject());
		assertEquals(7, ((Number) claims.get("userId")).longValue());
		assertEquals(List.of("ROLE_USER"), claims.get("roles"));
		assertTrue(claims.getExpiration().getTime() > System.currentTimeMillis());
	}

	@Test
	@DisplayName("tampered signature is rejected")
	void tamperedTokenRejected() {
		String token = service.generateToken(user);
		String tampered = token.substring(0, token.length() - 2) + "xx";

		assertThrows(JwtException.class, () -> service.parse(tampered));
	}

	@Test
	@DisplayName("expired token is rejected")
	void expiredTokenRejected() {
		JwtService shortLived = new JwtService(SECRET, 1L);
		String token = shortLived.generateToken(user);
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

		assertThrows(JwtException.class, () -> shortLived.parse(token));
	}
}

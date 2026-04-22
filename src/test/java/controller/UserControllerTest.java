package controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import exception.GlobalExceptionHandler;
import exception.ResourceNotFoundException;
import model.User;
import service.UserService;

@WebMvcTest(controllers = UserController.class)
@Import({ UserController.class, GlobalExceptionHandler.class })
@ContextConfiguration(classes = UserControllerTest.TestConfig.class)
class UserControllerTest {

	@SpringBootConfiguration
	static class TestConfig {
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private UserService userService;

	private User sampleUser;

	@BeforeEach
	void setUp() {
		sampleUser = new User(1L, "Kishan", "kishan@example.com", 25);
	}

	@Test
	@DisplayName("GET /api/users returns 200 and a JSON array")
	void getAllUsers_returns200AndList() throws Exception {
		when(userService.getAllUsers()).thenReturn(List.of(sampleUser));

		mockMvc.perform(get("/api/users"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].name").value("Kishan"))
				.andExpect(jsonPath("$[0].email").value("kishan@example.com"));
	}

	@Test
	@DisplayName("GET /api/users/{id} returns 200 with the user")
	void getUserById_returnsUser() throws Exception {
		when(userService.getUserById(1L)).thenReturn(Optional.of(sampleUser));

		mockMvc.perform(get("/api/users/{id}", 1L))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(1))
				.andExpect(jsonPath("$.name").value("Kishan"))
				.andExpect(jsonPath("$.age").value(25));
	}

	@Test
	@DisplayName("POST /api/users returns 201 when payload is valid")
	void createUser_returns201() throws Exception {
		User incoming = new User(null, "Asha", "asha@example.com", 30);
		User saved = new User(2L, "Asha", "asha@example.com", 30);
		when(userService.createUser(any(User.class))).thenReturn(saved);

		mockMvc.perform(post("/api/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(incoming)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(2))
				.andExpect(jsonPath("$.name").value("Asha"));
	}

	@Test
	@DisplayName("PUT /api/users/{id} returns 200 with the updated user")
	void updateUser_returnsUpdated() throws Exception {
		User updates = new User(null, "Kishan Updated", "k.new@example.com", 26);
		User saved = new User(1L, "Kishan Updated", "k.new@example.com", 26);
		when(userService.updateUser(eq(1L), any(User.class))).thenReturn(saved);

		mockMvc.perform(put("/api/users/{id}", 1L)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updates)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Kishan Updated"))
				.andExpect(jsonPath("$.email").value("k.new@example.com"));
	}

	@Test
	@DisplayName("PUT /api/users/{id} returns 404 when the id does not exist")
	void updateUser_whenMissing_returns404() throws Exception {
		User updates = new User(null, "X", "x@example.com", 20);
		when(userService.updateUser(eq(99L), any(User.class)))
				.thenThrow(new ResourceNotFoundException("User 99"));

		mockMvc.perform(put("/api/users/{id}", 99L)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(updates)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("User 99"));
	}

	@Test
	@DisplayName("DELETE /api/users/{id} returns 204 No Content")
	void deleteUser_returns204() throws Exception {
		doNothing().when(userService).deleteUser(1L);

		mockMvc.perform(delete("/api/users/{id}", 1L))
				.andExpect(status().isNoContent());

		verify(userService).deleteUser(1L);
	}

	@Test
	@DisplayName("DELETE /api/users/{id} returns 404 when id missing")
	void deleteUser_whenMissing_returns404() throws Exception {
		doThrow(new ResourceNotFoundException("User 99"))
				.when(userService).deleteUser(99L);

		mockMvc.perform(delete("/api/users/{id}", 99L))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.message").value("User 99"));
	}
}

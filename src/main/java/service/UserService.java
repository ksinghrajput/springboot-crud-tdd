package service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import exception.ResourceNotFoundException;
import model.User;
import repository.UserRepository;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	public Optional<User> getUserById(long id) {
		return userRepository.findById(id);
	}

	public Optional<User> getUserByEmail(String email) {
		return userRepository.findByEmail(email);

	}

	public User createUser(User user) {
		return userRepository.save(user);

	}

	public User updateUser(Long id, User updatedUser) {
		User existing = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User" + id));
		existing.setName(updatedUser.getName());
		existing.setEmail(updatedUser.getEmail());
		existing.setAge(updatedUser.getAge());
		return userRepository.save(existing);
	}

	public void deleteUser(long id) {
		if (!userRepository.existsById(id)) {
			throw new ResourceNotFoundException("User" + id);
		}
		userRepository.deleteById(id);
	}

}

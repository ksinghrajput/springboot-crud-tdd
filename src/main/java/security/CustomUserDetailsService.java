package security;

import java.util.List;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		model.User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

		return User.withUsername(user.getEmail())
				.password(user.getPassword())
				.authorities(List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
						"ROLE_" + user.getRole().name())))
				.build();
	}
}

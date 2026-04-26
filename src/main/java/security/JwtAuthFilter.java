package security;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

	private static final String HEADER = "Authorization";
	private static final String PREFIX = "Bearer ";

	private final JwtService jwtService;

	public JwtAuthFilter(JwtService jwtService) {
		this.jwtService = jwtService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String header = request.getHeader(HEADER);
		if (header == null || !header.startsWith(PREFIX)) {
			chain.doFilter(request, response);
			return;
		}

		String token = header.substring(PREFIX.length());
		try {
			Claims claims = jwtService.parse(token);
			String email = claims.getSubject();

			@SuppressWarnings("unchecked")
			List<String> roles = claims.get("roles", List.class);
			Collection<GrantedAuthority> authorities = roles == null
					? List.of()
					: roles.stream().map(SimpleGrantedAuthority::new).map(a -> (GrantedAuthority) a).toList();

			UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(email, null,
					authorities);
			auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(auth);
		} catch (JwtException ex) {
			SecurityContextHolder.clearContext();
		}

		chain.doFilter(request, response);
	}
}

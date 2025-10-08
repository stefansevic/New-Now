package com.example.demo.security;

import com.example.demo.model.Administrator;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import java.util.Collections;
import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	public CustomUserDetailsService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findById(username)
				.orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
		
		List<SimpleGrantedAuthority> authorities;
		if ("admin@system.local".equals(user.getEmail())) {
			authorities = Collections.singletonList(new SimpleGrantedAuthority("ADMIN"));
		} else {
			authorities = Collections.singletonList(new SimpleGrantedAuthority("USER"));
		}

		return new org.springframework.security.core.userdetails.User(
				user.getEmail(),
				user.getPassword(),
				authorities);
	}
}



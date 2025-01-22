package ch.martinelli.demo.vaadin.security;

import ch.martinelli.demo.vaadin.domain.User;
import ch.martinelli.demo.vaadin.domain.UserRepository;
import com.vaadin.flow.spring.security.AuthenticationContext;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserContext {

    private final UserRepository userRepository;
    private final AuthenticationContext authenticationContext;

    public UserContext(AuthenticationContext authenticationContext, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.authenticationContext = authenticationContext;
    }

    @Transactional(readOnly = true)
    public Optional<User> get() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class)
                .map(userDetails -> userRepository.findByUsername(userDetails.getUsername()));
    }

    public void logout() {
        authenticationContext.logout();
    }

}

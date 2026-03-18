package rw.madeleinegroup.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import rw.madeleinegroup.entity.User;
import rw.madeleinegroup.repository.UserRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        if (!user.getEnabled()) {
            throw new UsernameNotFoundException("User account is disabled");
        }
        return new UserPrincipal(user);
    }

    public static class UserPrincipal implements UserDetails {
        private final User user;

        public UserPrincipal(User user) {
            this.user = user;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
        }

        @Override
        public String getPassword() {
            return user.getPassword();
        }

        @Override
        public String getUsername() {
            return user.getEmail();
        }

        @Override
        public boolean isAccountNonExpired() { return true; }

        @Override
        public boolean isAccountNonLocked() { return true; }

        @Override
        public boolean isCredentialsNonExpired() { return true; }

        @Override
        public boolean isEnabled() { return user.getEnabled(); }

        public Long getId() { return user.getId(); }
        public String getEmail() { return user.getEmail(); }
    }
}

package rw.madeleinegroup.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import rw.madeleinegroup.entity.Role;
import rw.madeleinegroup.entity.User;
import rw.madeleinegroup.repository.UserRepository;

import java.util.Collection;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (!u.isEnabled()) {
            throw new UsernameNotFoundException("User disabled");
        }
        return new UserPrincipal(u.getId(), u.getEmail(), u.getPassword(), u.getRole());
    }

    public static final class UserPrincipal implements UserDetails {

        private final Long id;
        private final String email;
        private final String password;
        private final List<GrantedAuthority> authorities;

        public UserPrincipal(Long id, String email, String password, Role role) {
            this.id = id;
            this.email = email;
            this.password = password != null ? password : "";
            this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
        }

        public Long getId() {
            return id;
        }

        public String getEmail() {
            return email;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getUsername() {
            return email;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}

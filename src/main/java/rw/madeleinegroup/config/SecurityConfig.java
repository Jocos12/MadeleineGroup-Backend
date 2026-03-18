package rw.madeleinegroup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import rw.madeleinegroup.security.JwtAuthFilter;
import rw.madeleinegroup.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, CustomUserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    private static final String[] PUBLIC_URLS = {
            "/api/auth/**",
            "/api/contact",
            "/api/client-experiences/public/**",
            "/api/experiences/public/**",
            "/api/packages/public/**",
            "/api/gallery/public/**",
            "/api/branches/public/**",
            "/api/departments/**",
            "/api/bookings/available-dates",
            "/api/files/**",
            "/ws/**",
            "/actuator/health"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_URLS).permitAll()
                        .requestMatchers("/madeleineGroupLogo.jpeg").permitAll()
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "CEO")
                        .requestMatchers(HttpMethod.GET, "/api/blocked-dates").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/blocked-dates").hasAnyRole("ADMIN", "CEO", "MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/blocked-dates/*").hasAnyRole("ADMIN", "CEO", "MANAGER")
                        .requestMatchers("/api/bookings/*/status").hasAnyRole("ADMIN", "CEO", "MANAGER")
                        .requestMatchers("/api/ceo/bookings/**").hasAnyRole("CEO", "ADMIN", "MANAGER")
                        .requestMatchers("/api/ceo/**").hasRole("CEO")
                        .requestMatchers("/api/manager/**").hasAnyRole("MANAGER", "ADMIN", "CEO")
                        .requestMatchers("/api/finance/**").hasAnyRole("ADMIN", "CEO", "MANAGER")
                        .requestMatchers("/api/reminders/**").hasAnyRole("ADMIN", "CEO", "MANAGER")
                        .requestMatchers("/api/ai/usage-stats").hasRole("CEO")
                        .requestMatchers("/api/ai/**").hasAnyRole("ADMIN", "CEO", "MANAGER")
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}

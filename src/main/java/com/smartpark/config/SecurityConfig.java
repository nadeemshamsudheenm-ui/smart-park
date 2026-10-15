package com.smartpark.config;

import com.smartpark.security.CustomAuthenticationSuccessHandler;
import com.smartpark.security.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final CustomAuthenticationSuccessHandler successHandler;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          CustomAuthenticationSuccessHandler successHandler) {
        this.userDetailsService = userDetailsService;
        this.successHandler = successHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
            .authorizeHttpRequests(auth -> auth
                // Static resources & public pages
                .requestMatchers("/", "/login", "/register", "/css/**", "/js/**", "/images/**", "/error/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                // Read-only public parking areas & slots for browsing
                .requestMatchers(HttpMethod.GET, "/api/parking-areas/**", "/api/parking-slots/**").permitAll()
                // Customer pages & endpoints
                .requestMatchers("/customer/**").hasRole("CUSTOMER")
                .requestMatchers("/api/reservations/my", "/api/reservations").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("/api/reservations/*/cancel").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("/api/vehicles/**").hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers("/api/payments/my", "/api/payments/demo").hasAnyRole("CUSTOMER", "ADMIN")
                // Staff pages & endpoints
                .requestMatchers("/staff/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers("/api/staff/**").hasAnyRole("STAFF", "ADMIN")
                // Admin pages & endpoints
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/parking-areas/**", "/api/parking-slots/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/parking-areas/**", "/api/parking-slots/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/parking-areas/**", "/api/parking-slots/**").hasRole("ADMIN")
                // Any other request
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .successHandler(successHandler)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/error/403")
            )
            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}

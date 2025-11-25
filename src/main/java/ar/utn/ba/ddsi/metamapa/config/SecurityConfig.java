package ar.utn.ba.ddsi.metamapa.config;

import ar.utn.ba.ddsi.metamapa.providers.CustomAuthProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Importar
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  private final CustomAuthProvider customAuthProvider;

  public SecurityConfig(CustomAuthProvider customAuthProvider) {
    this.customAuthProvider = customAuthProvider;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        // 👇 1. DESACTIVAMOS CSRF para evitar bloqueos en el formulario de login
        .csrf(AbstractHttpConfigurer::disable)

        .authenticationProvider(customAuthProvider)

        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/",
                "/landing.html",
                "/css/**", "/js/**", "/img/**",
                "/login/**",
                "/registro",
                "/api-proxy/**",
                "/legal/**",
                "/debug/**"
            ).permitAll()
            .anyRequest().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login")
            .loginProcessingUrl("/login")
            .defaultSuccessUrl("/admin/inicio", true)
            .failureUrl("/login?error")
            .permitAll()
        )
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessUrl("/login?logout")
            .permitAll()
        );

    return http.build();
  }
}
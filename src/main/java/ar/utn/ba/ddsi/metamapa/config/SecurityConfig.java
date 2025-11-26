package ar.utn.ba.ddsi.metamapa.config;

import ar.utn.ba.ddsi.metamapa.handlers.CustomSuccessHandler;
import ar.utn.ba.ddsi.metamapa.providers.CustomAuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomSuccessHandler successHandler;

    private final CustomAuthProvider customAuthProvider;

    public SecurityConfig(CustomAuthProvider customAuthProvider) {
        this.customAuthProvider = customAuthProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Evitar problemas con el login
                .csrf(AbstractHttpConfigurer::disable)

                .authenticationProvider(customAuthProvider)

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/landing.html",
                                "/css/**", "/js/**", "/img/**",
                                "/login/**",
                                "/registro",
                                "/api-proxy/**",
                                "/legal/**",
                                "/debug/**",
                                "/colecciones",
                                "/colecciones/", "/colecciones/{id}", "/colecciones/ultimas"
                        ).permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/colecciones/nueva").hasRole("ADMIN")
                        .requestMatchers("/colecciones/crear").hasRole("ADMIN")
                        .requestMatchers("/colecciones/editar/**").hasRole("ADMIN")
                        .requestMatchers("/colecciones/eliminar/**").hasRole("ADMIN")

                        .requestMatchers("/contribuyente/**").hasRole("CONTRIBUYENTE")

                        .anyRequest().authenticated()
                )

                // LOGIN con éxito manejado por tu handler personalizado
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler)   // ESTE MANDA SEGÚN EL ROL
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
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
                .csrf(AbstractHttpConfigurer::disable)

                .authenticationProvider(customAuthProvider)

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) ->
                                res.sendRedirect("/error?code=401"))
                        .accessDeniedHandler((req, res, e) ->
                                res.sendRedirect("/error?code=403"))
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/hechos/ui/eliminar/**").hasRole("ADMIN")
                        .requestMatchers(
                                "/", "/landing", "/landing.html",
                                "/hechos", "/hechos/**",
                                "/objetivos",
                                "/accesos",
                                "/colecciones", "/colecciones/**",
                                "/legal/**",
                                "/css/**", "/js/**", "/img/**",
                                "/api-proxy/**",
                                "/login/**",
                                "/registro",
                                "/debug/**",
                                "/error/**"
                        ).permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/colecciones/nueva").hasRole("ADMIN")
                        .requestMatchers("/colecciones/crear").hasRole("ADMIN")
                        .requestMatchers("/colecciones/editar/**").hasRole("ADMIN")
                        .requestMatchers("/colecciones/eliminar/**").hasRole("ADMIN")

                        .requestMatchers("/contribuyente/**").hasRole("CONTRIBUYENTE")

                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler)
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
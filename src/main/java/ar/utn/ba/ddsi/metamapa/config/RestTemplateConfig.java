package ar.utn.ba.ddsi.metamapa.config;

import ar.utn.ba.ddsi.metamapa.API.CookieForwarder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    @Primary
    public RestTemplate restTemplate(CookieForwarder cookieForwarder) {

        RestTemplate restTemplate = new RestTemplate();

        // Interceptor para agregar JWT en cada request al backend API
        restTemplate.getInterceptors().add((request, body, execution) -> {

            String jwt = cookieForwarder.getTokenFromCurrentRequest();

            if (jwt != null && !jwt.isBlank()) {
                request.getHeaders().add("Authorization", "Bearer " + jwt);
            }

            return execution.execute(request, body);
        });

        return restTemplate;
    }
}
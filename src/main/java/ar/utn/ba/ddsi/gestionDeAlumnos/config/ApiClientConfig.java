package ar.utn.ba.ddsi.gestionDeAlumnos.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import jakarta.servlet.http.HttpServletRequest;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class ApiClientConfig {

    @Bean
    public WebClient apiWebClient(@Value("${app.api-base}") String apiBase) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(20));

        return WebClient.builder()
                .baseUrl(apiBase)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                // Filtro que copia el header Cookie (JSESSIONID u otros) del request entrante al saliente
                .filter(propagateCookiesFilter())
                .build();
    }

    private ExchangeFilterFunction propagateCookiesFilter() {
        return (request, next) -> {
            // Intentamos leer la Cookie del request HTTP actual (lado UI)
            RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                Object reqObj = attrs.resolveReference(RequestAttributes.REFERENCE_REQUEST);
                if (reqObj instanceof HttpServletRequest servletRequest) {
                    String cookieHeader = servletRequest.getHeader("Cookie");
                    if (cookieHeader != null && !cookieHeader.isBlank()) {
                        // Adjuntamos las mismas cookies al request que irá hacia el API
                        var mutated = WebClient
                                .RequestHeadersSpec.class
                                .cast(request)
                                .headers(h -> h.add("Cookie", cookieHeader));
                        // NOTA: como RequestHeadersSpec no es público acá, re-creamos la request con headers
                        return next.exchange(
                                request.mutate()
                                        .headers(h -> h.add("Cookie", cookieHeader))
                                        .build()
                        );
                    }
                }
            }
            return next.exchange(request);
        };
    }
}
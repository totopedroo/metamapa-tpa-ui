package ar.utn.ba.ddsi.metamapa.API;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class CookieForwarder {
    public Consumer<HttpHeaders> from(HttpServletRequest req) {
        return h -> { String c = req.getHeader("Cookie"); if (c != null) h.add("Cookie", c); };
    }
}
package ar.utn.ba.ddsi.metamapa.API;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.function.Consumer;

@Component
public class CookieForwarder {
    public Consumer<HttpHeaders> from(HttpServletRequest req) {
        return h -> { String c = req.getHeader("Cookie"); if (c != null) h.add("Cookie", c); };
    }

    public String getTokenFromCurrentRequest() {
        HttpServletRequest req =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                        .getRequest();
        if (req == null || req.getCookies() == null) return null;

        for (Cookie c : req.getCookies()) {
            if ("Authorization".equals(c.getName())) {
                return c.getValue().replace("Bearer ", ""); // por si lo guardaste así
            }
        }
        return null;
    }
}
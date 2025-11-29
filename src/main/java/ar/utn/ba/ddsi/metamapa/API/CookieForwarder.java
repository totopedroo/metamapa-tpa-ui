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
        var req = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes())
                .getRequest();

        var session = req.getSession(false);
        if (session == null) return null;

        return (String) session.getAttribute("jwt");
    }

    public String getCookieHeaderFromCurrentRequest() {
        var req = ((ServletRequestAttributes)
                RequestContextHolder.currentRequestAttributes())
                .getRequest();

        String cookie = req.getHeader("Cookie");
        return cookie != null ? cookie : "";
    }

}
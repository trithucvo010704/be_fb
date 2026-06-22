package vn.ezisolutions.cloud.facebook_service.core.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.ezisolutions.cloud.facebook_service.core.AuthorizedUser;
import vn.ezisolutions.cloud.facebook_service.services.cache.AuthCacheService;

import java.io.IOException;

@Component
public class ApiTokenFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(ApiTokenFilter.class);

    @Autowired
    private AuthCacheService cacheService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getServletPath();
        return path.startsWith("/base/google-ads/oauth/callback")
                || path.startsWith("/base/facebook/webhooks")
                || path.startsWith("/base/facebook/login")
                || path.startsWith("/_private/")
                || path.equals("/api/auth/demo-login")
                || path.equals("/api/facebook/oauth/url")
                || path.startsWith("/api/facebook/oauth/callback")
                || path.startsWith("/api/facebook/webhook")
                || path.equals("/api/data-deletion")
                || path.startsWith("/api/public/")
                || path.equals("/privacy-policy")
                || path.equals("/data-deletion");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = request.getHeader("Authorization");
        if (jwt == null || !isValidToken(jwt)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthenticated");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isValidToken(String token) {
        if (!token.startsWith("Bearer ")) {
            return false;
        }
        token = token.replaceFirst("Bearer ", "");
        try {
            AuthorizedUser user = cacheService.getUserByToken(token);
            if (user == null) {
                throw new Exception(String.format("Token %s not found", token));
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }
}

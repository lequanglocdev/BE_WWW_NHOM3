package fit.iuh.se.security;

import fit.iuh.se.entities.UserAccount;
import fit.iuh.se.repositories.UserAccountRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserAccountRepository userRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        System.out.println(">>> JWT FILTER <<<");
        String authHeader = request.getHeader("Authorization");
        System.out.println("Header: " + authHeader);
        if (authHeader != null
                && authHeader.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String token = authHeader.substring(7);
            System.out.println("Token: " + token);
            try {
                if (jwtUtils.validateToken(token)) {
                    String email = jwtUtils.extractEmail(token);
                    System.out.println("Email từ token: " + email);
                    UserAccount user = userRepo.findByEmail(email).orElse(null);
                    System.out.println("User từ DB: " + user);
                    if (user != null && user.getIsActive()) {
                        System.out.println("ROLE: " + (user.getIsAdmin() ? "ADMIN" : "USER"));
                        String role = user.getIsAdmin() ? "ROLE_ADMIN" : "ROLE_USER";

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(
                                        user.getEmail(),
                                        null,
                                        List.of(new SimpleGrantedAuthority(role))
                                );
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // 🔥 QUAN TRỌNG
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            } catch (Exception e) {
                System.out.println("JWT ERROR: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }

}

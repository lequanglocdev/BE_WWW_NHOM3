    package fit.iuh.se.security;

    import io.jsonwebtoken.Claims;
    import io.jsonwebtoken.Jwts;
    import org.springframework.stereotype.Component;
    import io.jsonwebtoken.security.Keys;

    import java.security.Key;
    import java.util.Date;

    @Component
    public class JwtUtils {

        private final String SECRET = "your_secret_key_12345678901234567890";

        private Key getKey() {
            return Keys.hmacShaKeyFor(SECRET.getBytes());
        }

        public String generateToken(String email, boolean isAdmin) {
            return Jwts.builder()
                    .setSubject(email)
                    .claim("role", isAdmin ? "ROLE_ADMIN" : "ROLE_USER")
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                    .signWith(getKey())
                    .compact();
        }

        public String extractEmail(String token) {
            return getClaims(token).getSubject();
        }

        public boolean validateToken(String token) {
            try {
                getClaims(token);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        private Claims getClaims(String token) {
            return Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        }
        // Thêm vào JwtUtils.java

        public String generateRefreshToken(String email) {
            return Jwts.builder()
                    .setSubject(email)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000)) // 7 ngày
                    .signWith(getKey())
                    .compact();
        }

        public boolean isTokenExpired(String token) {
            try {
                return getClaims(token).getExpiration().before(new Date());
            } catch (Exception e) {
                return true;
            }
        }
    }

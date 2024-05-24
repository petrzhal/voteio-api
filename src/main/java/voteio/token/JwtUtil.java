package voteio.token;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JdbcTemplate jdbcTemplate;
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.lifetime}")
    private Duration jwtLifetime;

    public String generateAccessToken(UserDetails userDetails) {
        Date issueDate = new Date();
        Date expiredDate = new Date(issueDate.getTime() + jwtLifetime.toMillis());
        return JWT.create()
                .withSubject(userDetails.getUsername())
                .withIssuedAt(issueDate)
                .withExpiresAt(expiredDate)
                .sign(Algorithm.HMAC256(secret));
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Date issueDate = new Date();
        Date expiredDate = new Date(issueDate.getTime() + (jwtLifetime.toMillis() * 2));
        return JWT.create()
                .withSubject(userDetails.getUsername())
                .withIssuedAt(issueDate)
                .withExpiresAt(expiredDate)
                .sign(Algorithm.HMAC256(secret));
    }

    @Transactional
    public void saveToken(String token, Integer userId) {
        try {
            var id = jdbcTemplate.queryForObject("SELECT id FROM token WHERE user_id=?", Integer.class, userId);
            if (id != null) {
                jdbcTemplate.update("UPDATE token SET token = ? WHERE user_id=?", token, userId);
                return;
            }
            jdbcTemplate.update("INSERT INTO token (user_id, token) VALUES (?, ?)", userId, token);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public String getLogin(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    public DecodedJWT getClaimsFromToken(String token) {
        return JWT.decode(token);
    }

    public void checkExpiration(String token) {
        JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token);
    }
}

package ag.token;

import ag.models.Role;
import ag.models.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.jaas.AuthorityGranter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.lifetime}")
    private Duration jwtLifetime;

    public String generateToken(UserDetails userDetails) {
        Date issueDate = new Date();
        Date expiredDate = new Date(issueDate.getTime() + jwtLifetime.toMillis());
        return JWT.create()
                .withSubject(userDetails.getUsername())
                .withIssuedAt(issueDate)
                .withExpiresAt(expiredDate)
                .sign(Algorithm.HMAC256(secret));
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

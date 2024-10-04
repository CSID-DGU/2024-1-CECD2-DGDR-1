package dgdr.server.vonage.global.utils;

import dgdr.server.vonage.global.domain.TokenType;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Date;

@Slf4j
@Component
public class TokenProvider {
    @Value("${jwt.secret:s2i3g4n5a6l7s8e9c0r3e2t1x3123}")
    private String secret;

    @Value("${jwt.accessToken.expiration:3600}")
    private Long accessTokenExpiration;

    @Value("${jwt.refreshToken.expiration:86400}")
    private Long refreshTokenExpiration;

    public String createAccessToken(String userId) {
        byte[] signingKey = secret.getBytes(StandardCharsets.UTF_8);

        return Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(signingKey), SignatureAlgorithm.HS512)
                .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(accessTokenExpiration).toInstant()))
                .setSubject(userId)
                .claim("type", TokenType.ACCESS)
                .compact();
    }

    public String createRefreshToken(String userId) {
        byte[] signingKey = secret.getBytes(StandardCharsets.UTF_8);

        return Jwts.builder()
                .signWith(Keys.hmacShaKeyFor(signingKey), SignatureAlgorithm.HS512)
                .setExpiration(Date.from(ZonedDateTime.now().plusDays(refreshTokenExpiration).toInstant()))
                .setSubject(userId)
                .claim("type", TokenType.REFRESH)
                .compact();
    }

    public String getUserIdByToken(String token) {
        byte[] signingKey = secret.getBytes(StandardCharsets.UTF_8);

        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            byte[] signingKey = secret.getBytes(StandardCharsets.UTF_8);
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String getTokenTypeByToken(String token) {
        byte[] signingKey = secret.getBytes(StandardCharsets.UTF_8);

        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("type")
                .toString();
    }


}

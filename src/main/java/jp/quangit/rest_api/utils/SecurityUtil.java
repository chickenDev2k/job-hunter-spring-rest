package jp.quangit.rest_api.utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.util.Base64;

@Service
public class SecurityUtil {
    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;
    private final JwtEncoder jwtEncoder;

    public SecurityUtil(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    @Value("${quangit.jwt.base64-secret}")
    private String jwtKey;

    @Value("${quangit.jwt.token-validity-in-seconds}")
    private long jwtExpiration;

    public String createToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.jwtExpiration, ChronoUnit.SECONDS);
        // @formatter:off
        JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuedAt(now)
        .expiresAt(validity)
        .subject(authentication.getName())
        .claim("quangit", authentication)
        .build();
        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader,
        claims)).getTokenValue();
            }
}

package jp.quangit.rest_api.utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
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


    /**
     * Get the login of the current user.
     *
     * @return the login of the current user.
     */
    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        } else if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        } else if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        } else if (authentication.getPrincipal() instanceof String s) {
            return s;
        }
        return null;
    }

    /**
     * Get the JWT of the current user.
     *
     * @return the JWT of the current user.
     */
    public static Optional<String> getCurrentUserJWT() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(securityContext.getAuthentication())
            .filter(authentication -> authentication.getCredentials() instanceof String)
            .map(authentication -> (String) authentication.getCredentials());
    }

    /**
     * Check if a user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise.
     */
    // public static boolean isAuthenticated() {
    //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //     return authentication != null && getAuthorities(authentication).noneMatch(AuthoritiesConstants.ANONYMOUS::equals);
    // }

    /**
     * Checks if the current user has any of the authorities.
     *
     * @param authorities the authorities to check.
     * @return true if the current user has any of the authorities, false otherwise.
     */
    // public static boolean hasCurrentUserAnyOfAuthorities(String... authorities) {
    //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //     return (
    //         authentication != null && getAuthorities(authentication).anyMatch(authority -> Arrays.asList(authorities).contains(authority))
    //     );
    // }

    /**
     * Checks if the current user has none of the authorities.
     *
     * @param authorities the authorities to check.
     * @return true if the current user has none of the authorities, false otherwise.
     */
    // public static boolean hasCurrentUserNoneOfAuthorities(String... authorities) {
    //     return !hasCurrentUserAnyOfAuthorities(authorities);
    // }

    /**
     * Checks if the current user has a specific authority.
     *
     * @param authority the authority to check.
     * @return true if the current user has the authority, false otherwise.
     */
    // public static boolean hasCurrentUserThisAuthority(String authority) {
    //     return hasCurrentUserAnyOfAuthorities(authority);
    // }

    // private static Stream<String> getAuthorities(Authentication authentication) {
    //     return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority);
    // }

}

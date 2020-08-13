package com.paymybuddy.PayMyBuddyWeb.services;

import com.paymybuddy.PayMyBuddyWeb.Utils.MSStringUtils;
import com.paymybuddy.PayMyBuddyWeb.interfaces.dao.AccountDAOInterface;
import com.paymybuddy.PayMyBuddyWeb.interfaces.dao.SecurityDAOInterface;
import com.paymybuddy.PayMyBuddyWeb.interfaces.dao.UserDAOInterface;
import com.paymybuddy.PayMyBuddyWeb.interfaces.service.SecurityServiceInterface;
import com.paymybuddy.PayMyBuddyWeb.models.Country;
import com.paymybuddy.PayMyBuddyWeb.models.User;
import io.jsonwebtoken.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.crypto.spec.SecretKeySpec;
import javax.inject.Singleton;
import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.Key;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class SecurityService implements SecurityServiceInterface {
    /**
     * Logger log4j2
     */
    private static final Logger logger = LogManager.getLogger("SecurityService");

    /**
     * Security DAO
     */
    private final SecurityDAOInterface securityDAO;

    /**
     * User DAO
     */
    private final UserDAOInterface userDAO;

    /**
     * Account DAO
     */
    private final AccountDAOInterface accountDAO;

    /**
     * Password manager
     */
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${jwt.secret}")
    private String JWTKey;

    /**
     * Constructor
     * @param securityDAO
     */
    public SecurityService(SecurityDAOInterface securityDAO, BCryptPasswordEncoder passwordEncoder, UserDAOInterface userDAO, AccountDAOInterface accountDAO) {
        this.securityDAO = securityDAO;
        this.passwordEncoder = passwordEncoder;
        this.userDAO = userDAO;
        this.accountDAO = accountDAO;
    }

    /**
     * @see SecurityServiceInterface {@link #registerUser(Map)}
     */
    @Override
    public Map<String, String> registerUser(Map<String, Object> signUpParams) throws IOException {
        Map<String, String> loginInformations = null;
        if ( signUpParams != null &&
                !MSStringUtils.isEmpty((String) signUpParams.get("firstname")) &&
                !MSStringUtils.isEmpty((String) signUpParams.get("lastName")) &&
                !MSStringUtils.isEmpty((String) signUpParams.get("email")) &&
                !MSStringUtils.isEmpty((String) signUpParams.get("password")) &&
                !MSStringUtils.isEmpty((String) signUpParams.get("age_year")) &&
                !MSStringUtils.isEmpty((String) signUpParams.get("age_month")) &&
                !MSStringUtils.isEmpty((String) signUpParams.get("age_day")) &&
                !MSStringUtils.isEmpty((String) signUpParams.get("country")) &&
                !MSStringUtils.isEmpty((String) signUpParams.get("currency"))
        ) {
            Integer year = Integer.valueOf((String) signUpParams.get("age_year"));
            Integer month = Integer.valueOf((String) signUpParams.get("age_month"));
            Integer day = Integer.valueOf((String) signUpParams.get("age_day"));

            LocalDate birthday = LocalDate.of(year, month, day);
            Country country = new Country((String) signUpParams.get("country"));
            User newUser = new User(null, (String) signUpParams.get("firstname"), (String) signUpParams.get("lastname"), birthday, (String) signUpParams.get("email"), country, null);
            userDAO.createUser(newUser, (String) signUpParams.get("currency"), passwordEncoder.encode((String) signUpParams.get("password")));
            loginInformations = logUser((String) signUpParams.get("email"), (String) signUpParams.get("password"), false);
        } else {
            logger.info("SecurityService.registerUser : Incomplete profile");
        }
        return loginInformations;
    }

    /**
     * @see SecurityServiceInterface {@link #logUser(String, String, Boolean)}
     */
    @Override
    public Map<String, String> logUser(String username, String password, Boolean rememberUser) {
        Map<String, String> loginInformations = null;
        String encodePassword = securityDAO.getUserPassword(username);
        if (encodePassword != null && !encodePassword.isEmpty() && passwordEncoder.matches(password, encodePassword)) {
            loginInformations = new HashMap<>();
            User user = userDAO.getUserByUsername(username);

            Map<String, Object> claims = new HashMap<>();
            claims.put("username", user.getEmail());
            claims.put("name", user.getFirstName() + " " + user.getLastName());
            if (rememberUser) {
                loginInformations.put("token", createJWT(user.getId(), "Login", "PayMyBuddy", claims, 1000 * 60 * 60 * 24 * 365));
            } else {
                loginInformations.put("token", createJWT(user.getId(), "Login", "PayMyBuddy", claims, 1000 * 60 * 60 * 5));
            }
        }
        return loginInformations;
    }

    /**
     * @see SecurityServiceInterface {@link #createJWT(Integer, String, String, Map, long)}
     */
    public String createJWT(Integer id, String subject, String issuer, Map<String, Object> claims, long ttlMillis) {
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(JWTKey);
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        JwtBuilder builder = Jwts.builder()
                .setId(id.toString())
                .setIssuedAt(now)
                .setSubject(subject)
                .setIssuer(issuer)
                .setClaims(claims)
                .signWith(signatureAlgorithm, signingKey);

        if (ttlMillis >= 0) {
            builder.setExpiration(new Date(nowMillis + ttlMillis));
        }

        return builder.compact();
    }

    /**
     * @see SecurityServiceInterface {@link #parseJWT(String)}
     */
    public Claims parseJWT(String token) {
        Claims claims = null;

        try {
            claims = Jwts.parser()
                    .setSigningKey(DatatypeConverter.parseBase64Binary(JWTKey))
                    .parseClaimsJws(token).getBody();
        } catch (SignatureException e) {
            logger.info("Invalid JWT signature.");
            logger.trace("Invalid JWT signature trace: {}", e);
        } catch (MalformedJwtException e) {
            logger.info("Invalid JWT token.");
            logger.trace("Invalid JWT token trace: {}", e);
        } catch (ExpiredJwtException e) {
            logger.info("Expired JWT token.");
            logger.trace("Expired JWT token trace: {}", e);
        } catch (UnsupportedJwtException e) {
            logger.info("Unsupported JWT token.");
            logger.trace("Unsupported JWT token trace: {}", e);
        } catch (IllegalArgumentException e) {
            logger.info("JWT token compact of handler are invalid.");
            logger.trace("JWT token compact of handler are invalid trace: {}", e);
        }

        return claims;
    }

    /**
     * @see SecurityServiceInterface {@link #isLog(HttpSession)}
     */
    public Boolean isLog(HttpSession session) {
        String token = (String) session.getAttribute("token");
        return ((token != null) && (parseJWT(token) != null));
    }
}

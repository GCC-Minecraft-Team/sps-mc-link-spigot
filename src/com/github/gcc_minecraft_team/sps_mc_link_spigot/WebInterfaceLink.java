package com.github.gcc_minecraft_team.sps_mc_link_spigot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;

import io.javalin.Javalin;
import io.jsonwebtoken.*;

import java.util.Date;
import java.util.logging.Level;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;

public class WebInterfaceLink {

    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe

    // start server
    public static void Listen() {
        Javalin app = null;

        /*
         * Below is an extremely stupid fix for Javalin. You have to do this whenever you're using it with Spigot
         */

        // Get the current class loader.
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // Temporarily set this thread's class loader to the plugin's class loader.
        // Replace JavalinTestPlugin.class with your own plugin's class.
        Thread.currentThread().setContextClassLoader(SPSSpigot.class.getClassLoader());

        // Instantiate the web server (which will now load using the plugin's class loader).
        app = Javalin.create().start(8000);
        app.get("/", context -> context.result("Yay Javalin works!"));

        // Put the original class loader back where it was.
        Thread.currentThread().setContextClassLoader(classLoader);
        SPSSpigot.logger().log(Level.INFO, "Listening for web-app requests on port 8000!");

        // listen for post request from web app
        app.post("/registerPlayer", ctx -> {
            SPSSpigot.logger().log(Level.INFO, "request " + ctx.body());
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode newUser = objectMapper.readTree(ctx.body());
            SPSSpigot.logger().log(Level.INFO, "request " + newUser.toString());

            String newUUID = null;

            // try to decode JWT
            try {
                newUUID = DecodeJWT(newUser.get("token").asText()).getId();
            } catch (JwtException exception) {
                SPSSpigot.logger().log(Level.SEVERE, "Something went wrong decoding a JSON web token");
            }

            // send response and load database data
            if (newUUID != null) {
                DatabaseLink.registerPlayer(newUUID, newUser.get("id").asText(), newUser.get("nick").asText());
                // success
                ctx.status(200);
            } else {
                // internal server error
                ctx.status(500);
            }
        });
    }

    public static String CreateJWT(String id, String issuer, String subject, long ttlMillis) {

        //The JWT signature algorithm we will be using to sign the token
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //We will sign our JWT with our ApiKey secret
        byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(PluginConfig.GetJWTSecret());
        Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());

        //Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder().setId(id)
                .setIssuedAt(now)
                .setSubject(subject)
                .setIssuer(issuer)
                .signWith(signatureAlgorithm, signingKey);

        //if it has been specified, let's add the expiration
        if (ttlMillis > 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

        //Builds the JWT and serializes it to a compact, URL-safe string
        return builder.compact();
    }

    public static Claims DecodeJWT(String jwt) {
        //This line will throw an exception if it is not a signed JWS (as expected)
        Claims claims = Jwts.parser()
                .setSigningKey(DatatypeConverter.parseBase64Binary(PluginConfig.GetJWTSecret()))
                .parseClaimsJws(jwt).getBody();
        return claims;
    }

}

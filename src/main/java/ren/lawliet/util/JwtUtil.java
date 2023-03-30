package ren.lawliet.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.*;


public class JwtUtil {
    private final static Long access_token_expiration = 7200L;

    private static SecretKey generalKey(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode("regLoginregLoginregLoginregLoginregLoginregLoginregLoginregLogin"));
    }
    public static String generateJwtToken(String uuid){
        Map<String, Object> claims = new HashMap<>();
        claims.put("uuid",uuid);
        return Jwts.builder()
                .signWith(generalKey())
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+access_token_expiration*1000))
                .compact();

    }
    public static Claims getClaimsFromJwt(String token){
        Claims claims = null;
        try {
            claims = Jwts.parserBuilder().setSigningKey(generalKey()).build().parseClaimsJws(token).getBody();
        }catch (Exception e){
            e.printStackTrace();
        }
        return claims;
    }
}

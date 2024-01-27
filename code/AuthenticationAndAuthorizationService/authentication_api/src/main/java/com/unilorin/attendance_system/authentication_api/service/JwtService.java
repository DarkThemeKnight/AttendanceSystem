package com.unilorin.attendance_system.authentication_api.service;
import com.unilorin.attendance_system.authentication_api.entity.ApplicationUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {
    @Value("${token}")
    private String tokenSecretKey;
    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7); // Extract the token excluding "Bearer "
        } else {
            log.error("Invalid JWT token in Authorization header");
            throw new IllegalArgumentException("Invalid JWT token in Authorization header");
        }
    }
    public String extractMail(String jwtToken) {
        return extractClaim(jwtToken, Claims::getSubject);
    }
    private <T> T extractClaim(String tokenSecretKey, Function<Claims,T> fn) {
        final Claims claims = extractAllClaims(tokenSecretKey);
        return fn.apply(claims);
    }

    public boolean isValidToken(String token, UserDetails applicationUser) {
        final String username = extractMail(token);
        return username.equals(applicationUser.getUsername()) && !isExpired(token);
    }
    public String generate(Map<String,Object> map, ApplicationUser user){
        Date date = new Date(System.currentTimeMillis());
        return Jwts.builder()
                .setClaims(map)
                .setSubject(user.getSchoolEmail())
                .setIssuedAt(date)
                .setExpiration(new Date(date.getTime()+(1000*60*60*6))) //6 hrs
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    private boolean isExpired(String tokenSecretKey){
         return extractClaim(tokenSecretKey,Claims::getExpiration).before(new Date(System.currentTimeMillis()));
    }
    private Claims extractAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    private Key getSecretKey(){
        byte[] keyBytes = Decoders.BASE64.decode(tokenSecretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
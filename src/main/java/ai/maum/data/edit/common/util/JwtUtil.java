package ai.maum.data.edit.common.util;

import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Map;

import ai.maum.data.edit.api.role.RoleType;
import ai.maum.data.edit.api.user.UserEntity;
import ai.maum.data.edit.common.enums.ResponseMeta;
import ai.maum.data.edit.common.enums.SecurityMeta;
import ai.maum.data.edit.common.enums.TokenType;
import ai.maum.data.edit.common.exception.BaseException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

public class JwtUtil {
  private JwtUtil() {
  }

  public static String generateToken(UserEntity user, TokenType type) {
    final Map<String, Object> claims = Map.of(
        "userId", user.getUserId().toString(),
        "clientId", user.getClientId(),
        "role", user.getIsAdmin() ? RoleType.ADMIN.getCode() : RoleType.USER.getCode()
    );

    return generateToken(claims, type);
  }

  public static String generateToken(Map<String, Object> claims, TokenType type) {
    long expireTime = type == TokenType.ACCESS ? SecurityMeta.ACCESS_TOKEN_EXPIRE_TIME :
        SecurityMeta.REFRESH_TOKEN_EXPIRE_TIME;

    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(new Date(System.currentTimeMillis() + expireTime))
        .signWith(Keys.hmacShaKeyFor(SecurityMeta.TOKEN_SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
        .compact();
  }

  public static ObjectId getClientId(String token) {
    return new ObjectId((String) extractAllClaims(token).get("clientId"));
  }

  public static RoleType getRole(String token) {
    return RoleType.fromCode((String) extractAllClaims(token).get("role"));
  }

  public static ObjectId getUserId(String token) {
    return new ObjectId((String) extractAllClaims(token).get("userId"));
  }

  public static boolean isTokenExpired(String token) {
    return getExpiration(token).before(new Date());
  }

  public static boolean validateToken(String token, UserEntity user) {
    Claims claims = extractAllClaims(token);
    // 정보 검증 로직
    return user.getUserId().toString().equals(claims.get("userId"));
  }

  private static Claims extractAllClaims(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(SecurityMeta.TOKEN_SECRET_KEY.getBytes())
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (MalformedJwtException | SignatureException | IllegalArgumentException e) {
      throw BaseException.of(ResponseMeta.TOKEN_INVALID);
    } catch (ExpiredJwtException e) {
      return e.getClaims();
    }
  }

  private static Date getExpiration(String token) {
    return extractAllClaims(token).getExpiration();
  }
}

package ai.maum.data.edit.common.config.security;

import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ai.maum.data.edit.api.role.RoleType;
import ai.maum.data.edit.api.user.UserEntity;
import ai.maum.data.edit.api.user.repository.UserRepository;
import ai.maum.data.edit.common.config.annotation.NoToken;
import ai.maum.data.edit.common.enums.RegexMeta;
import ai.maum.data.edit.common.enums.ResponseMeta;
import ai.maum.data.edit.common.enums.SecurityMeta;
import ai.maum.data.edit.common.exception.BaseException;
import ai.maum.data.edit.common.util.JwtUtil;
import ai.maum.data.edit.common.util.StringUtil;

public class JwtFilter extends OncePerRequestFilter {

  private final RequestMappingHandlerMapping handlerMapping;
  private final UserRepository userRepository;
  private final AuthenticationEntryPoint authenticationEntryPoint;

  JwtFilter(RequestMappingHandlerMapping handlerMapping,
      UserRepository userRepository,
      AuthenticationEntryPoint authenticationEntryPoint
  ) {
    this.handlerMapping = handlerMapping;
    this.userRepository = userRepository;
    this.authenticationEntryPoint = authenticationEntryPoint;
  }

  @Override
  protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
      @NotNull FilterChain filterChain) throws ServletException, IOException {
    try {
      String path = request.getServletPath();
      HttpSession session = request.getSession(false);

      if (StringUtil.matches(path, RegexMeta.UNNECESSARY_PATHS)) {
        filterChain.doFilter(request, response);
      } else if (StringUtil.matches(path, RegexMeta.DOCUMENT_PATHS)) {
        if (session == null || session.getAttribute("user") == null) {
          throw BaseException.of(ResponseMeta.UNAUTHORIZED_DOCUMENT);
        }
        filterChain.doFilter(request, response);
      } else if (StringUtil.matches(path, SecurityMeta.DOCUMENT_LOGIN_URL)) {
        if (session != null && session.getAttribute("user") != null) {
          throw BaseException.of(ResponseMeta.ALREADY_LOGIN_DOCUMENT_USER);
        }
        filterChain.doFilter(request, response);
      } else if (StringUtil.matches(path, RegexMeta.RESOURCE_PATHS)) {
        filterChain.doFilter(request, response);
      } else {
        HandlerMethod handlerMethod = (HandlerMethod) Objects.requireNonNull(handlerMapping
                .getHandler(request))
            .getHandler();

        if (handlerMethod.getMethodAnnotation(NoToken.class) != null) {
          filterChain.doFilter(request, response);
        } else {
          String token = request.getHeader("Authorization");

          if (StringUtils.isBlank(token)) {
            throw BaseException.of(ResponseMeta.UNAUTHORIZED);
          }
          token = token.replace("Bearer ", "");

          RoleType role = JwtUtil.getRole(token);
          UserEntity user = userRepository.findByUserId(JwtUtil.getUserId(token));
          if (user == null) {
            throw BaseException.of(ResponseMeta.TOKEN_INVALID);
          }

          if (JwtUtil.isTokenExpired(token)) {
            throw BaseException.of(ResponseMeta.ACCESS_TOKEN_EXPIRED);
          }

          if (JwtUtil.validateToken(token, user)) {
            if (!isTokenEquals(user, token)) {
              throw BaseException.of(ResponseMeta.TOKEN_INVALID);
            }
          } else {
            throw BaseException.of(ResponseMeta.TOKEN_INVALID);
          }

          SecurityContextHolder.getContext().setAuthentication(
              new UsernamePasswordAuthenticationToken(UserInfo.builder()
                  .userId(user.getUserId())
                  .password(user.getPassword())
                  .clientId(user.getClientId())
                  .role(role)
                  .build(),
                  user.getPassword())
          );

          filterChain.doFilter(request, response);
        }
      }
    } catch (Exception e) {
      String msg;
      if (e instanceof BaseException) {
        msg = ((BaseException) e).getInfo().getMessage();
      } else {
        msg = e.getMessage();
      }

      authenticationEntryPoint.commence(request, response, new AuthenticationException(msg, e) {
      });
    }
  }

  private boolean isTokenEquals(UserEntity user, String accessToken) {
    UserEntity.Token token = user.getToken();
    return token != null && StringUtils.isNotBlank(token.getAccessToken()) &&
        token.getAccessToken().equals(accessToken);
  }
}

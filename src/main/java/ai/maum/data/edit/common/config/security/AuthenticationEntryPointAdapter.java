package ai.maum.data.edit.common.config.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ai.maum.data.edit.common.enums.CodeInfo;
import ai.maum.data.edit.common.enums.RegexMeta;
import ai.maum.data.edit.common.enums.ResponseCode;
import ai.maum.data.edit.common.enums.ResponseMeta;
import ai.maum.data.edit.common.enums.SecurityMeta;
import ai.maum.data.edit.common.exception.BaseException;
import ai.maum.data.edit.common.filter.AccessAuthorityException;
import ai.maum.data.edit.common.util.LogUtil;
import ai.maum.data.edit.common.util.ResponseUtil;
import ai.maum.data.edit.common.util.StringUtil;

public class AuthenticationEntryPointAdapter implements AuthenticationEntryPoint {

  final List<String> unnecessaryPaths;

  public AuthenticationEntryPointAdapter() {
    this.unnecessaryPaths = Arrays.asList(
        "/",
        RegexMeta.UNNECESSARY_PATHS,
        RegexMeta.RESOURCE_PATHS
    );
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
      throws IOException {
    Exception e = (Exception) authException.getCause();

    if (e instanceof BaseException) {
      CodeInfo info = ((BaseException) e).getInfo();

      if (info.equals(ResponseMeta.UNAUTHORIZED) || info.equals(ResponseMeta.TOKEN_INVALID) ||
          info.equals(ResponseMeta.ACCESS_TOKEN_EXPIRED)) {
        String path = request.getServletPath();
        boolean isExcludedPath =
            unnecessaryPaths.stream().noneMatch(pattern -> StringUtil.matches(path, pattern) || path.equals(pattern));
        if (isExcludedPath) {
          LogUtil.error("req path: " + path
              + ", 인증 실패(" + info.getCode()
              + "), 토큰: " + request.getHeader("Authorization")
              + ", error: " + e);
        }
        ResponseUtil.success(response, info);
        return;
      }

      String redirectUrl;
      if (info.equals(ResponseMeta.UNAUTHORIZED_DOCUMENT)) {
        redirectUrl = SecurityMeta.DOCUMENT_LOGIN_URL;
      } else {
        redirectUrl = SecurityMeta.DOCUMENT_URL;
      }
      response.sendRedirect(redirectUrl);
    } else if (e instanceof AccessAuthorityException) {
      ResponseUtil.success(response, ResponseCode.ACCESS_DENIED);
    } else {
      String path = request.getServletPath();
      boolean isExcludedPath =
          unnecessaryPaths.stream().noneMatch(pattern -> StringUtil.matches(path, pattern) || path.equals(pattern));
      if (isExcludedPath) {
        LogUtil.error("req path: " + path
            + ", 인증 실패(" + ResponseMeta.AUTHENTICATION_FAILURE.getCode()
            + "), 토큰: " + request.getHeader("Authorization")
            + ", error: " + e);
      }
      ResponseUtil.success(response, ResponseMeta.AUTHENTICATION_FAILURE);
    }
  }
}

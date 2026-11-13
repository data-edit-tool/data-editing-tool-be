package ai.maum.data.edit.common.config.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ai.maum.data.edit.common.enums.ResponseMeta;
import ai.maum.data.edit.common.util.ResponseUtil;

public class AccessDeniedHandlerAdapter implements AccessDeniedHandler {
  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException accessDeniedException) throws IOException {
    ResponseUtil.success(response, ResponseMeta.ACCESS_DENIED);
  }
}

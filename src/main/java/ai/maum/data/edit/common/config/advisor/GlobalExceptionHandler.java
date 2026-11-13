package ai.maum.data.edit.common.config.advisor;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

import ai.maum.data.edit.common.enums.CodeInfo;
import ai.maum.data.edit.common.enums.LogLevelMeta;
import ai.maum.data.edit.common.enums.ResponseMeta;
import ai.maum.data.edit.common.exception.BaseException;
import ai.maum.data.edit.common.response.BaseResponse;
import ai.maum.data.edit.common.util.LogUtil;
import ai.maum.data.edit.common.util.StringUtil;
import lombok.RequiredArgsConstructor;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

  @ExceptionHandler({Exception.class})
  public BaseResponse<Void> handle(Exception e, HttpServletRequest request) {
    if (!(e instanceof BaseException) || ((BaseException) e).getInfo() == null) {
      String message = StringUtil.getStackTrace(e);
      LogUtil.notice(message, LogLevelMeta.ERROR);
      return BaseResponse.failure(e);
    } else {
      CodeInfo info = ((BaseException) e).getInfo();
      if (info.equals(ResponseMeta.UNAUTHORIZED) || info.equals(ResponseMeta.TOKEN_INVALID)
          || info.equals(ResponseMeta.ACCESS_TOKEN_EXPIRED) || info.equals(ResponseMeta.REFRESH_TOKEN_EXPIRED)) {
        LogUtil.error("req path: " + request.getServletPath()
            + ", 인증 실패(" + info.getCode()
            + "), 토큰: " + request.getHeader("Authorization"));
      } else {
        LogUtil.error(info.getCode() + ": " + info.getMessage());
      }
    }

    return BaseResponse.success(((BaseException) e).getInfo());
  }
}

package ai.maum.data.edit.common.config.interceptor;

import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ai.maum.data.edit.api.execinfo.ExecInfoEntity;
import ai.maum.data.edit.api.execinfo.ExecInfoRepository;
import ai.maum.data.edit.common.util.NetworkUtil;
import ai.maum.data.edit.common.util.ParsingUtil;

/**
 * API 수행 정보 인터셉터
 */
public class ExecInfoInterceptor implements HandlerInterceptor {

  private final ExecInfoRepository execInfoRepository;

  public ExecInfoInterceptor(ExecInfoRepository execInfoRepository) {
    this.execInfoRepository = execInfoRepository;
  }

  @Override
  public void afterCompletion(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
      @NotNull Object handler, Exception ex) {
    Map<String, Object> queryInfo = ParsingUtil.getQueryString(request);
    Object bodyInfo = request.getAttribute("args");

    if (bodyInfo != null) {
      if (bodyInfo instanceof List) {
        bodyInfo = ((List<?>) bodyInfo).stream()
            .map(value -> value != null ? value.toString() : "null")
            .collect(Collectors.toList());
      } else {
        bodyInfo = bodyInfo.toString();
      }
    }

    long endTime = System.currentTimeMillis();

    ExecInfoEntity execInfo = ExecInfoEntity.builder()
        .result((String) request.getAttribute("result"))
        .api(request.getServletPath())
        .time(endTime - (long) request.getAttribute("startTime"))
        .ip(NetworkUtil.getIp(request))
        .build();

    Object paramInfo = null;

    if (queryInfo != null && bodyInfo != null) {
      paramInfo = Map.of("query",
          queryInfo,
          "arg",
          bodyInfo);
    } else if (queryInfo != null) {
      paramInfo = queryInfo;
    } else if (bodyInfo != null) {
      paramInfo = bodyInfo;
    }

    if (paramInfo != null) {
      execInfo.setParams(paramInfo);
    }

    execInfoRepository.save(execInfo);
  }

  @Override
  public boolean preHandle(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
    request.setAttribute("startTime", System.currentTimeMillis());
    return true;
  }
}

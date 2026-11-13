package ai.maum.data.edit.common.util;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import ai.maum.data.edit.common.exception.BaseException;

public class NetworkUtil {
  private NetworkUtil() {
  }

  /**
   * 다른 서버와 HTTP 통신을 수행하는 RestTemplate 객체를 불러온다.
   *
   * @return RestTemplate 객체
   * @author baekgol
   */
  public static RestTemplate getClient() {
    return new RestTemplate();
  }

  /**
   * 요청 정보에서 클라이언트 IP를 불러온다.
   *
   * @param request 요청 정보
   * @return 클라이언트 IP
   * @author baekgol
   */
  public static String getIp(HttpServletRequest request) {
    String ip = request.getHeader("X-Forwarded-For");

    if (isEmpty(ip)) {
      ip = request.getHeader("Proxy-Client-IP");
    }
    if (isEmpty(ip)) {
      ip = request.getHeader("WL-Proxy-Client-IP");
    }
    if (isEmpty(ip)) {
      ip = request.getHeader("HTTP_CLIENT_IP");
    }
    if (isEmpty(ip)) {
      ip = request.getHeader("HTTP_X_FORWARDED_FOR");
    }
    if (isEmpty(ip)) {
      ip = request.getHeader("X-Real-IP");
    }
    if (isEmpty(ip)) {
      ip = request.getHeader("X-RealIP");
    }
    if (isEmpty(ip)) {
      ip = request.getHeader("REMOTE_ADDR");
    }
    if (isEmpty(ip)) {
      ip = request.getRemoteAddr();
    }
    if (isEmpty(ip)) {
      try {
        ip = InetAddress.getLocalHost().toString().split("\\/")[1];
      } catch (UnknownHostException ignored) {
      }
    }
    if (isEmpty(ip)) {
      ip = "unknown";
    }
    if (ip.contains(",")) {
      StringTokenizer st = new StringTokenizer(ip, ",");
      if (st.countTokens() > 0) {
        ip = st.nextToken();
      }
    }

    return ip;
  }

  /**
   * 다른 서버와 HTTP 통신을 수행한다.
   * 요청 헤더가 존재할 경우 Map<String, String> 객체를 파라미터로 넘겨준다.
   * 요청 쿼리가 존재할 경우 Map<String, Object> 객체를 파라미터로 넘겨준다.
   * 요청 바디가 존재할 경우 Object 객체를 파라미터로 넘겨준다.
   *
   * @param url          요청 URL
   * @param method       HTTP 메소드(GET, POST, PUT, PATCH, DELETE)
   * @param response     응답 클래스
   * @param contentType  데이터 형식
   * @param body         요청 바디 객체, Nullable
   * @param headerParams 요청 헤더 목록, Nullable
   * @param queryParams  요청 쿼리 파라미터 목록, Nullable
   * @return 응답 바디 객체, Nullable
   * @author baekgol
   */
  public static <T> T request(String url,
      HttpMethod method,
      Class<T> response,
      @Nullable MediaType contentType,
      @Nullable Object body,
      @Nullable Map<String, String> headerParams,
      @Nullable Map<String, Object> queryParams) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(contentType == null ? MediaType.APPLICATION_JSON : contentType);

    if (headerParams != null) {
      for (String key : headerParams.keySet()) {
        headers.add(key, headerParams.get(key));
      }
    }

    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);

    if (queryParams != null) {
      for (String key : queryParams.keySet()) {
        uriBuilder.queryParam(key, queryParams.get(key));
      }
    }

    T res;
    MultiValueMap<String, Object> info = null;

    try {
      HttpEntity<Object> entity = new HttpEntity<>(body, headers);

      if (contentType == MediaType.MULTIPART_FORM_DATA && body != null && !(body instanceof MultiValueMap)) {
        info = ParsingUtil.parseObjectToMultiValueMap(body);
        entity = new HttpEntity<>(info, headers);
      }

      res = new RestTemplate().exchange(uriBuilder.toUriString(),
              method,
              entity,
              response)
          .getBody();
    } catch (Exception e) {
      deleteTempFiles(info);
      return null;
    }
    deleteTempFiles(info);

    return res;
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private static void deleteTempFiles(MultiValueMap<String, Object> info) {
    if (info != null) {
      info.forEach((k, v) -> {
        if (v.size() > 0 && v.get(0) instanceof Resource) {
          for (Object resource : v) {
            try {
              File file = ((Resource) resource).getFile();
              file.delete();
              file.getParentFile().delete();
            } catch (IOException e) {
              throw BaseException.of(e);
            }
          }
        }
      });
    }
  }

  private static boolean isEmpty(String ip) {
    return ip == null
        || ip.length() == 0
        || ip.equalsIgnoreCase("unknown")
        || ip.equalsIgnoreCase("127.0.0.1")
        || ip.equalsIgnoreCase("localhost");
  }
}

package ai.maum.data.edit.common.util;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import ai.maum.data.edit.common.config.system.SystemConfig;

public class SystemUtil {
  private SystemUtil() {
  }

  /**
   * 서비스 컨텍스트를 반환한다.
   * 서비스 초기화 시점이나 static 영역에서는 반환할 수 없다.
   *
   * @author baekgol
   */
  public static ApplicationContext getContext() {
    return SystemConfig.getContext();
  }

  /**
   * 서비스를 종료한다.
   *
   * @author baekgol
   */
  public static void shutdown() {
    SpringApplication.exit(getContext());
  }

  /**
   * 서비스를 종료한다.
   * 예외 정보가 존재할 경우 해당 예외에 대한 정보를 로그 파일과 슬랙에 출력한다.
   *
   * @param e 예외
   * @author baekgol
   */
  public static void shutdown(Exception e) {
    ApplicationContext context = getContext();
    SpringApplication.exit(context);
  }

  /**
   * 컨텍스트를 통해 서비스를 종료한다.
   * 서비스 초기화 시점이나 static 영역에서 처리해야할 경우 호출하는 것을 권장한다.
   *
   * @author baekgol
   */
  public static void shutdownWithContext(ApplicationContext context) {
    SpringApplication.exit(context);
  }

  /**
   * 컨텍스트를 통해 서비스를 종료한다.
   * 서비스 초기화 시점이나 static 영역에서 처리해야할 경우 호출하는 것을 권장한다.
   * 예외 정보가 존재할 경우 해당 예외에 대한 정보를 로그 파일과 슬랙에 출력한다.
   *
   * @param e 예외
   * @author baekgol
   */
  public static void shutdownWithContext(ApplicationContext context, Exception e) {
    SpringApplication.exit(context);
  }
}

package ai.maum.data.edit.common.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ai.maum.data.edit.common.config.security.UserInfo;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AuthUtil {
  
  /**
   * 로그인 유저 정보 조회
   */
  public UserInfo getUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication != null && authentication.getPrincipal() instanceof UserInfo) {
      return (UserInfo) authentication.getPrincipal();
    } else {
      return null;
    }
  }
}

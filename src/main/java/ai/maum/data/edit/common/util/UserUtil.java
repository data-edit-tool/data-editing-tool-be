package ai.maum.data.edit.common.util;

import org.bson.types.ObjectId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import ai.maum.data.edit.api.user.enums.UserType;
import ai.maum.data.edit.common.config.security.UserInfo;
import ai.maum.data.edit.common.enums.ResponseCode;
import ai.maum.data.edit.common.exception.BaseException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserUtil {

  public String getClientId() {
    return getUser().getClientId();
  }

  public UserInfo getUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null) {
      throw BaseException.of(ResponseCode.AUTHENTICATION_FAILED);
    }

    Object principal = authentication.getPrincipal();
    if (principal instanceof UserInfo) {
      return (UserInfo) principal;
    }
    throw BaseException.of(ResponseCode.AUTHENTICATION_FAILED);
  }

  public ObjectId getUserId() {
    return getUser().getUserId();
  }

  public UserType getUserType() {
    return getUser().getType();
  }
}

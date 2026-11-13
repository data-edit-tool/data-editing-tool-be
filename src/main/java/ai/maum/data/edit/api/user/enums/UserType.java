package ai.maum.data.edit.api.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 사용자 종류
 */
@AllArgsConstructor
@Getter
public enum UserType {

  WORKER("W", "작업자"),
  CHECKER("C", "검수자"),
  VIEWER("V", "뷰어");

  private final String code;
  private final String name;

  public static UserType fromCode(String code) {
    for (UserType userType : UserType.values()) {
      if (userType.code.equals(code)) {
        return userType;
      }
    }
    throw new IllegalArgumentException("Unknown code: " + code);
  }
}

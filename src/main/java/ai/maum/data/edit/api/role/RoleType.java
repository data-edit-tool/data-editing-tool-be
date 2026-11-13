package ai.maum.data.edit.api.role;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RoleType {

  USER("USER"),
  ADMIN("ADMIN");

  private final String code;

  public static RoleType fromCode(String code) {
    for (RoleType roleType : values()) {
      if (roleType.getCode().equalsIgnoreCase(code)) {
        return roleType;
      }
    }
    throw new IllegalArgumentException("Unknown code: " + code);
  }
}

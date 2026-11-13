package ai.maum.data.edit.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ToolTypeMeta {
  T_TYPE_CC("T_TYPE_CC", "복합대화 가공 툴");

  private final String toolCode;
  private final String toolName;

  static public String getToolName(String toolCode) {
    String response = String.valueOf(toolCode);
    if (!response.equals("null")) {
      for (ToolTypeMeta type : ToolTypeMeta.values()) {
        if (response.equals(type.getToolCode())) {
          return type.getToolName();
        }
      }
    }
    return "";
  }
}

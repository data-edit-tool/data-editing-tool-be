package ai.maum.data.edit.common.response;

import lombok.Getter;

@Getter
public class CodeResponse {

  private final String code;
  private final String name;

  public CodeResponse(String code, String name) {
    this.code = code;
    this.name = name;
  }
}

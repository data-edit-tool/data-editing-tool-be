package ai.maum.data.edit.api.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminCreateRequest {

  @Schema(title = "사용자명")
  private String name;
  @Schema(title = "계정")
  private String account;
  @Schema(title = "비밀번호")
  private String password;
  @Schema(title = "복구 코드")
  private String recoveryCode;
}

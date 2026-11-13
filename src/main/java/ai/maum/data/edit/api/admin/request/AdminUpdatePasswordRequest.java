package ai.maum.data.edit.api.admin.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUpdatePasswordRequest {

  @Schema(title = "계정 복구 코드")
  private String recoveryCode;
  @Schema(title = "비밀번호")
  private String password;
}

package ai.maum.data.edit.api.admin.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(title = "관리자 사용자 등록 요청")
public class AdminUserRegisterRequest {

  @Schema(title = "사용자 계정", example = "worker001")
  private String account;
  @Schema(title = "사용자 설명", example = "1번 작업자입니다.")
  private String desc;
}

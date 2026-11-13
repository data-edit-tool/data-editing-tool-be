package ai.maum.data.edit.api.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(title = "초기 비밀번호 설정 요청")
public class PasswordInitRequest {

  @Schema(title = "사용자 계정", example = "worker001")
  private String account;
  @Schema(title = "변경 전 비밀번호", example = "SHA-512 암호화된 비밀번호")
  private String password;
  @Schema(title = "변경할 비밀번호", example = "SHA-512 암호화된 비밀번호")
  private String changePassword;
  @Schema(title = "변경할 비밀번호 확인", example = "1186964bbc221be8188b8b19114a6379e7c0b088252d2517d80fb36986888d31ef122c0ec96667977fa976f17d72f7302d175cd5472539be50a1c5015967f344")
  private String changePasswordCk;
}

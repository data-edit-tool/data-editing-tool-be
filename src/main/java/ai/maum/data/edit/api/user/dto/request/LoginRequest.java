package ai.maum.data.edit.api.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(title = "로그인 요청")
public class LoginRequest {

  @Schema(title = "사용자 계정", example = "worker001")
  private String account;
  @Schema(title = "사용자 비밀번호",
      description = "SHA-512 암호화된 비밀번호",
      example = "24bb6f9c1373bb7c8109974ae6bbee65d370fc8f67522ceeea9a68240875a72af0e524b5192539758fa0887e51a7849de746558c3ff8f81f1c1aba3f139442b6")
  private String password;
}

package ai.maum.data.edit.api.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(title = "AccessToken 재발급 요청")
public class ReissueRequest {

  @Schema(title = "리프레시 토큰", description = "JWT 리프레시 토큰", example = "token1234@")
  private String refreshToken;
}

package ai.maum.data.edit.api.user.dto.response;

import org.springframework.data.mongodb.core.mapping.Field;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(title = "워크스페이스 내에서의 사용자 권한(타입)")
public class UserTypeResponse {

  @Field("type")
  @Schema(title = "access_user type", example = "W")
  private String type;
}

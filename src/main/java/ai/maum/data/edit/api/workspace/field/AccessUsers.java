package ai.maum.data.edit.api.workspace.field;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import ai.maum.data.edit.api.user.enums.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
@Schema(title = "접근 가능 사용자", description = "워크스페이스에 접근한 가능 사용자")
public class AccessUsers {

  @Field("user_id")
  @Schema(title = "사용자 ID")
  private ObjectId userId;
  @Field("account")
  @Schema(title = "사용자 계정")
  private String account;
  @Field("type")
  @Schema(title = "사용자 타입", description = "사용자 타입(W:작업자/C:검수자/V:뷰어)", allowableValues = {"W", "C", "V"})
  private char type; /** @see UserType */
}

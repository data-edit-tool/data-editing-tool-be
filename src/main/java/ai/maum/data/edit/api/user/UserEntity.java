package ai.maum.data.edit.api.user;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import ai.maum.data.edit.common.domain.BaseField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Document(collection = "user")
public class UserEntity extends BaseField {

  @MongoId
  private ObjectId id;
  @Field("user_id")
  @Schema(title = "사용자 ID", example = "507f1f77bcf86cd799439011")
  private ObjectId userId;
  @Indexed(unique = true)
  @Field("account")
  @Schema(title = "사용자 계정", example = "worker001")
  private String account;
  @Field("name")
  private String name;
  @Field("desc")
  @Schema(title = "사용자 설명", example = "1번 작업자입니다.")
  private String desc;
  @Field("client_id")
  @Schema(title = "고객사 ID")
  private String clientId;
  @Setter
  @Field("password")
  @Schema(title = "비밀번호", description = "암호화된 비밀번호")
  private String password;
  @Setter
  @Field("pw_change_yn")
  @Schema(title = "비밀번호 초기화 상태")
  private Boolean pwChangeYn;
  @Field("is_admin")
  @Schema(title = "관리자 유무")
  private Boolean isAdmin;
  @Field("recovery_code")
  private String recoveryCode;
  @Field("token")
  @Schema(title = "토큰")
  private Token token;

  @Getter
  @ToString
  @Builder
  public static class Token {
    @Field("access_token")
    private String accessToken;
    @Field("refresh_token")
    private String refreshToken;
  }
}

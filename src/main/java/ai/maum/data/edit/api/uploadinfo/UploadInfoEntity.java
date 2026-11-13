package ai.maum.data.edit.api.uploadinfo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;
import java.util.List;

import ai.maum.data.edit.common.domain.BaseField;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Document("upload_info")
public class UploadInfoEntity extends BaseField {

  @MongoId
  private ObjectId id;
  @Field("user_id")
  private ObjectId userId;
  private List<Info> info;

  @Getter
  @ToString
  @Builder
  public static class Info {
    @Field("workspace_id")
    private ObjectId workspaceId;
    private LocalDateTime created;
  }
}

package ai.maum.data.edit.api.workspace;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDate;
import java.util.List;

import ai.maum.data.edit.api.workspace.field.AccessUsers;
import ai.maum.data.edit.api.workspace.field.Guides;
import ai.maum.data.edit.common.domain.BaseField;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Document(collection = "workspace")
public class WorkspaceEntity extends BaseField {

  @MongoId
  private ObjectId id;
  @Indexed(unique = true)
  @Field("workspace_id")
  private ObjectId workspaceId;
  @Field("client_id")
  private String clientId;
  @Field("workspace_name")
  private String workspaceName;
  @Field("incharge")
  private String incharge;
  @Field("start_date")
  private LocalDate startDate;
  @Field("end_date")
  private LocalDate endDate;
  @Field("workspace_intro")
  private String workspaceIntro;
  @Field("guides")
  private List<Guides> guides;
  @Field("access_users")
  private List<AccessUsers> accessUsers;
  @Field("is_deleted")
  @Schema(title = "삭제 여부")
  private Boolean isDeleted;
}

package ai.maum.data.edit.api.admin.workspace.vo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import ai.maum.data.edit.api.workspace.WorkspaceEntity;
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
public class AdminWorkspaceWithProjectsValue extends WorkspaceEntity {

  private List<ProjectInfo> projects;

  @Getter
  @Setter
  public static class ProjectInfo {
    @Field("project_id")
    private ObjectId projectId;
    @Field("post_status")
    private String postStatus;
    @Field("is_deleted")
    private boolean isDeleted;
  }
}

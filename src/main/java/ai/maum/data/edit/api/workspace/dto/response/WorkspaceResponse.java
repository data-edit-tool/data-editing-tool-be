package ai.maum.data.edit.api.workspace.dto.response;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import ai.maum.data.edit.api.project.dto.response.ProjectResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(title = "워크스페이스 상세 응답")
@Document("workspace")
public class WorkspaceResponse {

  @Field("workspace_id")
  private ObjectId workspaceId;
  @Schema(title = "고객사 ID")
  @Field("client_id")
  private String clientId;
  @Schema(title = "워크스페이스명")
  @Field("workspace_name")
  private String workspaceName;
  @Schema(title = "워크스페이스 소개")
  @Field("workspace_intro")
  private String workspaceIntro;
  @Schema(title = "프로젝트 목록")
  @Field("projects")
  private List<ProjectResponse> projects;
}

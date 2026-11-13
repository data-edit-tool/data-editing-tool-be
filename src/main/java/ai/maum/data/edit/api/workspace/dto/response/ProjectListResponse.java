package ai.maum.data.edit.api.workspace.dto.response;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import ai.maum.data.edit.api.project.dto.response.ProjectResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document("workspace")
public class ProjectListResponse {

  private String workspaceName;
  private Boolean isViewer;

  @Field("all_projects")
  @Schema(title = "Available Task")
  private List<ProjectResponse> allProjects;

  @Field("all_projects_by_user_id")
  @Schema(title = "Task in Progress")
  private List<ProjectResponse> allProjectsByUserId;
}

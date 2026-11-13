package ai.maum.data.edit.api.admin.job.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminJobWorkerUpdateRequest {

  private String workspaceId;
  private String projectId;
  private String userType;
  @Schema(nullable = true)
  private String userId;
}

package ai.maum.data.edit.api.admin.workspace.dto.response;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "관리자 워크스페이스 최근 업로드 완료 정보 조회 응답")
public class AdminWorkspaceLatestUploadResponse {

  @Field("workspace_id")
  @Schema(title = "워크스페이스 ID", example = "507f1f77bcf86cd799439011")
  private ObjectId workspaceId;
  @Field("workspace_name")
  @Schema(title = "워크스페이스명", example = "워크스페이스1")
  private String workspaceName;
}

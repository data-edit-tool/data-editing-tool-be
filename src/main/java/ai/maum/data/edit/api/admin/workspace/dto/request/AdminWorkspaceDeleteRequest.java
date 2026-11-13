package ai.maum.data.edit.api.admin.workspace.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(title = "관리자 워크스페이스 삭제 요청")
public class AdminWorkspaceDeleteRequest {

  @Schema(title = "삭제할 워크스페이스 ID 목록",
      example = "[\"507f1f77bcf86cd799439011\", \"507f1f77bcf86cd799439012\", \"507f1f77bcf86cd799439013\"]")
  private List<String> workspaceIds;
}

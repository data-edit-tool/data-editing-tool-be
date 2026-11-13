package ai.maum.data.edit.api.admin.workspace.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(title = "관리자 프로젝트 게시 상태 갱신 요청")
public class AdminWorkspaceUpdatePostStatusRequest {

  @Schema(title = "갱신할 게시 상태", description = "게시중: PJ_POST_ST_ON, 일시정지: PJ_POST_ST_OFF", example = "PJ_POST_ST_OFF")
  private String postStatus;
  @Schema(title = "프로젝트 ID 목록",
      example = "[\"507f1f77bcf86cd799439011\", \"507f1f77bcf86cd799439012\", \"507f1f77bcf86cd799439013\"]")
  private List<String> projectIds;
}

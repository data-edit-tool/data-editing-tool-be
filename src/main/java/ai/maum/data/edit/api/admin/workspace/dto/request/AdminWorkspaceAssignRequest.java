package ai.maum.data.edit.api.admin.workspace.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(title = "관리자 워크스페이스 작업자 등록 요청")
public class AdminWorkspaceAssignRequest {

  @Schema(title = "작업자 종류", description = "작업자: W, 검수자: C, 뷰어: V", example = "C")
  private char type;
  @Schema(title = "작업자로 등록될 사용자 ID 목록",
      example = "[\"507f1f77bcf86cd799439011\", \"507f1f77bcf86cd799439012\", \"507f1f77bcf86cd799439013\"]")
  private List<String> userIds;
}

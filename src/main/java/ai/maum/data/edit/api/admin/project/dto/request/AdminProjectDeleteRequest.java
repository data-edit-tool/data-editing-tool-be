package ai.maum.data.edit.api.admin.project.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(title = "관리자 프로젝트 삭제 요청")
public class AdminProjectDeleteRequest {

  @Schema(title = "삭제할 프로젝트 ID 목록",
      example = "[\"507f1f77bcf86cd799439011\", \"507f1f77bcf86cd799439012\", \"507f1f77bcf86cd799439013\"]")
  private List<String> projectIds;
}

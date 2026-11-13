package ai.maum.data.edit.api.project.dto.response;

import org.springframework.data.mongodb.core.mapping.Field;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(title = "프로젝트 정보 조회 응답")
public class ProjectInfoResponse {

  @Field("project_name")
  @Schema(title = "프로젝트 이름", example = "SET001_M001")
  private String projectName;
  @Field("user_type")
  @Schema(title = "작업자 유형(작업자: W, 검수자: C, 뷰어: V)", example = "W")
  private String userType;
  @Field("is_access")
  @Schema(title = "접근 가능 유무(가능: true, 불가능: false)", example = "true")
  private Boolean isAccess;
}

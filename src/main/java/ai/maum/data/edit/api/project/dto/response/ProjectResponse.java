package ai.maum.data.edit.api.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Builder
@Schema(title = "프로젝트 목록 조회 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectResponse {

  @Field("project_id")
  private ObjectId projectId;
  @Field("project_name")
  @Schema(title = "프로젝트명")
  private String projectName;
  @Field("post_status")
  @Schema(title = "게시 여부")
  private String postStatus;
  @Field("data_type")
  @Schema(title = "데이터 타입", description = "가공할 원천 데이터 타입")
  private Type dataType;
  @Field("tool_type")
  @Schema(title = "툴 타입 코드", description = "가공 툴 타입", example = "MC")
  private Type toolType;
  @Field("client_ctg_id")
  @Schema(title = "카테고리 ID", description = "레이블링 카테고리 목록 ID")
  private ObjectId clientCtgId;
  @Field("is_multiple_work")
  @Schema(title = "다중 작업 가능 여부")
  private Boolean isMultipleWork;
  @Field("created")
  private LocalDate created;

  @Getter
  @Setter
  @Builder
  public static class Type {
    @Field("name")
    private String name;
  }
}

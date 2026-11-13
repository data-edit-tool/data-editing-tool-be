package ai.maum.data.edit.api.admin.project.dto.response;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(title = "관리자 프로젝트 목록 조회 응답")
public class AdminProjectListResponse {

  @Field("project_count")
  @Schema(title = "전체 프로젝트 수", example = "3")
  private int projectCount;
  @Field("total_page")
  @Schema(title = "전체 페이지 수", example = "1")
  private int totalPage;
  @Field("result_count")
  @Schema(title = "검색 결과 수", example = "3")
  private int resultCount;
  @Schema(title = "프로젝트 정보 목록")
  private List<Project> projects;

  @Data
  @Builder
  public static class Project {

    @Field("project_id")
    @Schema(title = "프로젝트 ID", example = "507f1f77bcf86cd799439011")
    private ObjectId projectId;
    @Field("project_name")
    @Schema(title = "프로젝트명", example = "1번 프로젝트")
    private String name;
    @Field("data_type")
    @Schema(title = "데이터 유형", example = "이미지/비디오")
    private String dataType;
    @Field("tool_type")
    @Schema(title = "가공유형", example = "복합대화")
    private String toolType;
    @Field("category_type")
    @Schema(title = "카테고리 유형", example = "야생동물")
    private String categoryType;
    @Field("first_progress")
    @Schema(title = "1차 가공 진행률", example = "3/6")
    private String firstProgress;
    @Field("second_progress")
    @Schema(title = "2차 가공 진행률", example = "0/6")
    private String secondProgress;
    @Field("completed_progress")
    @Schema(title = "가공 완료 진행률", example = "0/6")
    private String completedProgress;
    @Field("upload_status")
    @Schema(title = "업로드 상태", example = "0/30")
    private String uploadStatus;
    @Field("project_status")
    @Schema(title = "프로젝트 상태", example = "진행중")
    private String projectStatus;
    @Field("post_status")
    @Schema(title = "게시 상태", example = "게시중")
    private String postStatus;
    @Schema(title = "생성날짜", example = "2023-05-31")
    private LocalDate created;
  }
}

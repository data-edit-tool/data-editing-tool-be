package ai.maum.data.edit.api.admin.workspace.dto.response;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(title = "관리자 워크스페이스 목록 조회 응답")
public class AdminWorkspaceListResponse {

  @Field("workspace_count")
  @Schema(title = "전체 워크스페이스 수", example = "3")
  private int workspaceCount;
  @Field("total_page")
  @Schema(title = "전체 페이지 수", example = "1")
  private int totalPage;
  @Field("result_count")
  @Schema(title = "검색 결과 수", example = "3")
  private int resultCount;
  @Schema(title = "워크스페이스 정보 목록")
  private List<Workspace> workspaces;

  @Data
  @Builder
  public static class Workspace {
    @Field("workspace_id")
    @Schema(title = "워크스페이스 ID", example = "507f1f77bcf86cd799439011")
    private ObjectId workspaceId;
    @Field("workspace_name")
    @Schema(title = "워크스페이스명", example = "1번 워크스페이스")
    private String name;
    @Field("workspace_intro")
    @Schema(title = "워크스페이스 소개", example = "1번 워크스페이스입니다.")
    private String desc;
    @Field("project_count")
    @Schema(title = "등록된 프로젝트 수", example = "3")
    private String projectCount;
    @Field("worker_count")
    @Schema(title = "작업자 수", example = "10")
    private String workerCount;
    @Field("checker_count")
    @Schema(title = "검수자 수", example = "5")
    private String checkerCount;
    @Field("viewer_count")
    @Schema(title = "뷰어 수", example = "3")
    private String viewerCount;
    @Field("incharge")
    @Schema(title = "담당자", example = "마음이")
    private String manager;
    @Schema(title = "워크스페이스 상태", example = "공개 중")
    private String status;
    @Field("start_date")
    @Schema(title = "시작일", example = "2023-05-31")
    private LocalDate startDate;
    @Field("end_date")
    @Schema(title = "종료일", example = "2023-06-30")
    private LocalDate endDate;
  }
}

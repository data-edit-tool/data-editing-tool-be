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
@Schema(title = "관리자 워크스페이스 조회 응답")
public class AdminWorkspaceResponse {

  @Schema(title = "워크스페이스명", example = "1번 워크스페이스")
  @Field("workspace_name")
  private String name;
  @Field("workspace_intro")
  @Schema(title = "워크스페이스 소개", example = "1번 워크스페이스입니다.")
  private String desc;
  @Field("incharge")
  @Schema(title = "담당자", example = "마음이")
  private String manager;
  @Field("start_date")
  @Schema(title = "작업 시작일", example = "2023-05-31")
  private LocalDate startDate;
  @Field("end_date")
  @Schema(title = "작업 종료일", example = "2023-06-30")
  private LocalDate endDate;
  @Schema(title = "가이드 파일 목록")
  private List<Guide> guides;

  @Data
  @Builder
  public static class Guide {
    @Field("guide_id")
    @Schema(title = "가이드 ID", example = "507f1f77bcf86cd799439011")
    private ObjectId guideId;
    @Field("guide_file_name")
    @Schema(title = "가이드 파일명", example = "file.txt")
    private String fileName;
    @Field("guide_file_url")
    @Schema(title = "가이드 파일 다운로드 URL", example = "https://maum.cloudfront.net/data_edit/workspace/507f1f77bcf86cd799439011/guide/67575137-2e20-4f62-b3cc-c10908c6aa73.txt")
    private String fileUrl;
    @Field("guide_file_size")
    @Schema(title = "가이드 파일 크기", example = "1280000")
    private long fileSize;
  }
}

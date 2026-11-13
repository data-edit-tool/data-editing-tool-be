package ai.maum.data.edit.api.admin.project.dto.response;

import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(title = "관리자 프로젝트 업로드 상태 목록 조회 응답")
public class AdminProjectFileListResponse {

  @Field("project_name")
  @Schema(title = "프로젝트 이름", example = "SET001_M001")
  private String projectName;
  @Field("total_count")
  @Schema(title = "전체 업로드 파일 수", example = "30개")
  private String totalCount;
  @Field("success_count")
  @Schema(title = "업로드 완료된 파일 수", example = "28개")
  private String successCount;
  @Field("error_count")
  @Schema(title = "업로드 실패한 파일 수", example = "2개")
  private String errorCount;
  @Field("total_page")
  @Schema(title = "전체 페이지 수", example = "1")
  private int totalPage;
  @Field("upload_infos")
  @Schema(title = "업로드 상태 정보 목록")
  private List<UploadInfo> uploadInfos;

  @Data
  @Builder
  public static class UploadInfo {
    @Schema(title = "파일 ID", example = "507f1f77bcf86cd799439011")
    @Field("file_id")
    private String fileId;
    @Field("file_name")
    @Schema(title = "파일명", example = "SET001_M001_S001_001_F_30_family_ws.wav")
    private String fileName;
    @Field("file_status")
    @Schema(title = "업로드 상태", example = "Success")
    private String fileStatus;
    @Field("file_size")
    @Schema(title = "파일 크기", example = "203MB")
    private String fileSize;
    @Field("file_type")
    @Schema(title = "파일 유형(발화자: SPEAKER, 원천 데이터: JOB, STT: STT)", example = "STT")
    private String fileType;
  }
}

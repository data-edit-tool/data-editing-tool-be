package ai.maum.data.edit.api.admin.project.dto.request;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(title = "관리자 프로젝트 파일 재업로드 요청")
public class AdminProjectReuploadRequest {

  @Schema(title = "파일 ID", example = "64a523400000000000e76891")
  private String fileId;
  @Schema(title = "파일 유형(발화자: SPEAKER, 원천 데이터: JOB, STT: STT)", example = "STT")
  private String fileType;
  @Schema(title = "파일")
  private MultipartFile file;
}

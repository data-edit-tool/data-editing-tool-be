package ai.maum.data.edit.api.admin.workspace.dto.request;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(title = "관리자 워크스페이스 등록 요청")
public class AdminWorkspaceRegisterRequest {

  @Schema(title = "워크스페이스명", example = "1번 워크스페이스")
  private String name;
  @Schema(title = "워크스페이스 소개", example = "1번 워크스페이스입니다.")
  private String desc;
  @Schema(title = "담당자", example = "마음이")
  private String manager;
  @Schema(title = "작업 시작일")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate start_date;
  @Schema(title = "작업 종료일")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate end_date;
  @Schema(title = "가이드 파일 목록", nullable = true)
  private List<MultipartFile> guides;
}

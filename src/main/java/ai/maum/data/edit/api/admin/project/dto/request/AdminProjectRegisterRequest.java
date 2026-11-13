package ai.maum.data.edit.api.admin.project.dto.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Schema(title = "관리자 프로젝트 등록 요청")
public class AdminProjectRegisterRequest {

  @Schema(title = "프로젝트명", example = "SET001_M002")
  private String name;
  @Schema(title = "데이터 유형 정보")
  private Type dataType;
  @Schema(title = "가공 유형 정보")
  private Type toolType;
  @Schema(title = "카테고리 유형 ID", example = "64a523400000000000e76889")
  private String categoryType;
  @Schema(title = "업로드할 발화자 이미지 파일 목록")
  private List<SpeakerFile> speakerFiles;
  private List<Subset> subsets;

  @Getter
  @Setter
  @ToString
  public static class Type {
    @Schema(title = "코드", example = "T_TYPE_CC")
    private String code;
    @Schema(title = "코드 이름", example = "복합대화 구간 태깅")
    private String name;
  }

  @Getter
  @Setter
  @ToString
  public static class SpeakerFile {
    @Schema(title = "업로드할 발화자 이미지 파일명", example = "SPEAKER_001_M_30_FRIEND.jpg")
    private String fileName;
    @Schema(title = "업로드할 발화자 이미지 파일 크기", example = "123456")
    private long fileSize;
  }

  @Getter
  @Setter
  @ToString
  public static class Subset {
    @Schema(title = "서브셋명", example = "S001")
    private String name;
    @Schema(title = "업로드할 데이터 파일 목록", example = "[{\"file_name\": \"SET001_M002_S001_001_F_30_family_cs.mp4\", \"file_size\": 123456 }, {\"file_name\": \"SET001_M002_S001_STT8K_001.wav\", \"file_size\": 123456 }]")
    private List<File> files;

    @Getter
    @Setter
    @ToString
    public static class File {
      @Schema(title = "업로드할 데이터 파일명")
      private String fileName;
      @Schema(title = "업로드할 데이터 파일 크기")
      private long fileSize;
    }
  }
}

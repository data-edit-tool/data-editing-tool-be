package ai.maum.data.edit.api.workspace.field.project;

import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ComplexConversation extends Project {

  @Field("speakers")
  @Schema(title = "발화자들")
  private List<Speaker> speakers;
  @Field("relationship")
  @Schema(title = "발화자들 간의 관계", example = "가족")
  private String relationship;

  @Getter
  @ToString
  @Builder
  public static class Speaker {
    @Field("speaker_id")
    @Schema(title = "발화자 고유 번호", description = "복합대화 영상 내 화자 별 고유 번호", example = "001")
    private String speakerId;
    @Field("age")
    @Schema(title = "발화자 연령대", example = "10")
    private int age;
    @Field("gender")
    @Schema(title = "발화자 성별", description = "발화자 성별(남자: M, 여자: F)", example = "M")
    private String gender;
    @Schema(title = "발화자 역할", example = "SYSTEM")
    private String role;
    @Field("thumbnail_file_name")
    @Schema(title = "발화자 이미지 파일명", example = "SPEAKER_001_M_30_FRIEND.jpg")
    private String thumbnailFileName;
    @Field("thumbnail_file_path")
    @Schema(title = "발화자 이미지 파일 경로", example = "/home/minds/...")
    private String thumbnailFilePath;
    @Field("thumbnail_file_url")
    @Schema(title = "발화자 이미지 파일 다운로드 URL", example = "https://maum.cloudfront.net/data_edit/workspace/507f1f77bcf86cd799439011/project/507f1f77bcf86cd799439012/speaker/67575137-2e20-4f62-b3cc-c10908c6aa73.jpg")
    private String thumbnailFileUrl;
    @Field("thumbnail_file_status")
    @Schema(title = "발화자 이미지 파일 상태", example = "PENDING")
    private String thumbnailFileStatus;
    @Field("thumbnail_file_size")
    @Schema(title = "발화자 이미지 파일 크기", example = "123123")
    private Long thumbnailFileSize;
  }
}

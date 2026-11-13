package ai.maum.data.edit.api.admin.project.vo;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
public class SpeakerForUploadValue {

  @Field("speaker_id")
  private String speakerId;
  private int age;
  private String gender;
  @Field("thumbnail_file_name")
  private String thumbnailFileName;
  @Field("thumbnail_file_status")
  private String thumbnailFileStatus;
  @Field("thumbnail_file_path")
  private String thumbnailFilePath;
}

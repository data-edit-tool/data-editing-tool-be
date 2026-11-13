package ai.maum.data.edit.api.project.vo;

import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpeakerInfo {
  @Field("speaker_id")
  private String speakerId;
  @Field("thumbnail_file_url")
  private String thumbnailFileUrl;
}

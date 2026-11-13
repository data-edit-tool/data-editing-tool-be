package ai.maum.data.edit.api.job.vo;

import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VoiceProperties {

  @Field("speaker_id")
  private String speakerId;

  @Field("measurements")
  private List<Measurement> measurements;

  @Getter
  @Setter
  public static class Measurement {
    @Field("offset_millis")
    private Long offsetMillis;
    @Field("pitch")
    private Double pitch;
    @Field("loudness")
    private Double loudness;
  }
}
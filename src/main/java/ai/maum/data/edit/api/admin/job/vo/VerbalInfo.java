package ai.maum.data.edit.api.admin.job.vo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Builder;
import lombok.Getter;

@Getter
public class VerbalInfo {
  @Field("verbal_id")
  private final ObjectId verbalId;
  @Field("start_time")
  private final int startTime;
  @Field("end_time")
  private final int endTime;
  @Field("sentence")
  private final String sentence;

  @Builder
  public VerbalInfo(ObjectId verbalId, int startTime, int endTime, String sentence) {
    this.verbalId = verbalId;
    this.startTime = startTime;
    this.endTime = endTime;
    this.sentence = sentence;
  }
}

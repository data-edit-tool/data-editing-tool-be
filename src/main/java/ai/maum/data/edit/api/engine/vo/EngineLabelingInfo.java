package ai.maum.data.edit.api.engine.vo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import ai.maum.data.edit.api.admin.job.vo.VerbalInfo;
import lombok.Getter;

@Getter
public class EngineLabelingInfo {
  @Field("labeling_id")
  private final ObjectId labelingId;
  @Field("speaker_id")
  private final String speakerId;
  @Field("verbals")
  private final List<VerbalInfo> verbals;

  public EngineLabelingInfo(ObjectId labelingId, String speakerId, List<VerbalInfo> verbals) {
    this.labelingId = labelingId;
    this.speakerId = speakerId;
    this.verbals = verbals;
  }
}

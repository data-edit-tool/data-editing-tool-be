package ai.maum.data.edit.api.admin.job.vo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.List;

import ai.maum.data.edit.api.engine.vo.EngineLabelingInfo;
import lombok.Getter;

@Getter
public class StartJobLabelingInfo {
  @Field("labeling_id")
  private final ObjectId labelingId;
  @Field("speaker_id")
  private final String speakerId;
  @Field("verbal")
  private final List<VerbalInfo> verbal;
  @Field("paraverbal")
  private final List<Object> paraverbal;
  @Field("nonverbal")
  private final List<Object> nonverbal;

  public StartJobLabelingInfo(EngineLabelingInfo labelingInfo) {
    this.labelingId = labelingInfo.getLabelingId();
    this.speakerId = labelingInfo.getSpeakerId();
    this.verbal = labelingInfo.getVerbals();

    // 빈 리스트 삽입
    this.paraverbal = new ArrayList<>();
    this.nonverbal = new ArrayList<>();
  }
}

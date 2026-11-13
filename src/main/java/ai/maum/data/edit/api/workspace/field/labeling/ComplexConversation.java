package ai.maum.data.edit.api.workspace.field.labeling;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import ai.maum.data.edit.api.project.vo.SpeakerInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class ComplexConversation extends Labeling {

  @Field("verbal")
  private List<LabelingAll> verbal;
  @Field("labeling_all")
  private List<LabelingAll> labelingAll; // step 2,3에서 verbal이 아닌 데이터에 대해 보여줌.
  @Field("labeling_info")
  private List<LabelingInfo> labelingInfo;
  @Field("speakers")
  private List<SpeakerInfo> speakers;

  @Getter
  @Setter
  @NoArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class LabelingAll {
    @Field("start_time")
    @Schema(title = "시작 지점")
    private Integer startTime;
    @Field("end_time")
    @Schema(title = "종료 지점")
    private Integer endTime;

    @Field("speaker_id")
    private String speakerId;
    @Field("labeling_id")
    private ObjectId labelingId;

    // verbal
    @Field("verbal_id")
    private ObjectId verbalId;
    @Field("sentence")
    private String sentence;
    @Field("channel")
    private String channel;
    @Field("sentiment")
    private String sentiment;
    @Field("fc_intent")
    private String fcIntent;
    @Field("bc_intent")
    private String bcIntent;
    @Field("emotion")
    private String emotion; // nonverbal에도 속함
    @Field("affective_state")
    private String affectiveState; // nonverbal에도 속함
    @Field("verbal_vad")
    private VAD verbalVad;
    @Field("overall_vad")
    private VAD overallVad;

    // paraverbal
    @Field("paraverbal_id")
    private ObjectId paraverbalId;
    @Field("func")
    private String func; // nonverbal에도 속함

    //nonverbal
    @Field("nonverbal_id")
    private ObjectId nonverbalId;
    @Field("angle")
    private String angle;
    @Field("subject")
    private String subject;
    @Field("action")
    private String action;
    @Field("action_str")
    private String actionStr;
    @Field("obj")
    private String obj;
    @Field("intent")
    private String intent;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class VAD {
    @Field("valence")
    private Integer valence;
    @Field("arousal")
    private Integer arousal;
    @Field("dominance")
    private Integer dominance;
  }

  @Getter
  @Setter
  @Builder
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class LabelingInfo {
    @Field("labeling_id")
    private String labelingId;
    @Field("speaker_id")
    private String speakerId;
  }
}

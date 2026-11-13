package ai.maum.data.edit.api.engine;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

import ai.maum.data.edit.common.domain.BaseField;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Document(collection = "engine")
public class EngineEntity extends BaseField {

  @MongoId
  private ObjectId id;
  @Field("engine_id")
  private ObjectId engineId;
  @Field("job_id")
  private ObjectId jobId;
  private List<Stt> stt;

  @Getter
  @ToString
  @Builder
  public static class Stt {
    @Field("stt_id")
    private ObjectId sttId;
    @Field("speaker_id")
    private String speakerId;
    @Field("file_name")
    private String fileName;
    @Field("file_status")
    private String fileStatus;
    @Field("file_size")
    private Long fileSize;
    private List<Data> data;

    @Getter
    @ToString
    @EqualsAndHashCode
    @Builder
    public static class Data {
      @Field("data_id")
      private ObjectId dataId;
      @Field("start_time")
      private int startTime;
      @Field("end_time")
      private int endTime;
      private String sentence;
    }
  }
}

package ai.maum.data.edit.api.csv;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
@Document(collection = "csv")
public class CsvEntity {

  @Field("job_id")
  private ObjectId jobId;
  @Field("job_file_id")
  private ObjectId jobFileId;
  @Field("work_id")
  private ObjectId workId;
  @Field("speaker_id")
  private String speakerId;
  @Field("datas")
  @Schema(title = "사용자 계정")
  private List<Data> datas;

  @Getter
  @Builder
  public static class Data {
    @Field("time")
    private Integer time;
    @Field("pitch")
    private String pitch;
    @Field("loudness")
    private String loudness;
  }
}

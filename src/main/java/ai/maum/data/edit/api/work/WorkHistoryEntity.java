package ai.maum.data.edit.api.work;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
@Document(collection = "work_history")
public class WorkHistoryEntity {

  @MongoId
  private ObjectId id;
  @Field("work_id")
  private ObjectId workId;
  @Field("pre_work_status")
  private String preWorkStatus;
  @Field("work_status")
  private String workStatus;
  @Field("user_id")
  @Schema(title = "사용자 계정")
  private ObjectId userId;
  @Schema(title = "(작업불가||반려) 사유")
  private String reason;
  @CreatedDate
  private LocalDateTime created;
}

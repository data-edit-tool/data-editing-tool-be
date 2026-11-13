package ai.maum.data.edit.api.admin.project.vo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
public class EngineForUploadValue {

  @Field("engine_id")
  private ObjectId engineId;
  @Field("stt_id")
  private ObjectId sttId;
}

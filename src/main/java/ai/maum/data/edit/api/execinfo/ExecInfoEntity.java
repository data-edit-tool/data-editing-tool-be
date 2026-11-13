package ai.maum.data.edit.api.execinfo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import ai.maum.data.edit.common.domain.BaseField;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Document(collection = "exec_info")
public class ExecInfoEntity extends BaseField {

  @MongoId
  private ObjectId id;
  private String result;
  private String api;
  private Object params;
  private long time;
  private String ip;
}

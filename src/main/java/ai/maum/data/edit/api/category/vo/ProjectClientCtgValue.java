package ai.maum.data.edit.api.category.vo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectClientCtgValue {

  @Field("client_ctg_id")
  private ObjectId clientCtgId;
}

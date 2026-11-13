package ai.maum.data.edit.api.admin.workspace.vo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.Data;

@Data
public class AdminWorkspaceJoinedProjectsValue {

  @Field("user_id")
  private ObjectId userId;
  private boolean result;
}

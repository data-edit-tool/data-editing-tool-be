package ai.maum.data.edit.api.admin.user.vo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

import lombok.Getter;

@Getter
public class DeletableUserListValue {

  @Field("deletable_users")
  private List<ObjectId> deletableUsers;
}

package ai.maum.data.edit.api.admin.job.response;

import org.bson.types.ObjectId;

import lombok.Getter;

@Getter
public class AdminJobAssignableUserResponse {

  private final ObjectId id;
  private final String account;
  private final String description;

  public AdminJobAssignableUserResponse(ObjectId id, String account, String description) {
    this.id = id;
    this.account = account;
    this.description = description;
  }
}

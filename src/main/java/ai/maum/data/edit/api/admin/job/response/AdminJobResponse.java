package ai.maum.data.edit.api.admin.job.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.bson.types.ObjectId;

import ai.maum.data.edit.common.enums.WorkStatus;
import ai.maum.data.edit.common.response.CodeResponse;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminJobResponse {

  private final ObjectId id;
  private final CodeResponse workStatus;
  private final String name;
  private final UserInfo worker;
  private final UserInfo checker;

  public AdminJobResponse(ObjectId id, WorkStatus workStatus, String name, UserInfo worker, UserInfo checker) {
    this.id = id;
    this.workStatus = new CodeResponse(workStatus.getCode(), workStatus.getName());
    this.name = name;
    this.worker = worker;
    this.checker = checker;
  }

  @Getter
  public static class UserInfo {

    private final ObjectId id;
    private final String account;
    private final String description;

    public UserInfo(ObjectId id, String account, String description) {
      this.id = id;
      this.account = account;
      this.description = description;
    }
  }
}

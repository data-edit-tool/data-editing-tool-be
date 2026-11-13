package ai.maum.data.edit.api.admin.job.request;

import ai.maum.data.edit.common.request.PageRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminJobAssignableUserListRequest extends PageRequest {

  private String search;
  private String workspace_id;
  private String user_type;

  public AdminJobAssignableUserListRequest() {
    super.setSize(3);
  }
}

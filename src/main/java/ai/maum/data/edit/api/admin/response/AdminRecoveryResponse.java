package ai.maum.data.edit.api.admin.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminRecoveryResponse {

  private final Boolean isValid;
  private final String account;

  public AdminRecoveryResponse(Boolean isValid, String account) {
    this.isValid = isValid;
    this.account = account;
  }
}

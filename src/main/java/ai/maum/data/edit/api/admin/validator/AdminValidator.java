package ai.maum.data.edit.api.admin.validator;

import ai.maum.data.edit.api.admin.request.AdminCreateRequest;
import ai.maum.data.edit.common.enums.ResponseCode;
import ai.maum.data.edit.common.exception.BaseException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AdminValidator {

  public void validate(AdminCreateRequest request) {
    if (!request.getName().matches("^[가-힣a-zA-Z0-9\\p{Punct}\\s]{1,20}$")) {
      throw BaseException.of(ResponseCode.INVALID_ADMIN_NAME);
    }
    if (!request.getAccount().matches("^[a-zA-Z0-9]+$")) {
      throw BaseException.of(ResponseCode.INVALID_ADMIN_ACCOUNT);
    }
  }
}

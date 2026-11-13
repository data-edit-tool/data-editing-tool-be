package ai.maum.data.edit.api.user.repository;

import java.time.LocalDate;
import java.util.regex.Pattern;

import ai.maum.data.edit.api.admin.user.dto.response.AdminUserListResponse;
import ai.maum.data.edit.common.repository.BasicCustomRepository;

public interface UserCustomRepository extends BasicCustomRepository {

  AdminUserListResponse findUserList(String clientId,
      String filter,
      Pattern query,
      LocalDate startDate,
      LocalDate endDate,
      int offset,
      int limit);
}
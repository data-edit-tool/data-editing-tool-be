package ai.maum.data.edit.api.admin;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import ai.maum.data.edit.api.admin.request.AdminCreateRequest;
import ai.maum.data.edit.api.admin.request.AdminUpdatePasswordRequest;
import ai.maum.data.edit.api.admin.response.AdminRecoveryResponse;
import ai.maum.data.edit.api.user.UserEntity;
import ai.maum.data.edit.api.user.dto.request.LoginRequest;
import ai.maum.data.edit.api.user.dto.response.LoginResponse;
import ai.maum.data.edit.api.user.repository.UserRepository;
import ai.maum.data.edit.common.enums.ResponseCode;
import ai.maum.data.edit.common.enums.TokenType;
import ai.maum.data.edit.common.exception.BaseException;
import ai.maum.data.edit.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final UserRepository userRepository;

  public void create(AdminCreateRequest request) {
    // 관리자 계정 존재 여부 검증
    if (getAdminExist()) {
      throw BaseException.of(ResponseCode.ADMIN_ALREADY_EXIST);
    }

    // 동일 계정 존재 여부 검증
    if (userRepository.existsByAccountAndIsAdmin(request.getAccount(), true)) {
      throw BaseException.of(ResponseCode.USER_ALREADY_EXIST);
    }
    userRepository.save(UserEntity.builder()
        .userId(new ObjectId())
        .account(request.getAccount())
        .password(request.getPassword())
        .name(request.getName())
        .clientId("CL_0000001")
        .pwChangeYn(false)
        .isAdmin(true)
        .recoveryCode(request.getRecoveryCode())
        .build());
  }

  public Boolean getAdminExist() {
    return userRepository.existsByIsAdmin(true);
  }

  public LoginResponse login(LoginRequest request) {
    UserEntity user = getAdminByAccount(request.getAccount());

    if (!user.getPassword().equals(request.getPassword())) {
      throw BaseException.of(ResponseCode.PW_CHECK);
    }

    String accessToken = JwtUtil.generateToken(user, TokenType.ACCESS);
    String refreshToken = JwtUtil.generateToken(user, TokenType.REFRESH);

    updateToken(user.getUserId(), accessToken, refreshToken);
    return new LoginResponse(accessToken, refreshToken);
  }

  public void updatePassword(AdminUpdatePasswordRequest request) {
    UserEntity admin = getAdminByRecoveryCode(request.getRecoveryCode());

    admin.setPassword(request.getPassword());
    userRepository.save(admin);
  }

  public AdminRecoveryResponse verifyRecoveryCode(String recoveryCode) {
    return Optional.ofNullable(userRepository.findByIsAdminAndRecoveryCode(true, recoveryCode))
        .map(user -> new AdminRecoveryResponse(true, user.getAccount()))
        .orElseGet(() -> new AdminRecoveryResponse(false, null));
  }

  private UserEntity getAdminByAccount(String account) {
    UserEntity user = userRepository.findByAccountAndIsAdmin(account, true);

    if (user == null) {
      throw BaseException.of(ResponseCode.USER_NOT_EXIST);
    }
    return user;
  }

  private UserEntity getAdminByRecoveryCode(String recoveryCode) {
    UserEntity admin = userRepository.findByIsAdminAndRecoveryCode(true, recoveryCode);
    if (admin == null) {
      throw BaseException.of(ResponseCode.ADMIN_NOT_EXIST);
    }
    return admin;
  }

  private void updateToken(ObjectId userId, String accessToken, String refreshToken) {
    if (StringUtils.isAllBlank(accessToken, refreshToken)) {
      return;
    }

    Query query = Query.query(Criteria.where("user_id").is(userId));
    Map<String, String> set = new HashMap<>();

    if (StringUtils.isNotBlank(accessToken)) {
      set.put("access_token", accessToken);
    }
    if (StringUtils.isNotBlank(refreshToken)) {
      set.put("refresh_token", refreshToken);
    }

    userRepository.updateOneWithFilters(UserEntity.class, query, null, Map.of("token", set), null);
  }
}

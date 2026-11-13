package ai.maum.data.edit.api.user;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import ai.maum.data.edit.api.user.dto.request.LoginRequest;
import ai.maum.data.edit.api.user.dto.request.PasswordInitRequest;
import ai.maum.data.edit.api.user.dto.request.ReissueRequest;
import ai.maum.data.edit.api.user.dto.response.LoginResponse;
import ai.maum.data.edit.api.user.dto.response.ReissueResponse;
import ai.maum.data.edit.api.user.repository.UserRepository;
import ai.maum.data.edit.common.enums.ResponseCode;
import ai.maum.data.edit.common.enums.ResponseMeta;
import ai.maum.data.edit.common.enums.TokenType;
import ai.maum.data.edit.common.exception.BaseException;
import ai.maum.data.edit.common.util.AuthUtil;
import ai.maum.data.edit.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  /**
   * 비밀번호 초기화
   */
  public boolean initPassword(PasswordInitRequest request) {
    UserEntity user = userRepository.findByAccountAndPassword(request.getAccount(), request.getPassword());
    if (user == null) {
      throw BaseException.of(ResponseCode.USER_NOT_EXIST);
    }

    String changePassword = request.getChangePassword();
    String changePasswordCk = request.getChangePasswordCk();

    // '변경할 비밀번호'와 '변경할 비밀번호 재확인' 불일치 시
    if (!changePasswordCk.equals(changePassword)) {
      throw BaseException.of(ResponseCode.PW_CHECK);
    } // 이전 비밀번호와 변경할 비밀번호가 동일할 때
    else if (changePassword.equals(user.getPassword())) {
      throw BaseException.of(ResponseCode.PW_CHECK_PREV_CURR);
    }

    user.setPassword(changePassword);
    user.setPwChangeYn(true);
    userRepository.save(user);

    return true;
  }

  public LoginResponse login(LoginRequest request) {
    UserEntity user = getUser(request.getAccount());

    if (!user.getPassword().equals(request.getPassword())) {
      throw BaseException.of(ResponseCode.PW_CHECK);
    }

    String accessToken = JwtUtil.generateToken(user, TokenType.ACCESS);
    String refreshToken = JwtUtil.generateToken(user, TokenType.REFRESH);

    updateToken(user.getUserId(), accessToken, refreshToken);
    return new LoginResponse(accessToken, refreshToken);
  }
  
  public void logout() {
    userRepository.unsetField(UserEntity.class,
        Query.query(Criteria.where("user_id").is(new ObjectId(AuthUtil.getUser().getUsername()))), "token");
  }

  public ReissueResponse reissue(String authorization, ReissueRequest request) {
    ObjectId userId = JwtUtil.getUserId(request.getRefreshToken());
    UserEntity user = userRepository.findByUserId(userId);

    validateToken(user, request.getRefreshToken(), authorization.replace("Bearer ", ""));

    String accessToken = JwtUtil.generateToken(user, TokenType.ACCESS);
    String refreshToken = JwtUtil.generateToken(user, TokenType.REFRESH);
    updateToken(userId, accessToken, refreshToken);
    return new ReissueResponse(accessToken, refreshToken);
  }

  public void validateInitPassword(LoginRequest request) {
    UserEntity user = userRepository.findByAccountAndPassword(request.getAccount(), request.getPassword());
    if (user == null) {
      throw BaseException.of(ResponseCode.USER_NOT_EXIST);
    }

    Boolean pwChangeYn = user.getPwChangeYn();
    if (!pwChangeYn) {
      throw BaseException.of(ResponseCode.PW_INIT);
    }
  }

  private UserEntity getUser(String account) {
    UserEntity user = userRepository.findByAccountAndIsAdmin(account, false);
    if (user == null) {
      throw BaseException.of(ResponseCode.USER_NOT_EXIST);
    }
    return user;
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

  private void validateToken(UserEntity user, String refreshToken, String accessToken) {
    if (JwtUtil.isTokenExpired(refreshToken)) {
      throw BaseException.of(ResponseMeta.REFRESH_TOKEN_EXPIRED);
    }

    if (user == null) {
      throw BaseException.of(ResponseMeta.TOKEN_INVALID);
    }

    UserEntity.Token token = user.getToken();
    if (token == null || StringUtils.isAnyBlank(token.getRefreshToken(), token.getAccessToken())
        || !token.getRefreshToken().equals(refreshToken) || !token.getAccessToken().equals(accessToken)) {
      throw BaseException.of(ResponseMeta.TOKEN_INVALID);
    }
  }
}
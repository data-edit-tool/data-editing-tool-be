package ai.maum.data.edit.api.admin.user;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ai.maum.data.edit.api.admin.user.dto.request.AdminUserDeleteRequest;
import ai.maum.data.edit.api.admin.user.dto.request.AdminUserRegisterRequest;
import ai.maum.data.edit.api.admin.user.dto.response.AdminUserListForWorkspaceAssignmentResponse;
import ai.maum.data.edit.api.admin.user.dto.response.AdminUserListResponse;
import ai.maum.data.edit.api.user.UserEntity;
import ai.maum.data.edit.api.user.enums.UserMeta;
import ai.maum.data.edit.api.user.repository.UserRepository;
import ai.maum.data.edit.api.workspace.repository.WorkspaceRepository;
import ai.maum.data.edit.common.enums.ResponseCode;
import ai.maum.data.edit.common.exception.BaseException;
import ai.maum.data.edit.common.util.CryptoUtil;
import ai.maum.data.edit.common.util.UserUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminUserService {

  private final UserRepository userRepository;
  private final WorkspaceRepository workspaceRepository;

  public boolean checkUserAccount(String account) {
    return userRepository.existsByAccountAndIsAdmin(account, false);
  }

  public void deleteUser(AdminUserDeleteRequest request) {
    String clientId = UserUtil.getClientId();
    List<UserEntity> users = userRepository.findByClientIdAndUserIdIn(clientId, request.getUserIds()
        .stream()
        .map(ObjectId::new)
        .collect(Collectors.toList()));

    if (users.isEmpty()) {
      throw BaseException.of(ResponseCode.USER_NOT_EXIST);
    }

    if (users.stream().anyMatch(UserEntity::getIsAdmin)) {
      throw BaseException.of(ResponseCode.USER_FUNCTION_IMPOSSIBLE_ADMIN);
    }

    List<ObjectId> userIds = users
        .stream()
        .map(UserEntity::getUserId)
        .collect(Collectors.toList());

    List<ObjectId> deletableUserIds =
        workspaceRepository.findDeletableUserList(clientId, userIds).getDeletableUsers();

    workspaceRepository.updateAccessUsersForDeleteUser(clientId, deletableUserIds);

    userRepository.deleteByUserIdIn(deletableUserIds);

    if (userIds.size() != deletableUserIds.size()) {
      throw BaseException.of(ResponseCode.USER_DELETE_IMPOSSIBLE);
    }
  }

  public AdminUserListForWorkspaceAssignmentResponse getAssignedUserListForWorkspaceAssignment(
      ObjectId workspaceId, int page, Character type, String query) {
    String clientId = UserUtil.getClientId();
    boolean isExisted = workspaceRepository.existsByClientIdAndWorkspaceId(clientId, workspaceId);

    if (isExisted) {
      AdminUserListForWorkspaceAssignmentResponse response =
          workspaceRepository.findAssignedUserListForWorkspaceAssignment(clientId,
              workspaceId,
              type,
              Pattern.compile(".*" + URLDecoder.decode(query, StandardCharsets.UTF_8) + ".*"),
              (page - 1) * 5,
              5);

      return response == null
          ? AdminUserListForWorkspaceAssignmentResponse.builder().users(List.of()).build()
          : response;
    } else {
      throw BaseException.of(ResponseCode.WS_NOT_EXIST);
    }
  }

  public AdminUserListForWorkspaceAssignmentResponse getAvailableUserListForWorkspaceAssignment(
      ObjectId workspaceId, int page, String query) {
    String clientId = UserUtil.getClientId();
    boolean isExisted = workspaceRepository.existsByClientIdAndWorkspaceId(clientId, workspaceId);

    if (isExisted) {
      AdminUserListForWorkspaceAssignmentResponse response =
          userRepository.findAvailableUserListForWorkspaceAssignment(clientId,
              workspaceId,
              Pattern.compile(".*" + URLDecoder.decode(query, StandardCharsets.UTF_8) + ".*"),
              (page - 1) * 5,
              5);

      return response == null
          ? AdminUserListForWorkspaceAssignmentResponse.builder().users(List.of()).build()
          : response;
    } else {
      throw BaseException.of(ResponseCode.WS_NOT_EXIST);
    }
  }

  public AdminUserListResponse getUserList(
      int page,
      String filter,
      String query,
      LocalDate startDate,
      LocalDate endDate) {
    switch (filter) {
      case "FILTER_US_CATEGORY_ID":
        filter = "account";
        break;
      case "FILTER_US_CATEGORY_DESC":
        filter = "desc";
        break;
    }

    return userRepository.findUserList(UserUtil.getClientId(),
        filter,
        Pattern.compile(".*" + URLDecoder.decode(query, StandardCharsets.UTF_8) + ".*"),
        startDate,
        endDate,
        (page - 1) * 10,
        10);
  }

  public void initPassword(ObjectId userId) {
    UserEntity user = userRepository.findByClientIdAndUserId(UserUtil.getClientId(), userId);
    if (user == null) {
      throw BaseException.of(ResponseCode.USER_NOT_EXIST);
    }
    if (user.getIsAdmin()) {
      throw BaseException.of(ResponseCode.USER_FUNCTION_IMPOSSIBLE_ADMIN);
    }
    user.setPassword(CryptoUtil.encode(UserMeta.USER_DEFAULT_PW));
    user.setPwChangeYn(false);
    userRepository.save(user);
  }

  public void registerUser(AdminUserRegisterRequest request) {
    if (checkUserAccount(request.getAccount())) {
      throw BaseException.of(ResponseCode.USER_ALREADY_EXIST);
    }

    userRepository.save(UserEntity.builder()
        .userId(new ObjectId())
        .account(request.getAccount())
        .desc(request.getDesc())
        .clientId(UserUtil.getClientId())
        .password(CryptoUtil.encode(UserMeta.USER_DEFAULT_PW))
        .pwChangeYn(false)
        .isAdmin(false)
        .build());
  }
}

package ai.maum.data.edit.api.admin.workspace;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ai.maum.data.edit.api.admin.workspace.dto.request.AdminWorkspaceAssignRequest;
import ai.maum.data.edit.api.admin.workspace.dto.request.AdminWorkspaceDeleteRequest;
import ai.maum.data.edit.api.admin.workspace.dto.request.AdminWorkspaceRegisterRequest;
import ai.maum.data.edit.api.admin.workspace.dto.request.AdminWorkspaceUnassignRequest;
import ai.maum.data.edit.api.admin.workspace.dto.request.AdminWorkspaceUpdateRequest;
import ai.maum.data.edit.api.admin.workspace.dto.response.AdminWorkspaceLatestUploadResponse;
import ai.maum.data.edit.api.admin.workspace.dto.response.AdminWorkspaceListResponse;
import ai.maum.data.edit.api.admin.workspace.dto.response.AdminWorkspaceResponse;
import ai.maum.data.edit.api.admin.workspace.vo.AdminWorkspaceJoinedProjectsValue;
import ai.maum.data.edit.api.admin.workspace.vo.AdminWorkspaceWithProjectsValue;
import ai.maum.data.edit.api.uploadinfo.UploadInfoRepository;
import ai.maum.data.edit.api.user.UserEntity;
import ai.maum.data.edit.api.user.repository.UserRepository;
import ai.maum.data.edit.api.workspace.WorkspaceEntity;
import ai.maum.data.edit.api.workspace.field.AccessUsers;
import ai.maum.data.edit.api.workspace.field.Guides;
import ai.maum.data.edit.api.workspace.repository.WorkspaceRepository;
import ai.maum.data.edit.common.enums.ResponseCode;
import ai.maum.data.edit.common.enums.ResponseMeta;
import ai.maum.data.edit.common.exception.BaseException;
import ai.maum.data.edit.common.util.FileUtil;
import ai.maum.data.edit.common.util.UserUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminWorkspaceService {

  @Value("${service.file.path}")
  private String filePath;

  @Value("${service.file.url}")
  private String fileUrl;

  private final UserRepository userRepository;
  private final WorkspaceRepository workspaceRepository;
  private final UploadInfoRepository uploadInfoRepository;

  public void assignMember(ObjectId workspaceId, AdminWorkspaceAssignRequest request) {
    List<String> members = request.getUserIds();
    if (members == null || members.isEmpty()) {
      throw BaseException.of(ResponseMeta.PARAM_WRONG);
    }

    String clientId = UserUtil.getClientId();
    if (userRepository.findByClientIdAndUserIdIn(clientId, members
            .stream()
            .map(ObjectId::new)
            .collect(Collectors.toList()))
        .stream()
        .anyMatch(UserEntity::getIsAdmin)) {
      throw BaseException.of(ResponseCode.USER_FUNCTION_IMPOSSIBLE_ADMIN);
    }

    WorkspaceEntity workspace = workspaceRepository.findByClientIdAndWorkspaceId(clientId, workspaceId);
    if (workspace == null) {
      throw BaseException.of(ResponseCode.WS_NOT_EXIST);
    }

    List<AccessUsers> accessUsers = workspace.getAccessUsers();
    if (accessUsers == null) {
      accessUsers = new ArrayList<>();
      workspace.setAccessUsers(accessUsers);
    }

    userRepository.findByClientIdAndUserIdIn(clientId, members.stream()
            .map(ObjectId::new)
            .collect(Collectors.toList()))
        .stream()
        .map(user -> AccessUsers.builder()
            .userId(user.getUserId())
            .account(user.getAccount())
            .type(request.getType())
            .build())
        .collect(Collectors.toList())
        .forEach(user -> workspaceRepository.updateOneWithFilters(WorkspaceEntity.class,
            Map.of("client_id", clientId, "workspace_id", workspaceId),
            null,
            null,
            Map.of("access_users", user)));

    workspaceRepository.updateOneWithFilters(WorkspaceEntity.class,
        Map.of("client_id", clientId, "workspace_id", workspaceId),
        null,
        Map.of("updated", LocalDateTime.now()),
        null);
  }

  public Boolean checkWorkspaceName(String name) {
    return workspaceRepository.existsByClientIdAndWorkspaceNameAndIsDeleted(UserUtil.getClientId(), name, false);
  }

  public void deleteWorkspace(AdminWorkspaceDeleteRequest request) {
    List<AdminWorkspaceWithProjectsValue> workspaces =
        workspaceRepository.findWsWithPjsByClientIdAndWorkspaceIdIn(UserUtil.getClientId(),
            request.getWorkspaceIds().stream()
                .map(ObjectId::new)
                .collect(Collectors.toList()));

    if (workspaces.isEmpty()) {
      throw BaseException.of(ResponseCode.WS_NOT_EXIST);
    }

    int size = workspaces.size();

    workspaces = workspaces.stream()
        .filter(workspace -> {
          List<AdminWorkspaceWithProjectsValue.ProjectInfo> projects = workspace.getProjects();
          return projects == null
              || projects.isEmpty()
              || projects.stream().allMatch(AdminWorkspaceWithProjectsValue.ProjectInfo::isDeleted);
        })
        .collect(Collectors.toList());

    workspaces.forEach(workspace -> {
      FileUtil.deleteAll(filePath + "/" + "workspace" + "/" + workspace.getWorkspaceId());
      workspaceRepository.updateOneWithFilters(WorkspaceEntity.class,
          Map.of("workspace_id", workspace.getWorkspaceId()),
          null,
          Map.of("is_deleted", true),
          null);
    });

    if (size != workspaces.size()) {
      throw BaseException.of(ResponseCode.WS_HAS_PROJECT);
    }
  }

  public AdminWorkspaceLatestUploadResponse getLatestUploadInfo() {
    return uploadInfoRepository.findLatestUploadWorkspaceName(UserUtil.getUserId());
  }

  public AdminWorkspaceResponse getWorkspace(ObjectId workspaceId) {
    AdminWorkspaceResponse response = workspaceRepository.findWorkspace(UserUtil.getClientId(), workspaceId);
    if (response == null) {
      throw BaseException.of(ResponseCode.WS_NOT_EXIST);
    }
    return response;
  }

  public AdminWorkspaceListResponse getWorkspaceList(
      int page,
      String filter,
      String query,
      String status,
      LocalDate startDate,
      LocalDate endDate) {
    switch (filter) {
      case "FILTER_WS_CATEGORY_NAME":
        filter = "workspace_name";
        break;
      case "FILTER_WS_CATEGORY_DESC":
        filter = "workspace_intro";
        break;
      case "FILTER_WS_CATEGORY_MANAGER":
        filter = "incharge";
        break;
    }

    return workspaceRepository.findWorkspaceList(UserUtil.getClientId(),
        filter,
        Pattern.compile(".*" + URLDecoder.decode(query, StandardCharsets.UTF_8) + ".*"),
        status,
        startDate,
        endDate,
        false,
        (page - 1) * 10,
        10);
  }

  public ObjectId registerWorkspace(AdminWorkspaceRegisterRequest request) {
    ObjectId workspaceId = new ObjectId();
    String targetPath = "workspace" + "/" + workspaceId + "/" + "guide" + "/" + UUID.randomUUID();

    WorkspaceEntity workspace = WorkspaceEntity.builder()
        .workspaceId(workspaceId)
        .clientId(UserUtil.getClientId())
        .workspaceName(request.getName())
        .incharge(request.getManager())
        .startDate(request.getStart_date())
        .endDate(request.getEnd_date())
        .workspaceIntro(request.getDesc())
        .isDeleted(false)
        .build();

    if (request.getGuides() != null && !request.getGuides().isEmpty()) {
      workspace.setGuides(FileUtil.upload(filePath + "/" + targetPath, request.getGuides())
          .stream()
          .map(info -> Guides.builder()
              .guideId(new ObjectId())
              .guideFileName(info.getOrgName())
              .guideFilePath(info.getSavedPath())
              .guideFileUrl(fileUrl + "/" + targetPath + "/" + info.getSavedName())
              .guideFileSize(info.getSize())
              .build())
          .collect(Collectors.toList()));
    }

    workspaceRepository.save(workspace);

    return workspaceId;
  }

  public void unassignMember(ObjectId workspaceId, AdminWorkspaceUnassignRequest request) {
    List<String> members = request.getUserIds();
    if (members == null || members.isEmpty()) {
      throw BaseException.of(ResponseMeta.PARAM_WRONG);
    }

    String clientId = UserUtil.getClientId();
    WorkspaceEntity workspace = workspaceRepository.findByClientIdAndWorkspaceId(clientId, workspaceId);
    if (workspace == null) {
      throw BaseException.of(ResponseCode.WS_NOT_EXIST);
    }

    List<AccessUsers> accessUsers = workspace.getAccessUsers();
    if (accessUsers == null || accessUsers.isEmpty()) {
      return;
    }

    List<ObjectId> userIds = request.getUserIds().stream().map(ObjectId::new).collect(Collectors.toList());
    List<ObjectId> newUserIds = workspaceRepository.findAnyJoinedProjectsByUserIds(clientId, workspaceId, userIds)
        .stream()
        .filter(info -> !info.isResult())
        .map(AdminWorkspaceJoinedProjectsValue::getUserId)
        .collect(Collectors.toList());

    workspaceRepository.updateAccessUsersForUnassign(clientId, workspaceId, newUserIds);

    if (!newUserIds.isEmpty()) {
      workspaceRepository.updateOneWithFilters(WorkspaceEntity.class,
          Map.of("client_id", clientId, "workspace_id", workspaceId),
          null,
          Map.of("updated", LocalDateTime.now()),
          null);
    }

    if (userIds.size() != newUserIds.size()) {
      throw BaseException.of(ResponseCode.WS_UNASSIGN_IMPOSSIBLE);
    }
  }

  public void updateWorkspace(ObjectId workspaceId, AdminWorkspaceUpdateRequest request) {
    WorkspaceEntity workspace = workspaceRepository.findByClientIdAndWorkspaceId(UserUtil.getClientId(), workspaceId);
    if (workspace == null) {
      throw BaseException.of(ResponseCode.WS_NOT_EXIST);
    }

    workspace = workspace.toBuilder()
        .workspaceName(request.getName())
        .workspaceIntro(request.getDesc())
        .incharge(request.getManager())
        .startDate(request.getStart_date())
        .endDate(request.getEnd_date())
        .build();

    Map<String, Object> setInfo = new HashMap<>();
    setInfo.put("workspace_name", request.getName());
    setInfo.put("workspace_intro", request.getDesc());
    setInfo.put("incharge", request.getManager());
    setInfo.put("start_date", request.getStart_date());
    setInfo.put("end_date", request.getEnd_date());

    List<Guides> guides = workspace.getGuides();
    if (guides == null) {
      guides = new ArrayList<>();
    }
    List<String> deletedGuides = request.getDeleted_guides();
    List<MultipartFile> newGuides = request.getNew_guides();

    if (deletedGuides != null && !deletedGuides.isEmpty()) {
      List<Guides> targets = guides.stream()
          .filter(guide -> deletedGuides.contains(guide.getGuideId().toString()))
          .collect(Collectors.toList());
      FileUtil.deleteWithParentDirectory(targets.stream()
          .map(Guides::getGuideFilePath)
          .collect(Collectors.toList()));
      guides.removeAll(targets);
    }

    if (newGuides != null && !newGuides.isEmpty()) {
      String targetPath = "workspace" + "/" + workspaceId + "/" + "guide" + "/" + UUID.randomUUID();
      guides.addAll(FileUtil.upload(filePath + "/" + targetPath, newGuides)
          .stream()
          .map(info -> Guides.builder()
              .guideId(new ObjectId())
              .guideFileName(info.getOrgName())
              .guideFilePath(info.getSavedPath())
              .guideFileUrl(fileUrl + "/" + targetPath + "/" + info.getSavedName())
              .guideFileSize(info.getSize())
              .build())
          .collect(Collectors.toList()));
    }

    setInfo.put("guides", guides);

    workspaceRepository.updateOneWithFilters(WorkspaceEntity.class,
        Map.of("workspace_id", workspaceId),
        null,
        setInfo,
        null);
  }
}

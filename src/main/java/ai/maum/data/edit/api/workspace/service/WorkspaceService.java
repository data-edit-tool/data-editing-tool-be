package ai.maum.data.edit.api.workspace.service;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.maum.data.edit.api.project.ProjectService;
import ai.maum.data.edit.api.project.repository.ProjectRepository;
import ai.maum.data.edit.api.user.dto.response.UserTypeResponse;
import ai.maum.data.edit.api.user.enums.UserType;
import ai.maum.data.edit.api.workspace.WorkspaceEntity;
import ai.maum.data.edit.api.workspace.dto.response.GuideResponse;
import ai.maum.data.edit.api.workspace.dto.response.ProjectListResponse;
import ai.maum.data.edit.api.workspace.dto.response.WorkspaceListResponse;
import ai.maum.data.edit.api.workspace.dto.response.WorkspaceResponse;
import ai.maum.data.edit.api.workspace.repository.WorkspaceRepository;
import ai.maum.data.edit.common.config.security.UserInfo;
import ai.maum.data.edit.common.enums.ResponseCode;
import ai.maum.data.edit.common.exception.BaseException;
import ai.maum.data.edit.common.util.FileUtil;
import ai.maum.data.edit.common.util.UserUtil;
import ai.maum.data.edit.common.util.ZipUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

  @Value("${service.file.path}")
  private String filePath;

  @Value("${service.file.url}")
  private String fileUrl;

  private final WorkspaceRepository workspaceRepository;
  private final ProjectRepository projectRepository;
  private final ProjectService projectService;

  public String extract(ObjectId workspaceId, List<String> request) {
    WorkspaceEntity workspace = workspaceRepository.findByClientIdAndWorkspaceId(UserUtil.getClientId(), workspaceId);

    if (workspace == null) {
      throw BaseException.of(ResponseCode.WS_NOT_EXIST);
    }

    List<String> targets = new ArrayList<>();

    projectRepository.findProjectIdsByWorkspaceId(workspaceId)
        .forEach(projectId -> {
          try {
            targets.add(projectService.extract(workspaceId, projectId, request, true));
          } catch (BaseException ignored) {
          }
        });

    String fileName = workspace.getWorkspaceId()
        + "_"
        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
        + ".zip";
    String targetPath = "workspace"
        + "/"
        + workspaceId;

    ZipUtil.zip(targets, FileUtil.getPathWithEncoding(filePath + "/" + targetPath, fileName).toString(), false);

    return fileUrl + "/" + targetPath + "/" + fileName;
  }

  public GuideResponse findGuidesList(ObjectId workspaceId) {
    Map<String, Object> where = new HashMap<>();
    where.put("workspace_id", workspaceId);

    List<String> fields = new ArrayList<>();
    fields.add("guides");
    return workspaceRepository.findOneWithFieldsBy(GuideResponse.class, where, fields);
  }

  public ProjectListResponse findWorkspace(ObjectId workspaceId) {
    UserInfo user = UserUtil.getUser();
    ProjectListResponse result = projectRepository.getProjects(workspaceId, user.getType(), user.getUserId());

    result.setWorkspaceName(
        workspaceRepository.findOneBy(
            WorkspaceResponse.class
            , Map.of(
                "workspace_id", workspaceId
                , "is_deleted", false
            )
        ).getWorkspaceName()
    );

    // 뷰어 여부 정보 세팅
    UserTypeResponse userType = workspaceRepository.getAccessUserType(workspaceId, user.getUserId());
    result.setIsViewer(userType.getType().equals(UserType.VIEWER.getCode()));

    return result;
  }

  public List<WorkspaceListResponse> workspaceList(String workspaceName, String workspaceIntro,
      String incharge, String startDate, String endDate) {
    UserInfo user = UserUtil.getUser();
    return workspaceRepository.findWorkspaceEntityBy(user.getClientId(), user.getUserId(),
        workspaceName, workspaceIntro, incharge, startDate, endDate);
  }
}

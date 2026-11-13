package ai.maum.data.edit.common.filter;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ai.maum.data.edit.api.project.repository.ProjectRepository;
import ai.maum.data.edit.api.user.dto.response.UserTypeResponse;
import ai.maum.data.edit.api.user.enums.UserType;
import ai.maum.data.edit.api.workspace.field.project.Project;
import ai.maum.data.edit.api.workspace.repository.WorkspaceRepository;
import ai.maum.data.edit.common.config.security.UserInfo;
import ai.maum.data.edit.common.enums.ResponseCode;
import ai.maum.data.edit.common.util.ParsingUtil;
import ai.maum.data.edit.common.util.StringUtil;
import ai.maum.data.edit.common.wrapper.RereadableRequestWrapper;

public class AccessAuthorityFilter extends OncePerRequestFilter {
  private final WorkspaceRepository workspaceRepository;
  private final ProjectRepository projectRepository;

  public AccessAuthorityFilter(WorkspaceRepository workspaceRepository, ProjectRepository projectRepository) {
    this.workspaceRepository = workspaceRepository;
    this.projectRepository = projectRepository;
  }

  @Override
  public void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response,
      @NotNull FilterChain filterChain) throws IOException, ServletException {
    RereadableRequestWrapper rereadableRequestWrapper = new RereadableRequestWrapper((HttpServletRequest) request);
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    UserInfo userInfo = ((UserInfo) authentication.getPrincipal());
    String method = request.getMethod();

    // workspaceId 얻기
    String workspaceId = null;
    if (method.equals("GET")) {
      workspaceId = request.getParameter("workspace_id");
      if (workspaceId == null) {
        String projectId = StringUtil.valueOf(request.getParameter("project_id"));
        String jobId = StringUtil.valueOf(request.getParameter("job_id"));
        String workId = StringUtil.valueOf(request.getParameter("work_id"));
        String fileId = StringUtil.valueOf(request.getParameter("file_id"));
        workspaceId = getWorkspaceId(projectId, jobId, workId, fileId);
      }
    } else {
      workspaceId = getParameterOnBody(rereadableRequestWrapper, "workspace_id");
      if (workspaceId == null) {
        String projectId = getParameterOnBody(rereadableRequestWrapper, "project_id");
        String jobId = getParameterOnBody(rereadableRequestWrapper, "job_id");
        String workId = getParameterOnBody(rereadableRequestWrapper, "work_id");
        workspaceId = getWorkspaceId(projectId, jobId, workId, "");
      }
    }

    // userInfo setType 하기
    UserTypeResponse type =
        workspaceRepository.getAccessUserType(new ObjectId(workspaceId), new ObjectId(userInfo.getUsername()));
    if (type != null) {
      userInfo.setType(UserType.fromCode(type.getType()));
      filterChain.doFilter(rereadableRequestWrapper, response);
    } else {
      throw new AccessAuthorityException(ResponseCode.ACCESS_DENIED.getMessage());
    }
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
    List<String> antMatchers = new ArrayList<>();
    antMatchers.add("/workspace/find");
    antMatchers.add("/project/(?!extract).*");
    antMatchers.add("/job/.*");
    antMatchers.add("/work/.*");

    return !StringUtil.matches(request.getRequestURI(), antMatchers);
  }

  private String getParameterOnBody(RereadableRequestWrapper rereadableRequestWrapper, String parameter)
      throws IOException {
    return StringUtil.valueOf(ParsingUtil.getBody(rereadableRequestWrapper).get(parameter));
  }

  private String getWorkspaceId(String projectId, String jobId, String workId, String fileId) {
    if (StringUtils.isNotBlank(projectId)) {
      Project project = projectRepository.getWorkspaceIdByProjectId(new ObjectId(projectId));
      return project.getWorkspaceId().toString();
    } else if (StringUtils.isNotBlank(jobId)) {
      Project project = projectRepository.getWorkspaceIdByJobId(new ObjectId(jobId));
      return project.getWorkspaceId().toString();
    } else if (StringUtils.isNotBlank(workId)) {
      Project project = projectRepository.getWorkspaceIdByWorkId(new ObjectId(workId));
      return project.getWorkspaceId().toString();
    } else if (StringUtils.isNotBlank(fileId)) {
      Project project = projectRepository.getWorkspaceIdByFileId(new ObjectId(fileId));
      return project.getWorkspaceId().toString();
    } else {
      return "";
    }
  }
}

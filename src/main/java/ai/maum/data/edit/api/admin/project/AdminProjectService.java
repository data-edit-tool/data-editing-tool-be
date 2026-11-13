package ai.maum.data.edit.api.admin.project;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.jcodec.common.DemuxerTrackMeta;
import org.jcodec.common.io.ByteBufferSeekableByteChannel;
import org.jcodec.containers.mp4.demuxer.MP4Demuxer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import ai.maum.data.edit.api.admin.project.dto.request.AdminProjectDeleteRequest;
import ai.maum.data.edit.api.admin.project.dto.request.AdminProjectRegisterRequest;
import ai.maum.data.edit.api.admin.project.dto.request.AdminProjectReuploadRequest;
import ai.maum.data.edit.api.admin.project.dto.response.AdminProjectFileListResponse;
import ai.maum.data.edit.api.admin.project.dto.response.AdminProjectListResponse;
import ai.maum.data.edit.api.admin.project.vo.EngineForUploadValue;
import ai.maum.data.edit.api.admin.project.vo.JobFileForProjectFileUploadValue;
import ai.maum.data.edit.api.admin.project.vo.SpeakerForUploadValue;
import ai.maum.data.edit.api.admin.project.vo.UploadResultNotSuccessValue;
import ai.maum.data.edit.api.admin.workspace.dto.request.AdminWorkspaceUpdatePostStatusRequest;
import ai.maum.data.edit.api.admin.workspace.vo.AdminWorkspaceWithProjectsValue;
import ai.maum.data.edit.api.engine.EngineEntity;
import ai.maum.data.edit.api.engine.EngineRepository;
import ai.maum.data.edit.api.project.repository.ProjectRepository;
import ai.maum.data.edit.api.uploadinfo.UploadInfoEntity;
import ai.maum.data.edit.api.uploadinfo.UploadInfoRepository;
import ai.maum.data.edit.api.workspace.WorkspaceEntity;
import ai.maum.data.edit.api.workspace.field.job.Job;
import ai.maum.data.edit.api.workspace.field.job.JobFile;
import ai.maum.data.edit.api.workspace.field.project.ComplexConversation;
import ai.maum.data.edit.api.workspace.field.project.Project;
import ai.maum.data.edit.api.workspace.field.project.enums.ProjectStatus;
import ai.maum.data.edit.api.workspace.repository.WorkspaceRepository;
import ai.maum.data.edit.common.config.security.UserInfo;
import ai.maum.data.edit.common.enums.FileMeta;
import ai.maum.data.edit.common.enums.FileStatus;
import ai.maum.data.edit.common.enums.ResponseCode;
import ai.maum.data.edit.common.enums.ToolTypeMeta;
import ai.maum.data.edit.common.enums.UploadStatus;
import ai.maum.data.edit.common.exception.BaseException;
import ai.maum.data.edit.common.util.DateUtil;
import ai.maum.data.edit.common.util.FileUtil;
import ai.maum.data.edit.common.util.LogUtil;
import ai.maum.data.edit.common.util.ThreadUtil;
import ai.maum.data.edit.common.util.UserUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminProjectService {

  private final WorkspaceRepository workspaceRepository;
  private final ProjectRepository projectRepository;
  private final EngineRepository engineRepository;
  private final UploadInfoRepository uploadInfoRepository;

  @Value("${service.file.path}")
  private String filePath;
  @Value("${service.file.url}")
  private String fileUrl;

  public void deleteProject(ObjectId workspaceId, AdminProjectDeleteRequest request) {
    AdminWorkspaceWithProjectsValue workspace =
        workspaceRepository.findWsWithPjByClientIdAndWorkspaceId(UserUtil.getClientId(), workspaceId);

    if (workspace == null) {
      throw BaseException.of(ResponseCode.WS_NOT_EXIST);
    }

    List<AdminWorkspaceWithProjectsValue.ProjectInfo> projects = workspace.getProjects();

    if (projects == null || projects.isEmpty()) {
      throw BaseException.of(ResponseCode.PJ_NOT_EXIST);
    }

    int size = request.getProjectIds().size();
    int count = 0;

    for (String target : request.getProjectIds()) {
      for (AdminWorkspaceWithProjectsValue.ProjectInfo project : projects) {
        ObjectId projectId = project.getProjectId();

        if (target.equals(projectId.toString())) {
          if (!project.isDeleted() && project.getPostStatus().equals(ProjectStatus.POST_OFF.getCode())) {
            FileUtil.deleteAll(filePath + "/" + "workspace" + "/" + workspaceId + "/" + "project" + "/" + projectId);
            projectRepository.updateOneWithFilters(Project.class,
                Map.of("project_id", projectId),
                null,
                Map.of("is_deleted", true),
                null);
            count++;
          }
          break;
        }
      }

      if (count == size) {
        break;
      }
    }

    if (size != count) {
      throw BaseException.of(ResponseCode.PJ_DELETE_IMPOSSIBLE);
    }
  }

  public AdminProjectFileListResponse getFileList(ObjectId workspaceId, ObjectId projectId, int page) {
    return projectRepository.findFileList(UserUtil.getClientId(), workspaceId, projectId, (page - 1) * 10, 10);
  }

  public AdminProjectListResponse getProjectList(
      ObjectId workspaceId,
      int page,
      String toolType,
      String uploadStatus,
      String projectStatus,
      String postStatus,
      String query,
      LocalDate startDate,
      LocalDate endDate) {
    String clientId = UserUtil.getClientId();
    WorkspaceEntity workspace = workspaceRepository.findByClientIdAndWorkspaceId(clientId, workspaceId);
    if (workspace == null) {
      throw BaseException.of(ResponseCode.WS_NOT_EXIST);
    }

    switch (toolType) {
      case "FILTER_PJ_TOOL_TYPE_ALL":
        toolType = null;
        break;
      case "FILTER_PJ_TOOL_TYPE_CC":
        toolType = ToolTypeMeta.T_TYPE_CC.getToolCode();
        break;
    }

    switch (uploadStatus) {
      case "FILTER_PJ_UPLOAD_STATUS_ALL":
        uploadStatus = null;
        break;
      case "FILTER_PJ_UPLOAD_STATUS_SUCCESS":
        uploadStatus = UploadStatus.SUCCESS.getCode();
        break;
      case "FILTER_PJ_UPLOAD_STATUS_PROGRESS":
        uploadStatus = UploadStatus.PROGRESS.getCode();
        break;
      case "FILTER_PJ_UPLOAD_STATUS_ERROR":
        uploadStatus = UploadStatus.ERROR.getCode();
        break;
    }

    switch (projectStatus) {
      case "FILTER_PJ_PROJECT_STATUS_ALL":
        projectStatus = null;
        break;
      case "FILTER_PJ_PROJECT_STATUS_PENDING":
        projectStatus = ProjectStatus.PENDING.getCode();
        break;
      case "FILTER_PJ_PROJECT_STATUS_WORKING":
        projectStatus = ProjectStatus.WORKING.getCode();
        break;
      case "FILTER_PJ_PROJECT_STATUS_FINISH":
        projectStatus = ProjectStatus.FINISH.getCode();
        break;
    }

    switch (postStatus) {
      case "FILTER_PJ_POST_STATUS_ALL":
        postStatus = null;
        break;
      case "FILTER_PJ_POST_STATUS_ON":
        postStatus = ProjectStatus.POST_ON.getCode();
        break;
      case "FILTER_PJ_POST_STATUS_OFF":
        postStatus = ProjectStatus.POST_OFF.getCode();
        break;
    }

    return projectRepository.findProjectList(clientId,
        workspaceId,
        toolType,
        uploadStatus,
        projectStatus,
        postStatus,
        Pattern.compile(".*" + URLDecoder.decode(query, StandardCharsets.UTF_8) + ".*"),
        startDate,
        endDate,
        false,
        (page - 1) * 10,
        10);
  }

  public ObjectId registerProject(ObjectId workspaceId, AdminProjectRegisterRequest request) {
    List<AdminProjectRegisterRequest.Subset> subsets = request.getSubsets();
    List<AdminProjectRegisterRequest.SpeakerFile> speakerFiles = request.getSpeakerFiles();

    if (subsets == null || subsets.isEmpty()) {
      throw BaseException.of(ResponseCode.PJ_DATA_FILE_NOT_EXIST);
    }
    if (speakerFiles == null || speakerFiles.isEmpty()) {
      throw BaseException.of(ResponseCode.PJ_SPEAKER_FILE_NOT_EXIST);
    }

    WorkspaceEntity workspace =
        workspaceRepository.findOneBy(WorkspaceEntity.class, Map.of("workspace_id", workspaceId));

    if (workspace == null) {
      throw BaseException.of(ResponseCode.WS_NOT_EXIST);
    }

    String[] nameInfo = request.getName().split("_");

    if (nameInfo.length != 2) {
      throw BaseException.of(ResponseCode.PJ_NAME_WRONG);
    }

    Map<String, Map<String, Object>> speakerInfos = new HashMap<>();
    String relInfo = null;

    for (AdminProjectRegisterRequest.SpeakerFile speakerFile : speakerFiles) {
      String fileName = speakerFile.getFileName();
      String[] fileNameInfo = fileName.split("_");

      /**
       * 파일명(fileNameInfo) 예제 : SPEAKER_[speakerId]_[gender]_[age]_[relationship]_[role].jpg
       * role이 없는 경우 : SPEAKER_001_M_30_FRIEND.jpg
       * role이 있는 경우 : SPEAKER_001_M_30_FRIEND_USER.jpg
       */
      if (fileNameInfo.length != 5 && fileNameInfo.length != 6) {
        throw BaseException.of(ResponseCode.PJ_SPEAKER_WRONG);
      }
      if (relInfo == null) {
        if (fileNameInfo.length == 5) { // role이 없을 경우
          relInfo = fileNameInfo[4].substring(0, fileNameInfo[4].indexOf(".")); // 확장자 자르기
        } else if (fileNameInfo.length == 6) { // role이 있을 경우
          relInfo = fileNameInfo[4];
        }
      }
      speakerInfos.put(fileName, Map.of("fileNameInfo", fileNameInfo, "fileSize", speakerFile.getFileSize()));
    }

    Set<Map.Entry<String, Map<String, Object>>> speakerInfosEntry = speakerInfos.entrySet();
    List<Project> projects = projectRepository.findAllByObjectId(Project.class, "workspace_id", workspaceId.toString());

    if (!projects.isEmpty()) {
      projects = projects.stream()
          .filter(project -> !project.isDeleted())
          .collect(Collectors.toList());

      for (Project project : projects) {
        if (project.getProjectName().equals(request.getName())) {
          throw BaseException.of(ResponseCode.PJ_ALREADY_EXIST);
        }
      }
    }

    ObjectId projectId = new ObjectId();
    Map<EngineEntity, List<EngineEntity.Stt>> engineInfos = new HashMap<>();

    ComplexConversation project = ComplexConversation.builder()
        .projectId(projectId)
        .workspaceId(workspaceId)
        .projectName(request.getName())
        .topic(nameInfo[1])
        .postStatus(ProjectStatus.POST_OFF.getCode())
        .dataType(Project.Type.builder()
            .code(request.getDataType().getCode())
            .name(request.getDataType().getName())
            .build())
        .toolType(Project.Type.builder()
            .code(request.getToolType().getCode())
            .name(request.getToolType().getName())
            .build())
        .clientCtgId(new ObjectId(request.getCategoryType()))
        .jobs(request.getSubsets().stream()
            .map(subset -> {
              ObjectId jobId = new ObjectId();

              EngineEntity engine = EngineEntity.builder()
                  .engineId(new ObjectId())
                  .jobId(jobId)
                  .build();

              List<EngineEntity.Stt> stts = new ArrayList<>();

              engineInfos.put(engine, stts);

              Job job = Job.builder()
                  .jobId(jobId)
                  .jobName(subset.getName())
                  .jobFiles(subset.getFiles().stream()
                      .map(file -> {
                        String fileName = file.getFileName();
                        long fileSize = file.getFileSize();

                        if (fileName.contains("STT8K")) {
                          String speakerInfo = fileName.split("_")[4];
                          stts.add(EngineEntity.Stt.builder()
                              .sttId(new ObjectId())
                              .speakerId(speakerInfo.substring(0, speakerInfo.lastIndexOf(".")))
                              .fileName(fileName)
                              .fileStatus(FileStatus.PENDING.getCode())
                              .fileSize(fileSize)
                              .build());
                          return null;
                        }

                        String fileType = FileUtil.getFileType(fileName);

                        JobFile jobFile = JobFile.builder()
                            .fileId(new ObjectId())
                            .fileName(fileName)
                            .fileType(fileType)
                            .fileExt(FileUtil.getFileExtension(fileName, false))
                            .fileStatus(FileStatus.PENDING.getCode())
                            .fileSize(fileSize)
                            .build();

                        if (fileType != null && fileType.equals(FileMeta.TYPE_VIDEO.getCode())) {
                          String[] fileNameInfo = fileName.split("_");
                          if (fileNameInfo.length != 8) {
                            throw BaseException.of(ResponseCode.PJ_FILE_NAME_WRONG);
                          }
                          jobFile.setFileInfo(JobFile.FileInfo.builder()
                              .angle(fileNameInfo[7].substring(0, fileNameInfo[7].indexOf(".")))
                              .build());
                        }

                        return jobFile;
                      })
                      .filter(Objects::nonNull)
                      .collect(Collectors.toList()))
                  .build();

              if (stts.isEmpty()) {
                throw BaseException.of(ResponseCode.PJ_STT_FILE_NOT_EXIST);
              }

              return job;
            })
            .collect(Collectors.toList()))
        .speakers(speakerInfosEntry
            .stream()
            .map(speakerInfo -> {
              String[] fileNameInfo = (String[]) speakerInfo.getValue().get("fileNameInfo");
              return ComplexConversation.Speaker.builder()
                  .speakerId(fileNameInfo[1])
                  .gender(fileNameInfo[2])
                  .age(Integer.parseInt(fileNameInfo[3]))
                  .role(fileNameInfo.length > 5 ? fileNameInfo[5].substring(0, fileNameInfo[5].indexOf(".")) : "")
                  .thumbnailFileName(speakerInfo.getKey())
                  .thumbnailFileStatus(FileStatus.PENDING.getCode())
                  .thumbnailFileSize((long) speakerInfo.getValue().get("fileSize"))
                  .build();
            })
            .collect(Collectors.toList()))
        .relationship(relInfo)
        .created(LocalDateTime.now())
        .updated(LocalDateTime.now())
        .build();

    engineRepository.saveAll(engineInfos.entrySet()
        .stream()
        .map(engineInfo -> engineInfo.getKey().toBuilder().stt(engineInfo.getValue()).build())
        .collect(Collectors.toList()));

    projectRepository.save(project);

    return projectId;
  }

  public void reupload(ObjectId workspaceId, ObjectId projectId, AdminProjectReuploadRequest request) {
    UserInfo user = UserUtil.getUser();
    String clientId = user.getClientId();

    AdminWorkspaceWithProjectsValue workspace =
        workspaceRepository.findWsWithPjByClientIdAndWorkspaceId(clientId, workspaceId);

    if (workspace == null) {
      throw BaseException.of(ResponseCode.WS_NOT_EXIST);
    }

    List<AdminWorkspaceWithProjectsValue.ProjectInfo> projects = workspace.getProjects();

    if (projects == null
        || projects.isEmpty()
        || projects.stream().noneMatch(project -> project.getProjectId().toString().equals(projectId.toString()))) {
      throw BaseException.of(ResponseCode.PJ_NOT_EXIST);
    }

    MultipartFile file = request.getFile();
    String fileName = file.getOriginalFilename();
    String fileType = FileUtil.getFileType(fileName);

    if (fileType == null) {
      throw BaseException.of(ResponseCode.PJ_FILE_TYPE_WRONG);
    }

    Map<String, Object> setInfo = new HashMap<>();

    SpeakerForUploadValue speakerFileInfo = null;
    JobFileForProjectFileUploadValue jobFileInfo = null;
    EngineForUploadValue sttInfo = null;

    String fileTypeInfo = request.getFileType();
    String targetPath = "workspace"
        + "/"
        + workspaceId
        + "/"
        + "project"
        + "/"
        + projectId;

    switch (fileTypeInfo) {
      case "SPEAKER":
        String speakerId = request.getFileId();

        speakerFileInfo = projectRepository.findSpeakerForReUpload(clientId,
            workspaceId,
            projectId,
            speakerId,
            fileName);

        if (speakerFileInfo == null || speakerFileInfo.getSpeakerId() == null) {
          throw BaseException.of(ResponseCode.PJ_REUPLOAD_IMPOSSIBLE);
        }

        setInfo.put("speakers.$[speaker].thumbnail_file_path", null);
        setInfo.put("speakers.$[speaker].thumbnail_file_url", null);
        setInfo.put("speakers.$[speaker].thumbnail_file_status", FileStatus.PROGRESS.getCode());

        projectRepository.updateOneWithFilters(Project.class,
            Map.of("project_id", projectId),
            Map.of("speaker.speaker_id", speakerId),
            setInfo,
            null);

        targetPath += ("/" + "speaker");
        break;
      case "JOB":
        ObjectId jobFileId = new ObjectId(request.getFileId());

        jobFileInfo = projectRepository.findJobFileForReUpload(clientId,
            workspaceId,
            projectId,
            jobFileId,
            fileName);

        if (jobFileInfo == null) {
          throw BaseException.of(ResponseCode.PJ_REUPLOAD_IMPOSSIBLE);
        }

        setInfo.put("jobs.$[].job_files.$[jf].file_path", null);
        setInfo.put("jobs.$[].job_files.$[jf].file_url", null);
        setInfo.put("jobs.$[].job_files.$[jf].file_info", null);
        setInfo.put("jobs.$[].job_files.$[jf].file_status", FileStatus.PROGRESS.getCode());

        projectRepository.updateOneWithFilters(Project.class,
            Map.of("project_id", projectId),
            Map.of("jf.file_id", jobFileId),
            setInfo,
            null);

        targetPath += ("/" + "job" + "/" + jobFileInfo.getJobId());
        break;
      case "STT":
        ObjectId sttFileId = new ObjectId(request.getFileId());

        sttInfo = engineRepository.findEngineForReUpload(sttFileId, fileName);

        if (sttInfo == null || sttInfo.getEngineId() == null || sttInfo.getSttId() == null) {
          throw BaseException.of(ResponseCode.PJ_REUPLOAD_IMPOSSIBLE);
        }

        setInfo.put("stt.$[info].data", null);
        setInfo.put("stt.$[info].file_status", FileStatus.PROGRESS.getCode());

        engineRepository.updateOneWithFilters(EngineEntity.class,
            Map.of("stt.stt_id", sttFileId),
            Map.of("info.stt_id", sttFileId),
            setInfo,
            null);
        break;
    }

    try {
      LogUtil.info("재업로드 시작: project_id(" + projectId + "), file(" + fileName + ")");

      if (fileTypeInfo.equals("SPEAKER")) {
        FileUtil.delete(speakerFileInfo.getThumbnailFilePath());
      } else if (fileTypeInfo.equals("JOB")) {
        FileUtil.delete(jobFileInfo.getJobFile().getFilePath());
      }

      FileUtil.FileInfo fileUploadInfo = FileUtil.upload(filePath + "/" + targetPath, file);

      switch (fileTypeInfo) {
        case "SPEAKER":
          projectRepository.updateOneWithFilters(Project.class,
              Map.of("project_id", projectId),
              Map.of("speaker.speaker_id", request.getFileId()),
              Map.of("speakers.$[speaker].thumbnail_file_path", fileUploadInfo.getSavedPath(),
                  "speakers.$[speaker].thumbnail_file_url",
                  fileUrl + "/" + targetPath + "/" + fileUploadInfo.getSavedName(),
                  "speakers.$[speaker].thumbnail_file_status", FileStatus.SUCCESS.getCode()),
              null);
          break;
        case "JOB":
          String fileExt = FileUtil.getFileExtension(fileName, false);
          setInfo.clear();

          if (fileType.equals(FileMeta.TYPE_VIDEO.getCode())) {
            DemuxerTrackMeta videoInfo = MP4Demuxer.createMP4Demuxer(new ByteBufferSeekableByteChannel(ByteBuffer
                    .wrap(file.getBytes()), (int) file.getSize()))
                .getVideoTrack()
                .getMeta();
            double duration = Math.ceil(videoInfo.getTotalDuration() * 10) / 10;
            setInfo.put("jobs.$[].job_files.$[jf].file_info", JobFile.FileInfo.builder()
                .duration(DateUtil.convertToStringBySecond(duration))
                .format(fileExt)
                .fps((int) Math.ceil(videoInfo.getTotalFrames() / duration))
                .width(videoInfo.getVideoCodecMeta().getSize().getWidth())
                .height(videoInfo.getVideoCodecMeta().getSize().getHeight())
                .angle(jobFileInfo.getJobFile().getFileInfo().getAngle())
                .build());
          } else if (fileType.equals(FileMeta.TYPE_AUDIO.getCode())) {
            AudioInputStream is = AudioSystem.getAudioInputStream(new ByteArrayInputStream(file.getBytes()));
            AudioFormat audioInfo = is.getFormat();
            int channel = audioInfo.getChannels();
            float sampleRate = audioInfo.getSampleRate();
            setInfo.put("jobs.$[].job_files.$[jf].file_info", JobFile.FileInfo.builder()
                .duration(DateUtil.convertToStringBySecond(is.getFrameLength() / sampleRate))
                .format(fileExt)
                .sampleRate(((int) Math.ceil(sampleRate) / 1000) + "kHz")
                .channel(channel == 1 ? "mono" : (channel == 2 ? "stereo" : "other"))
                .build());
          }

          setInfo.put("jobs.$[].job_files.$[jf].file_path", fileUploadInfo.getSavedPath());
          setInfo.put("jobs.$[].job_files.$[jf].file_url",
              fileUrl + "/" + targetPath + "/" + fileUploadInfo.getSavedName());
          setInfo.put("jobs.$[].job_files.$[jf].file_status", FileStatus.SUCCESS.getCode());

          projectRepository.updateOneWithFilters(Project.class,
              Map.of("project_id", projectId),
              Map.of("jf.file_name", jobFileInfo.getJobFile().getFileName()),
              setInfo,
              null);
          break;
        case "STT":
          engineRepository.updateOneWithFilters(EngineEntity.class,
              Map.of("engine_id", sttInfo.getEngineId()),
              Map.of("info.stt_id", sttInfo.getSttId()),
              Map.of("stt.$[info].file_status", FileStatus.SUCCESS.getCode()),
              null);
          break;
      }
    } catch (IOException | UnsupportedAudioFileException | BaseException e) {
      switch (fileTypeInfo) {
        case "SPEAKER":
          projectRepository.updateOneWithFilters(Project.class,
              Map.of("project_id", projectId),
              Map.of("speaker.speaker_id", request.getFileId()),
              Map.of("speakers.$[speaker].thumbnail_file_status", FileStatus.ERROR.getCode()),
              null);
          break;
        case "JOB":
          projectRepository.updateOneWithFilters(Project.class,
              Map.of("project_id", projectId),
              Map.of("jf.file_name", jobFileInfo.getJobFile().getFileName()),
              Map.of("jobs.$[].job_files.$[jf].file_status", FileStatus.ERROR.getCode()),
              null);
          break;
        case "STT":
          engineRepository.updateOneWithFilters(EngineEntity.class,
              Map.of("engine_id", sttInfo.getEngineId()),
              Map.of("info.stt_id", sttInfo.getSttId()),
              Map.of("stt.$[info].file_status", FileStatus.ERROR.getCode()),
              null);
          break;
      }

      LogUtil.error("재업로드 실패: project_id(" + projectId + "), file(" + fileName + "), error(" + e + ")");
      return;
    }

    checkUploadAndUpdatePost(user.getUserId(), clientId, workspaceId, projectId);

    LogUtil.info("재업로드 완료: project_id(" + projectId + "), file(" + fileName + ")");
  }

  public void updatePostStatus(ObjectId workspaceId, AdminWorkspaceUpdatePostStatusRequest request) {
    String clientId = UserUtil.getClientId();
    AdminWorkspaceWithProjectsValue workspace =
        workspaceRepository.findWsWithPjByClientIdAndWorkspaceId(clientId, workspaceId);

    if (workspace == null) {
      throw BaseException.of(ResponseCode.WS_NOT_EXIST);
    }

    List<AdminWorkspaceWithProjectsValue.ProjectInfo> projects = workspace.getProjects();

    if (projects == null || projects.isEmpty()) {
      throw BaseException.of(ResponseCode.PJ_NOT_EXIST);
    }

    int size = request.getProjectIds().size();
    int count = 0;

    for (String target : request.getProjectIds()) {
      for (AdminWorkspaceWithProjectsValue.ProjectInfo project : projects) {
        ObjectId projectId = project.getProjectId();

        if (target.equals(projectId.toString())) {
          UploadResultNotSuccessValue info =
              projectRepository.findUploadResultNotSuccess(clientId, workspaceId, projectId);

          if (info.getJobUploadResult() == 0 && info.getSpeakerUploadResult() == 0) {
            projectRepository.updateOneWithFilters(Project.class,
                Map.of("project_id", projectId),
                null,
                Map.of("post_status", request.getPostStatus()),
                null
            );
            count++;
          }
          break;
        }
      }

      if (count == size) {
        break;
      }
    }

    if (size != count) {
      throw BaseException.of(ResponseCode.PJ_POST_STATUS_UPDATE_IMPOSSIBLE);
    }
  }

  public void uploadFile(ObjectId workspaceId, ObjectId projectId, MultipartFile file) {
    UserInfo user = UserUtil.getUser();
    ObjectId userId = user.getUserId();
    String clientId = user.getClientId();

    AdminWorkspaceWithProjectsValue workspace =
        workspaceRepository.findWsWithPjByClientIdAndWorkspaceId(clientId, workspaceId);

    if (workspace == null) {
      throw BaseException.of(ResponseCode.WS_NOT_EXIST);
    }

    List<AdminWorkspaceWithProjectsValue.ProjectInfo> projects = workspace.getProjects();

    if (projects == null
        || projects.isEmpty()
        || projects.stream().noneMatch(project -> project.getProjectId().toString().equals(projectId.toString()))) {
      throw BaseException.of(ResponseCode.PJ_NOT_EXIST);
    }

    if (file == null || file.isEmpty() || StringUtils.isBlank(file.getOriginalFilename())) {
      throw BaseException.of(ResponseCode.PJ_FILE_WRONG);
    }

    String fileName = Paths.get(file.getOriginalFilename()).getFileName().toString();

    String fileType = FileUtil.getFileType(fileName);
    if (fileType == null) {
      throw BaseException.of(ResponseCode.PJ_FILE_TYPE_WRONG);
    }

    try {
      MultipartFile target = new FileUtil.MultipartFileAdapter(file.getName(),
          fileName,
          file.getContentType(),
          file.getBytes());

      String[] fileInfo = fileName.split("_");
      Runnable work;

      if (fileType.equals(FileMeta.TYPE_AUDIO.getCode())
          && fileInfo.length == 5
          && fileInfo[3].equals("STT8K")) {
        EngineForUploadValue engine = projectRepository.findEngineForUpload(clientId,
            workspaceId,
            projectId,
            fileName);

        if (engine == null || engine.getEngineId() == null || engine.getSttId() == null) {
          throw BaseException.of(ResponseCode.PJ_ENGINE_NOT_FOUND);
        }

        work = () -> {
          LogUtil.info("업로드 시작: project_id(" + projectId + "), file(" + fileName + ")");

          try {
            engineRepository.updateOneWithFilters(EngineEntity.class,
                Map.of("engine_id", engine.getEngineId()),
                Map.of("info.stt_id", engine.getSttId()),
                Map.of("stt.$[info].file_status", FileStatus.SUCCESS.getCode()),
                null);
          } catch (Exception e) {
            engineRepository.updateOneWithFilters(EngineEntity.class,
                Map.of("engine_id", engine.getEngineId()),
                Map.of("info.stt_id", engine.getSttId()),
                Map.of("stt.$[info].file_status", FileStatus.ERROR.getCode()),
                null);
            LogUtil.error("업로드 실패: project_id(" + projectId + "), file(" + fileName + "), error(" + e + ")");
            return;
          }

          checkUploadAndUpdatePost(userId, clientId, workspaceId, projectId);

          LogUtil.info("업로드 완료: project_id(" + projectId + "), file(" + fileName + ")");
        };
      } else {
        SpeakerForUploadValue speaker;
        JobFileForProjectFileUploadValue jobFileInfo;

        if (fileType.equals(FileMeta.TYPE_IMAGE.getCode())
            && (fileInfo.length == 5 || fileInfo.length == 6)
            && fileInfo[0].startsWith("SPEAKER")) {
          jobFileInfo = null;
          speaker = projectRepository.findSpeakerForUpload(clientId,
              workspaceId,
              projectId,
              fileName);

          if (speaker == null || speaker.getSpeakerId() == null) {
            throw BaseException.of(ResponseCode.PJ_SPEAKER_NOT_FOUND);
          }

          projectRepository.updateOneWithFilters(Project.class,
              Map.of("project_id", projectId),
              Map.of("speaker.speaker_id", fileInfo[1]),
              Map.of("speakers.$[speaker].thumbnail_file_status", FileStatus.PROGRESS.getCode()),
              null);
        } else {
          speaker = null;
          jobFileInfo = projectRepository.findJobFileForUpload(clientId,
              workspaceId,
              projectId,
              fileName);

          if (jobFileInfo == null) {
            LogUtil.error("업로드 실패: project_id(" + projectId + "), file(" + fileName + ")");
            throw BaseException.of(ResponseCode.PJ_FILE_UPLOAD_IMPOSSIBLE);
          }

          projectRepository.updateOneWithFilters(Project.class,
              Map.of("project_id", projectId),
              Map.of("jf.file_name", jobFileInfo.getJobFile().getFileName()),
              Map.of("jobs.$[].job_files.$[jf].file_status", FileStatus.PROGRESS.getCode()),
              null);
        }

        work = () -> {
          LogUtil.info("업로드 시작: project_id(" + projectId + "), file(" + fileName + ")");

          try {
            Path targetPath = Paths.get(
                "workspace",
                workspaceId.toString(),
                "project",
                projectId.toString()
            );

            if (speaker != null) {
              targetPath = targetPath.resolve("speaker");
            } else {
              targetPath = targetPath
                  .resolve("job")
                  .resolve(jobFileInfo.getJobId().toString());
            }

            Path uploadPath = Paths.get(filePath).resolve(targetPath);

            FileUtil.FileInfo fileUploadInfo = FileUtil.upload(uploadPath.toString(), target);

            String fileDownloadUrl = fileUrl
                + "/"
                + targetPath.toString().replace(File.separatorChar, '/')
                + "/"
                + fileUploadInfo.getSavedName();

            if (speaker != null) {
              projectRepository.updateOneWithFilters(Project.class,
                  Map.of("project_id", projectId),
                  Map.of("speaker.speaker_id", fileInfo[1]),
                  Map.of("speakers.$[speaker].thumbnail_file_path", fileUploadInfo.getSavedPath(),
                      "speakers.$[speaker].thumbnail_file_url", fileDownloadUrl,
                      "speakers.$[speaker].thumbnail_file_status", FileStatus.SUCCESS.getCode()),
                  null);
            } else {
              String fileExt = FileUtil.getFileExtension(fileName, false);
              Map<String, Object> setInfo = new HashMap<>();

              if (fileType.equals(FileMeta.TYPE_VIDEO.getCode())) {
                DemuxerTrackMeta videoInfo = MP4Demuxer.createMP4Demuxer(new ByteBufferSeekableByteChannel(ByteBuffer
                        .wrap(target.getBytes()), (int) target.getSize()))
                    .getVideoTrack()
                    .getMeta();
                double duration = Math.ceil(videoInfo.getTotalDuration() * 10) / 10;
                setInfo.put("jobs.$[].job_files.$[jf].file_info", JobFile.FileInfo.builder()
                    .duration(DateUtil.convertToStringBySecond(duration))
                    .format(fileExt)
                    .fps((int) Math.ceil(videoInfo.getTotalFrames() / duration))
                    .width(videoInfo.getVideoCodecMeta().getSize().getWidth())
                    .height(videoInfo.getVideoCodecMeta().getSize().getHeight())
                    .angle(jobFileInfo.getJobFile().getFileInfo().getAngle())
                    .build());
              } else if (fileType.equals(FileMeta.TYPE_AUDIO.getCode())) {
                AudioInputStream is = AudioSystem.getAudioInputStream(new ByteArrayInputStream(target.getBytes()));
                AudioFormat audioInfo = is.getFormat();
                int channel = audioInfo.getChannels();
                float sampleRate = audioInfo.getSampleRate();
                setInfo.put("jobs.$[].job_files.$[jf].file_info", JobFile.FileInfo.builder()
                    .duration(DateUtil.convertToStringBySecond(is.getFrameLength() / sampleRate))
                    .format(fileExt)
                    .sampleRate(((int) Math.ceil(sampleRate) / 1000) + "kHz")
                    .channel(channel == 1 ? "mono" : (channel == 2 ? "stereo" : "other"))
                    .build());
              }

              setInfo.put("jobs.$[].job_files.$[jf].file_path", fileUploadInfo.getSavedPath());
              setInfo.put("jobs.$[].job_files.$[jf].file_url", fileDownloadUrl);
              setInfo.put("jobs.$[].job_files.$[jf].file_status", FileStatus.SUCCESS.getCode());

              projectRepository.updateOneWithFilters(Project.class,
                  Map.of("project_id", projectId),
                  Map.of("jf.file_name", jobFileInfo.getJobFile().getFileName()),
                  setInfo,
                  null);
            }
          } catch (IOException | UnsupportedAudioFileException | BaseException e) {
            if (speaker != null) {
              projectRepository.updateOneWithFilters(Project.class,
                  Map.of("project_id", projectId),
                  Map.of("speaker.speaker_id", speaker.getSpeakerId()),
                  Map.of("speakers.$[speaker].thumbnail_file_status", FileStatus.ERROR.getCode()),
                  null);
            } else {
              projectRepository.updateOneWithFilters(WorkspaceEntity.class,
                  Map.of("project_id", projectId),
                  Map.of("jf.file_name", jobFileInfo.getJobFile().getFileName()),
                  Map.of("jobs.$[].job_files.$[jf].file_status", FileStatus.ERROR.getCode()),
                  null);
            }

            LogUtil.error("업로드 실패: project_id(" + projectId + "), file(" + fileName + "), error(" + e + ")");
            return;
          }

          checkUploadAndUpdatePost(userId, clientId, workspaceId, projectId);

          LogUtil.info("업로드 완료: project_id(" + projectId + "), file(" + fileName + ")");
        };
      }

      ThreadUtil.create(work).start();
    } catch (IOException e) {
      throw BaseException.of(ResponseCode.PJ_FILE_WRONG);
    }
  }

  private void checkUploadAndUpdatePost(ObjectId userId, String clientId, ObjectId workspaceId, ObjectId projectId) {
    UploadResultNotSuccessValue info = projectRepository.findUploadResultNotSuccess(clientId, workspaceId, projectId);

    if (info.getJobUploadResult() == 0 && info.getSpeakerUploadResult() == 0) {
      projectRepository.updateOneWithFilters(Project.class,
          Map.of("project_id", projectId),
          null,
          Map.of("post_status", ProjectStatus.POST_ON.getCode()),
          null);

      LocalDateTime now = LocalDateTime.now();
      UploadInfoEntity uploadInfo = uploadInfoRepository.findByUserId(userId);

      if (uploadInfo == null) {
        uploadInfo = UploadInfoEntity.builder()
            .userId(userId)
            .info(List.of(UploadInfoEntity.Info.builder()
                .workspaceId(workspaceId)
                .created(now)
                .build()))
            .build();
      } else {
        uploadInfo.getInfo().add(UploadInfoEntity.Info.builder()
            .workspaceId(workspaceId)
            .created(now)
            .build());
      }

      uploadInfoRepository.save(uploadInfo);
    }
  }
}

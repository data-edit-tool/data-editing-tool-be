package ai.maum.data.edit.api.project;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ai.maum.data.edit.api.csv.CsvEntity;
import ai.maum.data.edit.api.csv.CsvRepository;
import ai.maum.data.edit.api.project.dto.response.ProjectExtractResponse;
import ai.maum.data.edit.api.project.dto.response.ProjectInfoResponse;
import ai.maum.data.edit.api.project.repository.ProjectRepository;
import ai.maum.data.edit.api.user.dto.response.UserTypeResponse;
import ai.maum.data.edit.api.workspace.repository.WorkspaceRepository;
import ai.maum.data.edit.common.enums.ResponseCode;
import ai.maum.data.edit.common.enums.WorkStatus;
import ai.maum.data.edit.common.exception.BaseException;
import ai.maum.data.edit.common.util.FileUtil;
import ai.maum.data.edit.common.util.UserUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {

  @Value("${service.file.path}")
  private String filePath;

  @Value("${service.file.url}")
  private String fileUrl;

  private final WorkspaceRepository workspaceRepository;
  private final ProjectRepository projectRepository;
  private final CsvRepository csvRepository;
  private final ObjectMapper om;

  public String extract(
      ObjectId workspaceId,
      ObjectId projectId,
      List<String> request,
      boolean isWorkspace) {
    ProjectExtractResponse response = projectRepository.extractProjects(UserUtil.getClientId(),
        workspaceId,
        projectId,
        request,
        Pattern.compile("/(.*?)>|\\((.*?)\\)|[^A-Za-z0-9ㄱ-ㅎㅏ-ㅣ가-힣]"));

    response.getSubsetInfo()
        .getSets()
        .forEach(set -> {
          if (set.getSubsetStatus().equals(WorkStatus.CM.getCode())) {
            ObjectId jobId = set.getJobId();
            List<CsvEntity> csvs = csvRepository.findByJobId(jobId);

            if (csvs.isEmpty()) {
              throw BaseException.of(ResponseCode.PJ_EXTRACT_CSV_NOT_EXIST);
            }

            boolean isMix = csvs.stream().anyMatch(csv -> csv.getSpeakerId().equals("MIX"));
            Map<String, Map<String, Map<Integer, Map<String, String>>>> info = new HashMap<>();

            if (isMix) {
              csvs = csvs.stream().filter(csv -> csv.getSpeakerId().equals("MIX")).collect(Collectors.toList());
            }

            csvs.forEach(csv -> {
              if (!info.containsKey(jobId.toString())) {
                info.put(jobId.toString(), new HashMap<>());
              }
              info.get(jobId.toString()).put(csv.getSpeakerId(), csv.getDatas().stream()
                  .collect(Collectors.toMap(CsvEntity.Data::getTime,
                      data -> Map.of("pitch", data.getPitch(), "loudness", data.getLoudness()))));
            });

            set.getDialogue()
                .getInformation()
                .getParaVerbal()
                .forEach(p -> {
                  Map<Integer, Map<String, String>> data =
                      isMix ? info.get(jobId.toString()).get("MIX") : info.get(jobId.toString()).get(p.getSpeakerId());

                  if (data == null) {
                    throw BaseException.of(ResponseCode.PJ_EXTRACT_CSV_NOT_EXIST);
                  }

                  List<String> pitches = new ArrayList<>();
                  List<String> loudnesses = new ArrayList<>();

                  int startTime =
                      p.getOrgStartTime() % 10 == 0 ? p.getOrgStartTime() : ((p.getOrgStartTime() / 10) * 10);
                  int endTime = (p.getOrgEndTime() % 10 == 0 ? p.getOrgEndTime() : ((p.getOrgEndTime() / 10) + 1) * 10);

                  for (int time = startTime; time < endTime; time += 10) {
                    String pitch, loudness;

                    if (data.get(time) == null) {
                      pitch = "0";
                      loudness = "0";
                    } else {
                      pitch = data.get(time).get("pitch");
                      loudness = data.get(time).get("loudness");
                    }

                    pitches.add(pitch);
                    loudnesses.add(loudness);
                  }

                  p.setPitch(pitches);
                  p.setLoudness(loudnesses);
                });
          }
        });

    if (response.getMainsetInfo() == null) {
      throw BaseException.of(ResponseCode.PJ_EXTRACT_IMPOSSIBLE_AS_NO_MATCH);
    }

    try {
      String fileName = response.getMainsetInfo().getMainsetId()
          + "_"
          + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
          + ".json";
      String targetPath = "workspace"
          + "/"
          + workspaceId
          + "/"
          + "project"
          + "/"
          + projectId;
      Path target = FileUtil.getPathWithEncoding(filePath + "/" + targetPath, fileName);

      Files.createDirectories(FileUtil.getPathWithEncoding(filePath + "/" + targetPath, null));
      om.writeValue(target.toFile(), response);

      return isWorkspace ? target.toString() : (fileUrl + "/" + targetPath + "/" + fileName);
    } catch (IOException e) {
      throw BaseException.of(ResponseCode.PJ_EXTRACT_FILE_SAVE_IMPOSSIBLE);
    }
  }

  public ProjectInfoResponse findProjectInfo(ObjectId workspaceId, ObjectId projectId) {
    ObjectId userId = UserUtil.getUserId();
    UserTypeResponse userType = workspaceRepository.getAccessUserType(workspaceId, userId); // user_type

    ProjectInfoResponse response = projectRepository.findProjectInfo(projectId, userType.getType(), userId);
    response.setUserType(userType.getType());

    return response;
  }
}

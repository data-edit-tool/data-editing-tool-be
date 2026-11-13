package ai.maum.data.edit.api.work.service;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.maum.data.edit.api.user.enums.UserType;
import ai.maum.data.edit.api.work.dto.request.ComplexConversationLabelingRequest;
import ai.maum.data.edit.api.work.dto.request.SaveLabelingRequest;
import ai.maum.data.edit.api.work.repository.WorkRepository;
import ai.maum.data.edit.api.workspace.field.labeling.ComplexConversationLabeling;
import ai.maum.data.edit.api.workspace.field.project.Project;
import ai.maum.data.edit.common.enums.ResponseCode;
import ai.maum.data.edit.common.enums.WorkStatus;
import ai.maum.data.edit.common.exception.BaseException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LComplexConversationService implements LabelingService {

  private final WorkRepository workRepository;

  @Override
  public Object saveLabeling(ObjectId userId, UserType userType, SaveLabelingRequest request, String workStatus) {
    int step = request.getStep(); // 현재 저장될 step

    if (workRepository.checkMyJob(userId, userType, request)) { // 본인이 하던 작업인지 확인하기 위한 쿼리
      if (!workStatus.equals(WorkStatus.IC.getCode())) { // 작업 불가 검수 중이 아닐 때
        ComplexConversationLabelingRequest labeling = null;

        // Labeling 데이터 저장
        ObjectId labelingId;
        for (int i = 0; i < request.getLabelings().size(); i++) {
          labeling = request.getLabelings().get(i); // 요청받은 labeling 데이터
          String labelingIdStr = labeling.getLabelingId(); // labeling id 가져오기

          Map<String, Object> set = new HashMap<>();
          if (StringUtils.isBlank(labelingIdStr)) {
            labelingId = new ObjectId();
          } else {
            labelingId = new ObjectId(labelingIdStr);
          }

          switch (step) {
            case 1: // verbal
              set.put("jobs.$[job].works.$[work].labelings.$[labeling].verbal", makeVerbalsData(labeling.getVerbal()));
              break;

            case 2: // paraverbal
              List<ComplexConversationLabelingRequest.Paraverbal> paraverbals = labeling.getParaverbal();
              ComplexConversationLabelingRequest.Paraverbal paraverbal =
                  new ComplexConversationLabelingRequest.Paraverbal();
              List<ComplexConversationLabeling.Paraverbal> newDatas2 = new ArrayList<>();//
              if (paraverbals != null && !paraverbals.isEmpty()) {
                for (int j = 0; j < paraverbals.size(); j++) {
                  ObjectId dataId = null;
                  paraverbal = paraverbals.get(j);
                  // 새로운 데터 set하기
                  if (paraverbal.getParaverbalId() == null || paraverbal.getParaverbalId().equals("")) {
                    dataId = new ObjectId();
                  } else {
                    dataId = new ObjectId(paraverbal.getParaverbalId());
                  }

                  // 새로 데이터 세팅
                  newDatas2.add(ComplexConversationLabeling.Paraverbal.builder()
                      .paraverbalId(dataId)
                      .func(paraverbal.getFunc())
                      .startTime(paraverbal.getStartTime())
                      .endTime(paraverbal.getEndTime())
                      .build());
                }
              }
              set.put("jobs.$[job].works.$[work].labelings.$[labeling].paraverbal", newDatas2);
              break;

            case 3: // nonverbal
              List<ComplexConversationLabelingRequest.Nonverbal> nonverbals = labeling.getNonverbal();
              ComplexConversationLabelingRequest.Nonverbal nonverbal =
                  new ComplexConversationLabelingRequest.Nonverbal();
              List<ComplexConversationLabeling.Nonverbal> newDatas3 = new ArrayList<>();//
              if (nonverbals != null && !nonverbals.isEmpty()) {
                for (int j = 0; j < nonverbals.size(); j++) {
                  ObjectId dataId = null;
                  nonverbal = nonverbals.get(j);
                  // 새로운 데터 set하기
                  if (nonverbal.getNonverbalId() == null || nonverbal.getNonverbalId().equals("")) {
                    dataId = new ObjectId();
                  } else {
                    dataId = new ObjectId(nonverbal.getNonverbalId());
                  }

                  // 새로 데이터 세팅
                  newDatas3.add(ComplexConversationLabeling.Nonverbal.builder()
                      .nonverbalId(dataId)
                      .angle(nonverbal.getAngle())
                      .subject(nonverbal.getSubject())
                      .action(nonverbal.getAction())
                      .actionStr(nonverbal.getActionStr())
                      .obj(nonverbal.getObj())
                      .intent(nonverbal.getIntent())
                      .func(nonverbal.getFunc())
                      .emotion(nonverbal.getEmotion())
                      .affectiveState(nonverbal.getAffectiveState())
                      .startTime(nonverbal.getStartTime())
                      .endTime(nonverbal.getEndTime())
                      .build());
                }
              }
              set.put("jobs.$[job].works.$[work].labelings.$[labeling].nonverbal", newDatas3);
              break;
          }

          // update 세팅
          List<Map<String, Object>> filter = makeArrayFilters(request);
          filter.get(1).put("labeling.labeling_id", labelingId);

          set.put("jobs.$[job].works.$[work].updated", LocalDateTime.now());
          workRepository.updateOneWithFilters(Project.class, filter.get(0), filter.get(1), set, null);

          if (step != 1 && labeling.getVerbal() != null && !labeling.getVerbal().isEmpty()) {
            modifyVerbals(filter.get(0), filter.get(1), labeling.getVerbal());
          }
        }
      } // 작업 불가 검수 중일 경우 데이터 저장은 없음. 아래에서 step만 update함.

      // step 저장
      int moveStep = request.getMoveStep();
      List<Map<String, Object>> filter = makeArrayFilters(request);
      if (moveStep != 0) {
        step = moveStep;

        Map<String, Object> set = new HashMap<>();
        if (userType == UserType.WORKER) {
          set.put("jobs.$[job].works.$[work].step", step);
        } else if (userType == UserType.CHECKER) {
          set.put("jobs.$[job].works.$[work].check_step", step);
        }
        workRepository.updateOneWithFilters(Project.class, filter.get(0), filter.get(1), set, null);
      }

      return step;
    } else {
      throw BaseException.of(ResponseCode.WK_NOT_YOURS);
    }
  }

  private List<Map<String, Object>> makeArrayFilters(SaveLabelingRequest request) {
    List<Map<String, Object>> result = new ArrayList<>();

    Map<String, Object> where = new HashMap<>();
    where.put("project_id", new ObjectId(request.getProjectId()));
    result.add(where);

    Map<String, Object> arrayFilters = new HashMap<>();
    arrayFilters.put("job.job_id", new ObjectId(request.getJobId()));
    arrayFilters.put("work.work_id", new ObjectId(request.getWorkId()));
    result.add(arrayFilters);

    return result;
  }

  private List<ComplexConversationLabeling.Verbal> makeVerbalsData(
      List<ComplexConversationLabelingRequest.Verbal> verbals) {
    List<ComplexConversationLabeling.Verbal> newVerbals = new ArrayList<>();

    for (int i = 0; i < verbals.size(); i++) {
      ObjectId dataId;
      ComplexConversationLabelingRequest.Verbal verbal = verbals.get(i);

      if (StringUtils.isBlank(verbal.getVerbalId())) {
        dataId = new ObjectId();
      } else {
        dataId = new ObjectId(verbal.getVerbalId());
      }

      // 새로 데이터 세팅
      ComplexConversationLabeling.Verbal.VAD verbalVad = null;
      ComplexConversationLabeling.Verbal.VAD overallVad = null;
      if (verbal.getVerbalVad() != null) {
        verbalVad = ComplexConversationLabeling.Verbal.VAD.builder()
            .valence(verbal.getVerbalVad().getValence())
            .arousal(verbal.getVerbalVad().getArousal())
            .dominance(verbal.getVerbalVad().getDominance())
            .build();
      }
      if (verbal.getOverallVad() != null) {
        overallVad = ComplexConversationLabeling.Verbal.VAD.builder()
            .valence(verbal.getOverallVad().getValence())
            .arousal(verbal.getOverallVad().getArousal())
            .dominance(verbal.getOverallVad().getDominance())
            .build();
      }

      newVerbals.add(ComplexConversationLabeling.Verbal.builder()
          .verbalId(dataId)
          .startTime(verbal.getStartTime())
          .endTime(verbal.getEndTime())
          .affectiveState(verbal.getAffectiveState())
          .sentiment(verbal.getSentiment())
          .sentence(verbal.getSentence())
          .channel(verbal.getChannel())
          .bcIntent(verbal.getBcIntent())
          .fcIntent(verbal.getFcIntent())
          .emotion(verbal.getEmotion())
          .verbalVad(verbalVad)
          .overallVad(overallVad)
          .build());
    }

    return newVerbals;
  }

  private void modifyVerbals(Map<String, Object> where, Map<String, Object> filter,
      List<ComplexConversationLabelingRequest.Verbal> verbals) {
    Map<String, Map<String, Object>> arrayFilters = new HashMap<>();
    Map<String, Map<String, Object>> set = new HashMap<>();

    verbals.forEach(verbal -> {
      Map<String, Object> map = new HashMap<>();
      map.put("jobs.$[job].works.$[work].labelings.$[labeling].verbal.$[verbal].sentence", verbal.getSentence());
      map.put("jobs.$[job].works.$[work].labelings.$[labeling].verbal.$[verbal].start_time", verbal.getStartTime());
      map.put("jobs.$[job].works.$[work].labelings.$[labeling].verbal.$[verbal].end_time", verbal.getEndTime());
      set.put(verbal.getVerbalId(), map);

      Map<String, Object> filterMap = new HashMap<>(filter);
      filterMap.put("verbal.verbal_id", new ObjectId(verbal.getVerbalId()));
      arrayFilters.put(verbal.getVerbalId(), filterMap);
    });

    workRepository.updateBulk(Project.class, where, arrayFilters, set);
  }
}

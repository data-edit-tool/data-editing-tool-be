package ai.maum.data.edit.api.workspace.field.project.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProjectStatus {

  // project status
  PENDING("PJ_ST_PENDING", "대기중"),
  WORKING("PJ_ST_WORKING", "진행중"),
  FINISH("PJ_ST_FINISH", "완료"),
  POST_ON("PJ_POST_ST_ON", "게시중"),
  POST_OFF("PJ_POST_ST_OFF", "일시정지");

  private final String code;
  private final String codeName;
}

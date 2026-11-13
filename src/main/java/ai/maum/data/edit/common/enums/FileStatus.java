package ai.maum.data.edit.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileStatus {

  // file status
  PENDING("PENDING", "대기중"),
  PROGRESS("PROGRESS", "진행중"),
  SUCCESS("SUCCESS", "완료"),
  ERROR("ERROR", "오류");

  private final String code;
  private final String codeName;
}

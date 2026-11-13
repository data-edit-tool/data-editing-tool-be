package ai.maum.data.edit.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum WorkStatus {
  RD("RD", "작업 전"),
  // worker status
  AS("AS", "작업 중"), // '작업 시작' 시 작업 중으로 상태 변경
  RQ("RQ", "제출"), // = 검수 요청
  IM("IM", "작업 불가"), // = 작업 불가 검수 요청

  // checker status
  RJ("RJ", "반려"),
  IS("IS", "검수 중"),
  IC("IC", "작업 불가 검수 중"),
  NU("NU", "작업 불가 승인"),
  CM("CM", "최종 정상 승인");

  private final String code;
  private final String name;

  public static WorkStatus fromCode(String code) {
    for (WorkStatus workStatus : WorkStatus.values()) {
      if (workStatus.code.equals(code)) {
        return workStatus;
      }
    }
    throw new IllegalArgumentException("Unknown code: " + code);
  }
}

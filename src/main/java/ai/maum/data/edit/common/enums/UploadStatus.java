package ai.maum.data.edit.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UploadStatus {
  SUCCESS("UL_ST_SUCCESS", "완료"),
  PROGRESS("UL_ST_PROGRESS", "진행중"),
  ERROR("UL_ST_ERROR", "실패");

  private final String code;
  private final String codeName;
}

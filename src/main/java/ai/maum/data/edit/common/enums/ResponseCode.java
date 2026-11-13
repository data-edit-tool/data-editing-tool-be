package ai.maum.data.edit.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseCode implements CodeInfo {
  // Uxxx : 사용자 관련
  USER_NOT_EXIST("U000", "일치하는 계정 정보가 없습니다. 다시 확인해 주세요."),
  USER_ALREADY_EXIST("U001", "이미 존재하는 사용자 계정입니다."),
  USER_DELETE_IMPOSSIBLE("U002", "진행 중인 프로젝트가 존재해 삭제할 수 없습니다."),
  ACCESS_DENIED("U003", "접근 권한이 없습니다."),
  USER_FUNCTION_IMPOSSIBLE_ADMIN("U004", "관리자 계정은 해당 기능을 수행할 수 없습니다."),
  AUTHENTICATION_FAILED("U005", "인증에 실패했습니다."),

  // U5xx : 비밀번호 초기화 관련
  PW_INIT("U500", "비밀번호 초기화를 진행해 주세요."),
  PW_CHECK("U501", "비밀번호가 일치하지 않습니다. 다시 시도해 주세요."),
  PW_CHECK_PREV_CURR("U502", "현재 비밀번호와 동일합니다. 동일하지 않은 비밀번호로 변경해 주세요."),

  // Axxx : 관리자 관련
  ADMIN_NOT_EXIST("A000", "관리자 계정이 존재하지 않습니다."),
  ADMIN_ALREADY_EXIST("A001", "관리자 계정은 이미 존재합니다."),
  INVALID_ADMIN_NAME("A002", "사용자명은 한글, 영문, 숫자, 특수문자만 사용 가능하며 20자 이내로 입력해주세요."),
  INVALID_ADMIN_ACCOUNT("A003", "계정은 영문과 숫자만 사용 가능합니다."),
  INVALID_ADMIN_PASSWORD("A004", "비밀번호는 최소 8자 이상이며, 대소문자, 숫자, 특수문자를 각각 포함해야 합니다."),

  // W000 ~ 200 : 워크스페이스 관련
  WS_NOT_EXIST("W000", "해당 워크스페이스가 존재하지 않습니다."),
  WS_HAS_PROJECT("W001", "해당 워크스페이스에 진행 중인 프로젝트가 존재합니다."),
  WS_NOT_MATCH_CLIENT_ID("W002", "해당 워크스페이스와 고객사 ID가 일치하지 않습니다."),
  WS_UNASSIGN_IMPOSSIBLE("W003", "작업 중인 프로젝트가 존재하여 등록 취소를 할 수 없습니다."),
  WS_ACCESS_USER_NOT_EXIST("W004", "워크스페이스에 해당 Access User가 존재하지 않습니다."),

  // W201 ~ 400 : 작업/검수관련
  WK_DATA_NOT_EXIT("W201", "저장할 데이터가 없습니다."),
  WK_STATUS_IM("W202", "작업 불가 건으로 접근 불가합니다."),
  WK_ALREADY_WORKING("W203", "이미 작업 진행 중에 있어 작업 시작이 불가능합니다."),
  WK_NOT_YOURS("W204", "본인이 하던 작업이 아니므로 저장이 불가합니다."),
  WK_CANNOT_CANCEL("W205", "검수 중이므로 제출 취소가 불가합니다."),
  WK_CANNOT_CHECK("W206", "아직 작업 중이므로 검수가 불가합니다."),
  WK_COMPLETE_FAIL("W207", "저장에 실패하였습니다. 다시 시도해주세요."),

  // Pxxx : 프로젝트 관련
  PJ_NOT_EXIST("P000", "해당 프로젝트가 존재하지 않습니다."),
  PJ_NAME_WRONG("P001", "프로젝트명이 잘못되었습니다."),
  PJ_SPEAKER_WRONG("P002", "발화자 정보가 잘못되었습니다."),
  PJ_DATA_FILE_NOT_EXIST("P003", "프로젝트에 업로드할 데이터 파일 정보가 존재하지 않습니다."),
  PJ_SPEAKER_FILE_NOT_EXIST("P004", "프로젝트에 업로드할 발화자 이미지 파일 정보가 존재하지 않습니다."),
  PJ_ALREADY_EXIST("P005", "워크스페이스에 이름이 동일한 프로젝트가 존재합니다."),
  PJ_DELETE_IMPOSSIBLE("P006", "게시 중인 프로젝트가 존재하여 삭제할 수 없습니다."),
  PJ_LIST_NOT_EXIST("P007", "프로젝트가 존재하지 않습니다."),
  PJ_FILE_UPLOAD_IMPOSSIBLE("P008", "프로젝트에 해당 파일을 업로드할 수 없습니다."),
  PJ_FILE_NAME_WRONG("P009", "파일명이 잘못되었습니다."),
  PJ_FILE_TYPE_WRONG("P010", "파일 종류가 잘못되었습니다."),
  PJ_SPEAKER_NOT_FOUND("P011", "해당 파일에 대한 발화자 데이터가 존재하지 않습니다."),
  PJ_ENGINE_NOT_FOUND("P012", "해당 파일에 대한 엔진 데이터가 존재하지 않습니다."),
  PJ_FILE_WRONG("P013", "잘못된 파일입니다."),
  PJ_STT_FILE_NOT_EXIST("P014", "프로젝트에 업로드할 STT 음성 파일 정보가 존재하지 않습니다."),
  PJ_POST_STATUS_UPDATE_IMPOSSIBLE("P015", "프로젝트가 존재하지 않거나 업로드가 완료되지 않아 게시 상태를 갱신할 수 없습니다."),
  PJ_REUPLOAD_IMPOSSIBLE("P016", "해당 파일이 존재하지 않거나 오류 상태가 아니기 때문에 재업로드를 할 수 없습니다."),
  PJ_EXTRACT_FILE_SAVE_IMPOSSIBLE("P017", "추출한 데이터를 저장할 수 없습니다."),
  PJ_EXTRACT_CSV_NOT_EXIST("P018", "CSV 정보가 존재하지 않아 추출할 수 없습니다."),
  PJ_EXTRACT_IMPOSSIBLE_AS_NO_MATCH("P019", "해당 상태의 추출 가능한 작업은 없습니다."),

  // Jxxx : 작업(서브셋) 관련
  JOB_NOT_EXIST("J000", "해당 작업이 존재하지 않습니다."),
  JOB_WORKER_ASSIGN_NOT_ALLOWED("J001", "작업자를 지정할 수 없는 상태입니다."),
  JOB_WORKER_CHANGE_NOT_ALLOWED("J002", "작업자를 변경/해제할 수 없는 상태입니다."),
  JOB_CHECKER_ASSIGN_NOT_ALLOWED("J003", "검수자를 지정할 수 없는 상태입니다."),
  JOB_CHECKER_CHANGE_NOT_ALLOWED("J004", "검수자를 변경/해제할 수 없는 상태입니다."),
  JOB_VIEWER_ONLY_ACCESS("J005", "해당 작업에는 뷰어만 접근할 수 있습니다.");

  private final String code;
  private final String message;
}

package ai.maum.data.edit.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseMeta implements CodeInfo {
  
  SUCCESS("F000", "성공적으로 수행하였습니다."),
  FAILURE("F001", "알 수 없는 오류가 발생했습니다."),
  UNAUTHORIZED("F002", "인증 정보가 존재하지 않습니다."),
  ACCESS_DENIED("F003", "권한이 존재하지 않습니다."),
  FILE_UPLOAD_ERROR("F004", "파일 업로드 중 오류가 발생했습니다."),
  FILE_DELETE_ERROR("F005", "파일 삭제 중 오류가 발생했습니다."),
  PARAM_WRONG("F006", "요청 파라미터가 존재하지 않거나 잘못되었습니다."),
  UNAUTHORIZED_DOCUMENT("F007", "API 문서 접근이 인증되지 않은 사용자입니다."),
  WRONG_DOCUMENT_USER("F008", "잘못된 API 문서 사용자입니다."),
  ALREADY_LOGIN_DOCUMENT_USER("F009", "이미 로그인한 API 문서 사용자입니다."),
  ACCESS_TOKEN_EXPIRED("F010", "Access Token이 만료되었습니다."),
  REFRESH_TOKEN_EXPIRED("F011", "Refresh Token이 만료되었습니다."),
  TOKEN_INVALID("F012", "유효하지 않은 Token입니다."),
  AUTHENTICATION_FAILURE("F013", "인증 과정 중 오류가 발생했습니다.");

  private final String code;
  private final String message;
}

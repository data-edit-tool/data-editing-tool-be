package ai.maum.data.edit.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import ai.maum.data.edit.common.enums.CodeInfo;
import ai.maum.data.edit.common.enums.ResponseMeta;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.ToString;


@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(title = "공통 응답", description = "기본적인 API에 적용될 공통 응답 양식이다.")
public class BaseResponse<T> {

  @Schema(title = "결과", description = "API 처리 결과 유무", example = "true")
  private Boolean result;
  @Schema(title = "코드", description = "API 처리 결과 코드", example = "F000")
  private String code;
  private T data;
  @Schema(title = "메시지", description = "API 처리 결과 메시지", example = "성공적으로 수행하였습니다.")
  private String message;

  private BaseResponse() {
  }

  /**
   * API 처리 결과가 실패일 경우 응답 양식이다.
   * 응답 메시지가 기본값으로 입력된다.
   *
   * @return 공통 응답 객체
   * @author baekgol
   */
  public static BaseResponse<Void> failure() {
    BaseResponse<Void> response = new BaseResponse<>();
    response.result = false;
    response.message = ResponseMeta.FAILURE.getMessage();
    return response;
  }

  /**
   * API 처리 결과가 실패일 경우 응답 양식이다.
   * 응답 메시지를 입력할 수 있다.
   *
   * @param message 응답 메시지
   * @return 공통 응답 객체
   * @author baekgol
   */
  public static BaseResponse<Void> failure(String message) {
    BaseResponse<Void> response = new BaseResponse<>();
    response.result = false;
    response.message = message;
    return response;
  }

  /**
   * API 처리 결과가 실패일 경우 응답 양식이다.
   * 응답 메시지에 발생한 예외 정보가 입력된다.
   *
   * @param e 예외
   * @return 공통 응답 객체
   * @author baekgol
   */
  public static BaseResponse<Void> failure(Exception e) {
    BaseResponse<Void> response = new BaseResponse<>();
    response.result = false;
    response.message = e.getMessage();
    return response;
  }

  /**
   * API 처리 결과가 성공일 경우 응답 양식이다.
   * 응답 데이터와 메시지를 입력할 수 있다.
   *
   * @param data    응답 데이터
   * @param message 응답 메시지
   * @return 공통 응답 객체
   * @author baekgol
   */
  public static <T> BaseResponse<T> success(T data, String message) {
    BaseResponse<T> response = new BaseResponse<>();
    response.result = true;
    response.code = ResponseMeta.SUCCESS.getCode();
    response.data = data;
    response.message = message;
    return response;
  }

  /**
   * API 처리 결과가 성공일 경우 응답 양식이다.
   * 응답 데이터, 코드, 메시지를 입력할 수 있다.
   *
   * @param data 응답 데이터
   * @param info 코드 정보
   * @return 공통 응답 객체
   * @author baekgol
   */
  public static <T> BaseResponse<T> success(T data, CodeInfo info) {
    BaseResponse<T> response = new BaseResponse<>();
    response.result = true;
    response.code = info.getCode();
    response.data = data;
    response.message = info.getMessage();
    return response;
  }

  /**
   * API 처리 결과가 성공일 경우 응답 양식이다.
   * 응답 코드를 입력할 수 있다.
   * 응답 메시지에 발생한 예외 정보가 입력된다.
   *
   * @param code 응답 코드
   * @param e    예외
   * @return 공통 응답 객체
   * @author baekgol
   */
  public static BaseResponse<Void> success(String code, Exception e) {
    BaseResponse<Void> response = new BaseResponse<>();
    response.result = true;
    response.code = code;
    response.message = e.getMessage();
    return response;
  }

  /**
   * API 처리 결과가 성공일 경우 응답 양식이다.
   * 응답 코드 및 메시지가 기본값으로 입력된다.
   *
   * @return 공통 응답 객체
   * @author baekgol
   */
  public static BaseResponse<Void> success() {
    BaseResponse<Void> response = new BaseResponse<>();
    response.result = true;
    response.code = ResponseMeta.SUCCESS.getCode();
    response.message = ResponseMeta.SUCCESS.getMessage();
    return response;
  }

  /**
   * API 처리 결과가 성공일 경우 응답 양식이다.
   * 응답 데이터를 입력할 수 있다.
   * 응답 코드 및 메시지가 기본값으로 입력된다.
   *
   * @param data 응답 데이터
   * @return 공통 응답 객체
   * @author baekgol
   */
  public static <T> BaseResponse<T> success(T data) {
    BaseResponse<T> response = new BaseResponse<>();
    response.result = true;
    response.code = ResponseMeta.SUCCESS.getCode();
    response.data = data;
    response.message = ResponseMeta.SUCCESS.getMessage();
    return response;
  }

  /**
   * API 처리 결과가 성공일 경우 응답 양식이다.
   * 응답 코드와 메시지를 입력할 수 있다.
   * 응답 데이터가 필요하지 않을 경우 사용한다.
   *
   * @param info 코드 정보
   * @return 공통 응답 객체
   * @author baekgol
   */
  public static BaseResponse<Void> success(CodeInfo info) {
    BaseResponse<Void> response = new BaseResponse<>();
    response.result = true;
    response.code = info.getCode();
    response.message = info.getMessage();
    return response;
  }
}

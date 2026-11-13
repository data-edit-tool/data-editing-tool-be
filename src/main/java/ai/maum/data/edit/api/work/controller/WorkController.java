package ai.maum.data.edit.api.work.controller;

import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import ai.maum.data.edit.api.work.dto.request.UpdateStatusRequest;
import ai.maum.data.edit.api.work.dto.response.WorkStatusReasonResponse;
import ai.maum.data.edit.api.work.service.WorkService;
import ai.maum.data.edit.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "가공 작업", description = "work API")
@RestController
@RequestMapping("/work")
@RequiredArgsConstructor
public class WorkController {

  private final WorkService workService;

  /**
   * 가공 작업 상태 사유 목록을 조회한다.
   *
   * @param workId     작업 ID
   * @param workStatus 작업 상태
   * @return 작업 상태 사유 목록
   */
  @GetMapping("/status/reason")
  @Operation(summary = "가공 작업 상태 사유 목록 조회", description = "가공 작업 상태 사유 목록을 조회한다.")
  public BaseResponse<List<WorkStatusReasonResponse>> getStatusReasonList(
      @Parameter(description = "작업 ID", example = "507f1f77bcf86cd799439011") @RequestParam("work_id") String workId,
      @Parameter(description = "작업 상태(반려: RJ, 작업불가: IM, 작업불가 최종 승인: NU)", example = "RJ") @RequestParam("work_status") String workStatus
  ) {
    return BaseResponse.success(workService.getStatusReasonList(new ObjectId(workId), workStatus));
  }

  @PutMapping("/updateStatus")
  @Operation(summary = "가공 작업 상태 수정", description = "가공 작업 상태를 수정한다.")
  public BaseResponse<Boolean> updateStatus(@RequestBody UpdateStatusRequest request) {
    return BaseResponse.success(workService.updateStatus(request));
  }
}

package ai.maum.data.edit.api.job.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import ai.maum.data.edit.api.job.dto.response.JobFileResponse;
import ai.maum.data.edit.api.job.service.JobFileService;
import ai.maum.data.edit.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "작업 원천 파일", description = "job file API")
@RestController
@RequestMapping("/job/file")
@RequiredArgsConstructor
public class JobFileController {

  private final JobFileService jobFileService;

  @GetMapping("/find")
  @Operation(summary = "원천 데이터 파일 조회", description = "원천 데이터 파일을 불러온다.")
  public BaseResponse<JobFileResponse> findJobFile(@RequestParam("file_id") String fileId) {
    return BaseResponse.success(jobFileService.findJobFile(fileId));
  }

  @GetMapping("/list")
  @Operation(summary = "원천 데이터 파일 목록 조회", description = "원천 데이터 파일 목록을 조회한다.")
  public BaseResponse<List<JobFileResponse>> jobFileList(@RequestParam("job_id") String jobId) {
    return BaseResponse.success(jobFileService.jobFileList(jobId));
  }
}

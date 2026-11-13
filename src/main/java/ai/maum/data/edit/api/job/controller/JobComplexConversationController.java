package ai.maum.data.edit.api.job.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import ai.maum.data.edit.api.job.service.JobComplexConversationService;
import ai.maum.data.edit.api.workspace.field.project.ComplexConversation;
import ai.maum.data.edit.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "작업 복합대화", description = "job CC API")
@RestController
@RequestMapping("/job/complex")
@RequiredArgsConstructor
public class JobComplexConversationController {

  private final JobComplexConversationService jobService;

  @GetMapping("/findSpeakers")
  @Operation(summary = "가공 작업에 필요한 메타 데이터", description = "가공 작업에 필요한 메타 데이터를 조회한다.")
  public BaseResponse<List<ComplexConversation>> findSpeakers(@RequestParam("job_id") String jobId) {
    return BaseResponse.success(jobService.findSpeakersByJobId(jobId));
  }
}

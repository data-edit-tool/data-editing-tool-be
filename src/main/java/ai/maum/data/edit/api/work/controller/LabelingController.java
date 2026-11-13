package ai.maum.data.edit.api.work.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ai.maum.data.edit.api.work.dto.request.SaveLabelingRequest;
import ai.maum.data.edit.api.work.service.WorkService;
import ai.maum.data.edit.api.workspace.field.labeling.Labeling;
import ai.maum.data.edit.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "작업 라벨링", description = "labeling API")
@RestController
@RequestMapping("/work/labeling")
@RequiredArgsConstructor
public class LabelingController {

  private final WorkService workService;

  @PostMapping("/save")
  @Operation(summary = "라벨링 데이터 저장", description = "가공 작업한 라벨링 데이터를 저장한다.")
  public BaseResponse<Labeling> saveLabeling(@RequestBody SaveLabelingRequest request) {
    return BaseResponse.success(workService.saveLabeling(request));
  }
}

package ai.maum.data.edit.api.admin.category;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import ai.maum.data.edit.api.admin.category.dto.response.AdminCategoryResponse;
import ai.maum.data.edit.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/admin/category")
@RequiredArgsConstructor
@Tag(name = "관리자 카테고리")
public class AdminCategoryController {

  private final AdminCategoryService adminCategoryService;

  /**
   * 고객사별 해당 가공 유형 코드의 카테고리 유형 목록을 조회한다.
   *
   * @param toolTypeCode 가공 유형 코드
   * @return 카테고리 유형 목록
   */
  @GetMapping("/client")
  @Secured({"ROLE_ADMIN"})
  @Operation(summary = "고객사별 카테고리 유형 목록 조회", description = "고객사별 해당 가공 유형 코드의 카테고리 유형 목록을 조회한다.")
  public BaseResponse<List<AdminCategoryResponse>> getCategoryListByClient(
      @RequestParam("tool_type_code") String toolTypeCode
  ) {
    return BaseResponse.success(adminCategoryService.getCategoryListByClient(toolTypeCode));
  }
}

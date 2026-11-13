package ai.maum.data.edit.api.category.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import ai.maum.data.edit.api.category.dto.response.CategoryResponse;
import ai.maum.data.edit.api.category.service.CategoryService;
import ai.maum.data.edit.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@Tag(name = "카테고리", description = "category API")
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping("/find")
  @Operation(summary = "툴에 종속된 카테고리 목록 가져오기")
  public BaseResponse<List<CategoryResponse>> findCategory(@RequestParam("project_id") String projectId,
      @Parameter(description = "ctg_name", example = "verbal|paraverbal|nonverbal") @RequestParam("ctg_name") String ctgName) {
    return BaseResponse.success(categoryService.find(projectId, ctgName));
  }

  @GetMapping("/find/child")
  @Operation(summary = "상위 카테고리에 종속된 하위 카테고리를 가져오기")
  public BaseResponse<List<CategoryResponse>> findChildCategory(@RequestParam("project_id") String projectId,
      @RequestParam("parent_ctg_id") String parentCtgId) {
    return BaseResponse.success(categoryService.findChildCategory(projectId, parentCtgId));
  }
}

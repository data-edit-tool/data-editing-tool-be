package ai.maum.data.edit.api.admin.category;

import org.springframework.stereotype.Service;

import java.util.List;

import ai.maum.data.edit.api.admin.category.dto.response.AdminCategoryResponse;
import ai.maum.data.edit.api.category.repository.CategoryRepository;
import ai.maum.data.edit.common.util.UserUtil;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminCategoryService {

  private final CategoryRepository categoryRepository;

  public List<AdminCategoryResponse> getCategoryListByClient(String toolTypeCode) {
    return categoryRepository.findCategoryListByClient(UserUtil.getClientId(), toolTypeCode);
  }
}

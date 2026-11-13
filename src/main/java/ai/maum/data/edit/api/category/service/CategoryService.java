package ai.maum.data.edit.api.category.service;

import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;

import ai.maum.data.edit.api.category.dto.response.CategoryResponse;
import ai.maum.data.edit.api.category.repository.CategoryRepository;
import ai.maum.data.edit.api.category.vo.ProjectClientCtgValue;
import ai.maum.data.edit.api.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final ProjectRepository projectRepository;

  public List<CategoryResponse> find(String projectId, String ctgName) {
    ProjectClientCtgValue clientCtg = projectRepository.getClientCtgIdByProjectId(new ObjectId(projectId));

    return categoryRepository.findByClientCtgIdAndCtgName(clientCtg.getClientCtgId(), ctgName);
  }

  public List<CategoryResponse> findChildCategory(String projectId, String parentCtgId) {
    ProjectClientCtgValue clientCtg = projectRepository.getClientCtgIdByProjectId(new ObjectId(projectId));
    
    return categoryRepository.findChildCategoryByParentCtgId(clientCtg.getClientCtgId(), new ObjectId(parentCtgId));
  }
}

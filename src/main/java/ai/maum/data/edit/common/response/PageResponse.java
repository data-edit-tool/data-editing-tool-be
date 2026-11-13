package ai.maum.data.edit.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

import ai.maum.data.edit.common.request.PageRequest;
import ai.maum.data.edit.common.vo.PageInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {

  private final List<T> list;
  private final PageInfo pageable;

  @Builder
  public PageResponse(PageRequest request, List<T> list, Long totalElements) {
    this.list = list;
    this.pageable = new PageInfo(request, totalElements);
  }
}
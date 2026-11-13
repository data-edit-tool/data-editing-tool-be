package ai.maum.data.edit.common.vo;

import com.fasterxml.jackson.annotation.JsonInclude;

import org.apache.commons.lang3.math.NumberUtils;

import ai.maum.data.edit.common.request.PageRequest;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageInfo {

  private Long totalElements;
  private Integer totalPages;
  private Integer currentPage;
  private Integer pageSize;
  private Boolean hasNext;
  private Boolean hasPrevious;
  private Boolean isFirst;
  private Boolean isLast;

  public PageInfo(PageRequest request, long totalElements) {
    if (totalElements < 1) {
      return;
    }

    this.totalElements = totalElements;
    int size = request.getSize();
    int page = request.getPage();

    this.currentPage = page;
    this.pageSize = size;
    this.hasNext = NumberUtils.compare(totalElements - ((long) size * page), 0) > 0;
    this.hasPrevious = NumberUtils.compare(page, 1) > 0;
    this.isFirst = NumberUtils.compare(page, 1) == 0;
    this.isLast = NumberUtils.compare(totalElements, (long) size * page) == 0 ||
        NumberUtils.compare(totalElements, (long) size * page) < 0;
    this.totalPages = (int) Math.ceil((totalElements * 1.0d) / (size * 1.d));
  }
}
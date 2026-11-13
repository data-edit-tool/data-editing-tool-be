package ai.maum.data.edit.common.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class PageRequest {

  private int page = 1;
  private int size = 10;
  private String sort;
  private String direction;

  public PageRequest(int page, int size) {
    this(page, size, null, null);
  }

  public PageRequest(int page, int size, String sort, String direction) {
    this.page = Math.max(page, 1);
    this.size = size < 1 ? 10 : size;
    this.sort = sort;
    this.direction = direction;
  }

  public int getOffset() {
    return getPage() > 0 ? (getPage() - 1) * getSize() : 0;
  }
}

package ai.maum.data.edit.common.domain;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class BaseField {

  @Field("created")
  @CreatedDate
  private LocalDateTime created;
  @Field("updated")
  @LastModifiedDate
  private LocalDateTime updated;
}

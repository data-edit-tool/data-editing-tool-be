package ai.maum.data.edit.common.config.serialization.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import ai.maum.data.edit.common.enums.DateMeta;

/**
 * LocalDateTime을 날짜 및 시간 형식의 문자열로 직렬화하는 Serializer
 */
public class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateMeta.DATE_TIME_FORMAT_NORMAL);

  @Override
  public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeString(value.format(formatter));
  }
}

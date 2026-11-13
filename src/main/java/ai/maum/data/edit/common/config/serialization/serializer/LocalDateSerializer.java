package ai.maum.data.edit.common.config.serialization.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import ai.maum.data.edit.common.enums.DateMeta;

/**
 * LocalDate를 날짜 형식의 문자열로 직렬화하는 Serializer
 */
public class LocalDateSerializer extends JsonSerializer<LocalDate> {
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DateMeta.DATE_FORMAT);

  @Override
  public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
    gen.writeString(value.format(formatter));
  }
}

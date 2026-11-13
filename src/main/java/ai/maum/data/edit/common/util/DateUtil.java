package ai.maum.data.edit.common.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import ai.maum.data.edit.common.enums.DateMeta;

public class DateUtil {
  private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DateMeta.DATE_FORMAT);
  private static final DateTimeFormatter dateTimeFormatter =
      DateTimeFormatter.ofPattern(DateMeta.DATE_TIME_FORMAT_NORMAL);

  private DateUtil() {
  }

  /**
   * 날짜 형식의 문자열을 LocalDate 객체로 변환한다.
   *
   * @param date 날짜(yyyy-MM-dd)
   * @return 입력한 날짜의 LocalDate 객체
   * @author baekgol
   */
  public static LocalDate convertToDateByString(String date) {
    return LocalDate.parse(date, dateFormatter);
  }

  /**
   * 날짜 및 시간 형식의 문자열을 LocalDateTime 객체로 변환한다.
   *
   * @param dateTime 날짜 및 시간(yyyy-MM-dd HH:mm:ss)
   * @return 입력한 날짜의 LocalDateTime 객체
   * @author baekgol
   */
  public static LocalDateTime convertToDateTimeByString(String dateTime) {
    return LocalDateTime.parse(dateTime, dateTimeFormatter);
  }

  /**
   * LocalDate 객체를 날짜 형식의 문자열로 변환한다.
   *
   * @param date 날짜
   * @return 날짜 형식의 문자열(yyyy-MM-dd)
   * @author baekgol
   */
  public static String convertToStringByDate(LocalDate date) {
    return date.format(dateTimeFormatter);
  }

  /**
   * LocalDateTime 객체를 날짜 및 시간 형식의 문자열로 변환한다.
   *
   * @param dateTime 날짜 및 시간
   * @return 날짜 및 시간 형식의 문자열(yyyy-MM-dd HH:mm:ss)
   * @author baekgol
   */
  public static String convertToStringByDateTime(LocalDateTime dateTime) {
    return dateTime.format(dateTimeFormatter);
  }

  /**
   * ms 단위 시간을 날짜 형식의 문자열로 변환한다.
   *
   * @param time ms 단위의 시간
   * @return 날짜 형식의 문자열(yyyy-MM-dd HH:mm:ss)
   * @author baekgol
   */
  public static String convertToStringByMs(long time) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()).format(dateTimeFormatter);
  }

  /**
   * 초 단위 시간을 시간 형식의 문자열로 변환한다.
   * ms 소수점 한 자리까지 표시한다.
   *
   * @param time 초 단위 시간
   * @return 시간 형식의 문자열(HH:mm:ss.S)
   * @author baekgol
   */
  public static String convertToStringBySecond(double time) {
    double target = time;
    int hour = 0;
    int minute = 0;
    double second = 0;
    int ms = 0;

    if (target >= 3600) {
      hour = (int) (target / 3600);
      target %= 3600;
    }

    if (target >= 60) {
      minute = (int) (target / 60);
      target %= 60;
    }

    second = target;
    ms = (int) ((second - (int) second) * 10);

    return String.format("%02d:%02d:%02d.%01d", hour, minute, (int) second, ms);
  }
}

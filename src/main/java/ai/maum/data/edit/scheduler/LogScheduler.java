package ai.maum.data.edit.scheduler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import ai.maum.data.edit.common.enums.LogLevelMeta;
import ai.maum.data.edit.common.enums.RegexMeta;
import ai.maum.data.edit.common.exception.BaseException;
import ai.maum.data.edit.common.util.LogUtil;
import ai.maum.data.edit.common.util.ZipUtil;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = {"service.log.path", "service.log.zip.path"})
public class LogScheduler {

  @Value("${service.batch.status}")
  private boolean status;

  @Value("${service.log.path}")
  private String logPath;

  @Value("${service.log.zip.path}")
  private String zipPath;

  /**
   * 매일 일별 로그 파일들을 압축한다.
   *
   * @author baekgol
   */
  @Scheduled(cron = "0 0 0 * * ?")
  public void schedule() {
    if (status) {
      try {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        ZipUtil.zip(logPath,
            RegexMeta.LOG_FILE_PREFIX
                + yesterday
                + RegexMeta.LOG_FILE_SUFFIX,
            zipPath + "/" + yesterday + ".zip",
            true);

        String msg = "["
            + yesterday
            + "]"
            + " 로그를 압축하였습니다.";

        LogUtil.notice(msg, LogLevelMeta.INFO);
      } catch (Exception e) {
        throw BaseException.of(e);
      }
    }
  }
}

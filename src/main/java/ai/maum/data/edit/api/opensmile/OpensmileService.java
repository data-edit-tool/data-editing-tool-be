package ai.maum.data.edit.api.opensmile;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.messages.ExecCreation;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ai.maum.data.edit.api.csv.CsvEntity;
import ai.maum.data.edit.api.csv.CsvRepository;
import ai.maum.data.edit.api.job.dto.response.JobFileResponse;
import ai.maum.data.edit.api.job.repository.JobFileRepository;
import ai.maum.data.edit.common.enums.ResponseCode;
import ai.maum.data.edit.common.exception.BaseException;
import ai.maum.data.edit.common.util.LogUtil;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OpensmileService {

  private final CsvRepository csvRepository;
  private final JobFileRepository jobFileRepository;

  private final OpensmileProperties properties;

  @Value("${service.file.path}")
  private String filePath;
  @Value("${service.file.csv-path}")
  private String csvFilePath;

  public void insertCsv(ObjectId workspaceId, ObjectId projectId, ObjectId jobId, ObjectId workId) {
    LogUtil.info(":::::::::::::::: Start to export csv ::::::::::::::::");

    String csvPath = properties.getCsvPath();

    try {
      DockerClient docker = DefaultDockerClient.fromEnv().build();

      String containerName = properties.getContainerName();

      // 추출해야 할 job file
      List<JobFileResponse> jobFileList = jobFileRepository.getFiles(workspaceId, projectId, jobId);
      boolean deleteCheck = false; // 추출 진행 후 csv 파일 삭제 여부

      for (JobFileResponse file : jobFileList) {
        ObjectId jobFileId = file.getFileId();
        String jobFileIdStr = jobFileId.toString();

        CsvEntity csv = csvRepository.findByJobIdAndJobFileId(jobId, new ObjectId(jobFileIdStr));
        if (csv == null) {
          LogUtil.info("CSV is null.");

          String csvOutputPath = csvPath + "/" + jobFileIdStr + ".csv";

          // docker 내부에서 실행할 명령어(= opensmile 실행 명령어)
          String command = new StringBuilder()
              .append(properties.getExecBin()) // opensmile 실행파일인 SMILExtract
              .append(" -C ")
              .append(properties.getConfigPath()) // config 파일 설정
              .append(" -I ")
              .append(
                  file.getFilePath().replaceAll(filePath, "/data") // wav 파일 path
              ) // input 파일(audio 파일) 경로 설정
              .append(" -csvoutput ")
              .append(csvOutputPath) // output file(csv file)
              .toString();

          // 새로운 실행 환경 생성
          ExecCreation opensmileExec = docker.execCreate(
              containerName,
              new String[]{"/bin/sh", "-c", command},
              DockerClient.ExecCreateParam.attachStdout(),
              DockerClient.ExecCreateParam.attachStderr()
          );

          LogUtil.info("Command Result: " + docker.execStart(opensmileExec.id()).readFully());

          // HOST에서 실행(csv파일 파싱 후 csv document에 insert)
          csvRepository.insert(
              parsingCsvFile(
                  jobId,
                  jobFileId,
                  workId,
                  file.getFileName().split("_")[3]
              )
          );
          deleteCheck = true;
        }
      }
      docker.close();

      if (deleteCheck) {
        // csv 파일 삭제하기
        docker = DefaultDockerClient.fromEnv().build();

        ExecCreation deleteCsvFile = docker.execCreate(
            containerName,
            new String[]{"/bin/sh", "-c", "rm -rf " + csvPath + "/*"},
            DockerClient.ExecCreateParam.attachStdout(),
            DockerClient.ExecCreateParam.attachStderr()
        );
        LogUtil.info("Command Result: " + docker.execStart(deleteCsvFile.id()).readFully());
        // Docker 호스트와의 연결 종료
        docker.close();
      }
      LogUtil.info(":::::::::::::::: End to export csv ::::::::::::::::");
    } catch (Exception e) {
      LogUtil.error(e);
      throw BaseException.of(ResponseCode.WK_COMPLETE_FAIL);
    }
  }

  private CsvEntity parsingCsvFile(ObjectId jobId, ObjectId jobFileId, ObjectId workId, String speakerId) {
    List<CsvEntity.Data> dataList = new ArrayList<>();

    try (
        Reader reader = new FileReader(csvFilePath + "/" + jobFileId + ".csv");
        CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT)
    ) {
      List<CSVRecord> records = csvParser.getRecords();
      Set<BigDecimal> check = new HashSet<>(); // time 중복 체크를 위한 변수

      for (int i = 1; i < records.size(); i++) { // csv 파일의 모든 행을 read함.
        CSVRecord csvRecord = records.get(i);
        String[] data = csvRecord.get(0).split(";");
        String pitch = data[3];
        String loudness = data[4];

        BigDecimal time =
            BigDecimal.valueOf(
                Math.round(
                    Double.valueOf(data[1].substring(0, 6)) * 1000
                )
            );

        if (!check.contains(time)) { // time(1초) 마다의 meta(pitch,loudness) 정보는 1개씩만 있어야 하기 때문에 이미 등록된 time이 있을 경우 건너뛰기.
          dataList.add(
              CsvEntity.Data.builder()
              .time(time.intValue())
              .pitch(pitch)
              .loudness(loudness)
              .build()
          );

          check.add(time);
        }
      }
    } catch (IOException e) {
      LogUtil.error(e);
      throw BaseException.of(ResponseCode.WK_COMPLETE_FAIL);
    }

    return CsvEntity.builder()
        .jobId(jobId)
        .jobFileId(jobFileId)
        .workId(workId)
        .speakerId(speakerId)
        .datas(dataList)
        .build();
  }
}

package ai.maum.data.edit.common.enums;

public class RegexMeta {
  public static final String LOG_FILE_PREFIX = ".*(\\/|\\\\)(info(\\/|\\\\)info|err(\\/|\\\\)err)_";
  public static final String LOG_FILE_SUFFIX = "_\\d*.log$";
  public static final String UNNECESSARY_PATHS = "/favicon.ico|/error";
  public static final String DOCUMENT_PATHS = "/doc|/swagger-ui/.*|/v3/api-docs.*";
  public static final String DOCUMENT_LOGIN_PATHS = "/doc/login(/.*)?";
  public static final String RESOURCE_PATHS = "/resource/.*";
  public static final String MULTIPART_DATE_FORMAT = "^\\[.*\\]$";
  public static final String ADMIN_ACCOUNT_MANAGEMENT_PATHS = "^/admin/user(|/delete)$";
}

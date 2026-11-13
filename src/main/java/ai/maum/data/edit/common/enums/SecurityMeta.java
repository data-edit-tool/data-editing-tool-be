package ai.maum.data.edit.common.enums;

public class SecurityMeta {
  public static final long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 7;
  public static final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 30;
  public static final String TOKEN_SECRET_KEY = "MindsLABMAUMAccountManagementSystemAMS1234567890";
  public static final String ENCODE_SALT = "";
  public static final String DOCUMENT_URL = "/doc";
  public static final String DOCUMENT_LOGIN_URL = "/doc/login/page";
  public static final int DOCUMENT_SESSION_EXPIRE_TIME = 60 * 60 * 3;
  public static final String DOCUMENT_NO_TOKEN = "no-token";
}

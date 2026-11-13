package ai.maum.data.edit.common.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.ServletRequestPathFilter;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import ai.maum.data.edit.api.execinfo.ExecInfoRepository;
import ai.maum.data.edit.api.project.repository.ProjectRepository;
import ai.maum.data.edit.api.user.repository.UserRepository;
import ai.maum.data.edit.api.workspace.repository.WorkspaceRepository;
import ai.maum.data.edit.common.config.interceptor.ExecInfoInterceptor;
import ai.maum.data.edit.common.enums.SecurityMeta;
import ai.maum.data.edit.common.exception.BaseException;
import ai.maum.data.edit.common.filter.AccessAuthorityFilter;
import ai.maum.data.edit.common.util.CryptoUtil;
import ai.maum.data.edit.common.util.LogUtil;
import ai.maum.data.edit.common.util.SystemUtil;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig implements WebMvcConfigurer {
  @Value("${service.cors.origins}")
  private List<String> origins;

  @Value("${service.cors.methods}")
  private List<String> methods;

  @Value("${service.cors.headers}")
  private List<String> headers;

  @Value("${service.cors.credentials}")
  private boolean credentials;

  private final Environment env;
  private final ApplicationContext context;
  private final UserRepository userRepository;
  private final WorkspaceRepository workspaceRepository;
  private final ProjectRepository projectRepository;
  private final ExecInfoRepository execInfoRepository;

  /**
   * 인터셉터를 설정한다.
   *
   * @param registry 인터셉터 등록 정보
   * @author baekgol
   */
  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new ExecInfoInterceptor(execInfoRepository))
        .excludePathPatterns(SecurityMeta.DOCUMENT_LOGIN_URL, "/swagger-ui/**", "/v3/api-docs/**");
  }

  /**
   * 자원 접근을 설정한다.
   *
   * @param registry 자원 등록 정보
   * @author baekgol
   */
  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/resource/**")
        .addResourceLocations("classpath:/static/");
  }

  /**
   * 사용자 인증에 필요한 AuthenticationManager를 등록한다.
   *
   * @return AuthenticationManager
   * @author baekgol
   */
  @Bean
  public AuthenticationManager authenticationManager() {
    DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

    PasswordEncoderAdapter passwordEncoder = (PasswordEncoderAdapter) CryptoUtil.getPasswordEncoder();
    if (!passwordEncoder.isAlive()) {
      SystemUtil.shutdownWithContext(context, BaseException.of(new NoSuchAlgorithmException()));
    }
    provider.setPasswordEncoder(passwordEncoder);

    provider.setUserDetailsService(new UserDetailsServiceAdapter(userRepository));

    return new ProviderManager(provider);
  }

  /**
   * CORS 설정에 필요한 CorsConfigurationSource를 등록한다.
   *
   * @return CorsConfigurationSource
   * @author baekgol
   */
  @Bean
  public CorsConfigurationSource configurationSource() {
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    CorsConfiguration configuration = new CorsConfiguration();
    LogUtil.info(origins);
    LogUtil.info(methods);
    LogUtil.info(headers);
    LogUtil.info(credentials);
    configuration.setAllowedOrigins(origins);
    configuration.setAllowedMethods(methods);
    configuration.setAllowedHeaders(headers);
    configuration.setAllowCredentials(credentials);
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  /**
   * 애플리케이션 전반적인 보안 설정을 적용한다.
   * 특정 URL에 대해 특정 권한만 접근 가능하게 하려면 hasRole()을 이용하여 설정한다.
   * 단, API 접근 권한에 대해서는 해당 컨트롤러에 @Secured나 @PreAuthorize를 이용하여 설정한다.
   *
   * @param http 보안 설정 정보
   * @return SecurityFilterChain
   * @author baekgol
   */
  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    AuthenticationEntryPoint authenticationEntryPoint = new AuthenticationEntryPointAdapter();
    ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry urlRegistry = http
        .csrf().disable()
        .cors()
        .configurationSource(configurationSource())
        .and()
        .authorizeRequests()
        .antMatchers(SecurityMeta.DOCUMENT_LOGIN_URL, "/resource/**")
        .permitAll()
        .antMatchers("/favicon.ico", "/error")
        .denyAll();

    ExpressionUrlAuthorizationConfigurer<HttpSecurity>.AuthorizedUrl docUrlConfig = urlRegistry
        .antMatchers("/doc/**", "/swagger-ui/**", "/v3/api-docs/**");

    if (Arrays.asList(env.getActiveProfiles()).contains("prod")) {
      urlRegistry = docUrlConfig.denyAll();
    } else {
      urlRegistry = docUrlConfig.permitAll();
    }

    return urlRegistry
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(authenticationEntryPoint)
        .accessDeniedHandler(new AccessDeniedHandlerAdapter())
        .and()
        .addFilterBefore(new JwtFilter(context.getBean(RequestMappingHandlerMapping.class),
                userRepository,
                authenticationEntryPoint
            ),
            UsernamePasswordAuthenticationFilter.class)
        .addFilterBefore(new ServletRequestPathFilter(), JwtFilter.class)
        .addFilterAfter(new AccessAuthorityFilter(workspaceRepository, projectRepository), JwtFilter.class)
        .build();
  }
}

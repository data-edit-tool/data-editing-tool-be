package ai.maum.data.edit.common.config.security;

import org.bson.types.ObjectId;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import ai.maum.data.edit.api.role.RoleType;
import ai.maum.data.edit.api.user.UserEntity;
import ai.maum.data.edit.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 인증 시 사용자 정보를 조회하는 서비스
 */
@RequiredArgsConstructor
public class UserDetailsServiceAdapter implements UserDetailsService {

  private final UserRepository userRepository;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    UserEntity user = userRepository.findByUserId(new ObjectId(username));

    if (user == null) {
      throw new UsernameNotFoundException("User not found: " + username);
    }

    return UserInfo.builder()
        .userId(user.getUserId())
        .password(user.getPassword())
        .clientId(user.getClientId())
        .role(user.getIsAdmin() ? RoleType.ADMIN : RoleType.USER)
        .build();
  }
}

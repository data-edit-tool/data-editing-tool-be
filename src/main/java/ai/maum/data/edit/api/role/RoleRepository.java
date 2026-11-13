package ai.maum.data.edit.api.role;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RoleRepository extends MongoRepository<RoleEntity, String> {

  List<RoleEntity> findByIdIn(List<String> roles);
}

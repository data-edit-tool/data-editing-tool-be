package ai.maum.data.edit.api.execinfo;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExecInfoRepository extends MongoRepository<ExecInfoEntity, ObjectId> {
}

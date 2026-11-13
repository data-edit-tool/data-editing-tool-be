package ai.maum.data.edit.api.work.service;

import org.bson.types.ObjectId;

import ai.maum.data.edit.api.user.enums.UserType;
import ai.maum.data.edit.api.work.dto.request.SaveLabelingRequest;

public interface LabelingService {

  Object saveLabeling(ObjectId userId, UserType userType, SaveLabelingRequest request, String workStatus);
}

package vn.ezisolutions.cloud.facebook_service.services.facebook.publishing.strategy;

import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPost;
import vn.ezisolutions.cloud.facebook_service.entity.facebook.FbPostMedia;

import java.util.List;

public interface IFbPublishStrategy {
    String publish(FbPost post, List<FbPostMedia> mediaList, String token, String fbPageId) throws CustomException;
    String update(FbPost post, List<FbPostMedia> mediaList, String token) throws CustomException;
}

package vn.ezisolutions.cloud.facebook_service.core.utils;

import vn.ezisolutions.cloud.facebook_service.core.BaseResponse;

public class HttpResponseUtils {
    public static BaseResponse errorClient(String message) {
        return BaseResponse.builder()
                .message(message)
                .status(400)
                .build();
    }
}

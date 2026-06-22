package vn.ezisolutions.cloud.facebook_service.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class FbAdsBatchResponse {
    private int code;
    private List<Object> headers;
    private String body;
}

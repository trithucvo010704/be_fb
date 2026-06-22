package vn.ezisolutions.cloud.facebook_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FbAdsBatchRequest {
    private String method;
    private String relative_url;
    private String body;
}

package vn.ezisolutions.cloud.facebook_service.controllers.facebook;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.ezisolutions.cloud.facebook_service.core.BaseResponse;

@RestController
public class PolicyController {

    @GetMapping("/privacy-policy")
    public BaseResponse privacyPolicy() {
        return BaseResponse.success("Privacy Policy page for Central Facebook Service");
    }

    @GetMapping("/data-deletion")
    public BaseResponse dataDeletion() {
        return BaseResponse.success("Data Deletion instructions for Facebook Page data");
    }
}

package vn.ezisolutions.cloud.facebook_service.dto.response;

import java.util.List;

public record FacebookOAuthExchangeResponse(
        String code,
        String state,
        String error,
        String errorDescription,
        Boolean exchanged,
        Boolean connected,
        List<FacebookConnectedPageResponse> pages
) {
    public static FacebookOAuthExchangeResponse error(
            String code,
            String state,
            String error,
            String errorDescription
    ) {
        return new FacebookOAuthExchangeResponse(code, state, error, errorDescription, false, false, List.of());
    }
}


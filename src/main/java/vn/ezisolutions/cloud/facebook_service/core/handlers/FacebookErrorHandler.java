package vn.ezisolutions.cloud.facebook_service.core.handlers;

import com.facebook.ads.sdk.APIException;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.FacebookApiException;

import java.util.concurrent.Callable;

@Service
@RequiredArgsConstructor
public class FacebookErrorHandler {
    private static final Logger log = LoggerFactory.getLogger(FacebookErrorHandler.class);
    private static final int MAX_RETRY = 3;

    public <T> T execute(Callable<T> sdkCall, String operation, String context) {
        try {
            return sdkCall.call();
        } catch (Exception ex) {
            throw buildAndLog(ex, operation, context);
        }
    }

    public void executeVoid(FacebookVoidCall sdkCall, String operation, String context) {
        execute(() -> {
            sdkCall.execute();
            return null;
        }, operation, context);
    }

    public <T> T executeWithRetry(Callable<T> sdkCall, String operation, String context) {
        int attempt = 0;
        while (true) {
            try {
                return sdkCall.call();
            } catch (Exception ex) {
                FacebookApiException fae = buildAndLog(ex, operation, context);
                if (fae.isRetryable() && attempt < MAX_RETRY) {
                    long backoff = Math.min(fae.getRetryAfterMs() * (1L << attempt), 600_000L);
                    log.warn("[FB-RETRY] operation={} context={} attempt={}/{} backoff={}s",
                            operation, context, attempt + 1, MAX_RETRY, backoff / 1000);
                    sleep(backoff);
                    attempt++;
                } else {
                    throw fae;
                }
            }
        }
    }

    private FacebookApiException buildAndLog(Exception ex, String operation, String context) {
        if (!(ex instanceof APIException apiEx)) {
            log.error("[FB-SYSTEM-ERR] operation={} context={} msg={}",
                    operation, context, ex.getMessage(), ex);
            return new FacebookApiException(-1, 0, ex.getMessage(), null);
        }

        ParsedFbError parsed = parseError(apiEx);

        FacebookApiException fae = new FacebookApiException(
                parsed.code, parsed.subcode, parsed.message, parsed.traceId);

        log.warn("[FB-API-ERR] operation={} context={} code={} subcode={} category={} retryable={} traceId={} msg={}",
                operation, context,
                parsed.code, parsed.subcode,
                fae.getCategory(), fae.isRetryable(), parsed.traceId, parsed.message);

        return fae;
    }

    private ParsedFbError parseError(APIException apiEx) {
        try {
            JsonObject root  = apiEx.getRawResponseAsJsonObject();
            JsonObject error = root.getAsJsonObject("error");

            if (error != null) {
                int    code    = error.has("code")            ? error.get("code").getAsInt()            : -1;
                int    subcode = error.has("error_subcode")   ? error.get("error_subcode").getAsInt()   : 0;
                String message = error.has("message")         ? error.get("message").getAsString()      : apiEx.getMessage();
                String traceId = error.has("fbtrace_id")      ? error.get("fbtrace_id").getAsString()   : null;
                return new ParsedFbError(code, subcode, message, traceId);
            }
        } catch (Exception ignored) {
            // Ignored because we fall back to generic error properties in the return statement below.
        }

        return new ParsedFbError(-1, 0, apiEx.getMessage(), null);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private record ParsedFbError(int code, int subcode, String message, String traceId) {}
    
    @FunctionalInterface
    public interface FacebookVoidCall {
        void execute() throws APIException;
    }
}

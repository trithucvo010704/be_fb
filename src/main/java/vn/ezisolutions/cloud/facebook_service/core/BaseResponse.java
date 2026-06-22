package vn.ezisolutions.cloud.facebook_service.core;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@Getter
@Setter
public class BaseResponse implements Serializable {
    private Integer status;
    private String message;

    @JsonAlias({"data", "payload"})
    private Object payload;

    public BaseResponse() {
        this.status = 1;
        this.message = "success";
        this.payload = null;
    }

    public <T> BaseResponse(T payload) {
        this.status = 1;
        this.message = "success";
        this.payload = payload;
    }

    public <T> BaseResponse(List<T> payload) {
        this.status = 1;
        this.message = "success";
        this.payload = payload;
    }

    public <T> BaseResponse(Page<T> payload) {
        this.status = 1;
        this.message = "success";
        this.payload = new BasePagination<T>(payload);
    }

    public static <T> BaseResponse success(String message, T payload) {
        return BaseResponse.builder().status(1).message(message).payload(payload).build();
    }

    public static <T> BaseResponse success(T payload) {
        return BaseResponse.builder().status(1).message("success").payload(payload).build();
    }

    public static <T> BaseResponse success(String message) {
        return BaseResponse.builder().status(1).message(message).payload(null).build();
    }


    public static <T> BaseResponse fail(String message, T payload) {
        return BaseResponse.builder().status(0).message(message).payload(payload).build();
    }

    public static <T> BaseResponse fail(T payload) {
        return BaseResponse.builder().status(0).message("failed").payload(payload).build();
    }

    public static <T> BaseResponse fail(String message) {
        return BaseResponse.builder().status(0).message(message).payload(null).build();
    }

}

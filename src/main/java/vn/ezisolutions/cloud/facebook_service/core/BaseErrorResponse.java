package vn.ezisolutions.cloud.facebook_service.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter

@AllArgsConstructor
public class BaseErrorResponse<T> implements Serializable {
    private int status;
    private String message;
    private Map<String, String> errors;
}

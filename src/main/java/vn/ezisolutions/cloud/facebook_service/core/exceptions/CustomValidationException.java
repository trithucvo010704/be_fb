package vn.ezisolutions.cloud.facebook_service.core.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.validation.ObjectError;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CustomValidationException extends RuntimeException {
    private String message;
    private List<ObjectError> errors;

    public CustomValidationException(List<ObjectError> errors) {
        super();
        this.message = "dữ liệu truyền vào không hợp lệ";
        this.errors = errors;
    }

}

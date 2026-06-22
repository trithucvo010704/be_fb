package vn.ezisolutions.cloud.facebook_service.core.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import vn.ezisolutions.cloud.facebook_service.core.BaseErrorResponse;
import vn.ezisolutions.cloud.facebook_service.core.BaseResponse;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomException;
import vn.ezisolutions.cloud.facebook_service.core.exceptions.CustomValidationException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Object> handleAccessDeniedException(Exception ex, WebRequest request) {
        logger.error("Exception: ", ex);
        return new ResponseEntity<>(new BaseResponse(0, ex.getMessage(), null), new HttpHeaders(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({UsernameNotFoundException.class})
    public ResponseEntity<Object> handleUsernameNotFoundExceptionException(Exception ex, WebRequest request) {
        logger.error("UsernameNotFoundException: ", ex);
        return new ResponseEntity<>(new BaseResponse(0, ex.getMessage(), null), new HttpHeaders(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler({CustomException.class})
    public ResponseEntity<Object> handleAccessDeniedException(CustomException ex, WebRequest request) {
        logger.error("CustomException: ", ex);
        return new ResponseEntity<>(new BaseResponse(0, ex.getMessage(), null), new HttpHeaders(), HttpStatus.valueOf(ex.getStatusCode()));
    }

    @ExceptionHandler({CustomValidationException.class})
    public ResponseEntity<Object> handleValidationException(CustomValidationException ex, WebRequest request) {
        logger.error("CustomValidationException: ", ex);
        Map<String, String> errors = new HashMap<>();
        if (ex.getErrors() != null) {
            ex.getErrors().forEach((error) -> {
                String fieldName = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });
        }
        return new ResponseEntity<>(new BaseErrorResponse<>(0, ex.getMessage(), errors), new HttpHeaders(), HttpStatus.BAD_REQUEST);
    }


    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        logger.error("MethodArgumentNotValidException: ", ex);
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(new BaseErrorResponse<>(0, "Dữ liệu không hợp lệ", errors), headers, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({RequestRejectedException.class})
    public ResponseEntity<Object> handleRequestRejectedException(RequestRejectedException ex, WebRequest request) {
        logger.error("RequestRejectedException: ", ex);
        return new ResponseEntity<>(new BaseErrorResponse<>(0, ex.getMessage(), null), new HttpHeaders(), HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        logger.error("HttpMessageNotReadableException: ", ex);
        String message = "Dữ liệu gửi lên không hợp lệ";
        Map<String, String> errors = new HashMap<>();
        Throwable cause = ex.getCause();

        if (cause instanceof com.fasterxml.jackson.databind.exc.InvalidFormatException ife) {
            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                Object[] enumConstants = ife.getTargetType().getEnumConstants();
                StringBuilder validValues = new StringBuilder();
                for (int i = 0; i < enumConstants.length; i++) {
                    if (i > 0) validValues.append(", ");
                    validValues.append(enumConstants[i].toString());
                }
                message = "Giá trị '" + ife.getValue() + "' không hợp lệ. Các giá trị hợp lệ: [" + validValues + "]";
            }
            String fieldName = extractFieldName(ife);
            errors.put(fieldName, message);
        } else if (cause instanceof com.fasterxml.jackson.databind.exc.MismatchedInputException mie) {
            String fieldName = extractFieldName(mie);
            message = "Trường '" + fieldName + "' phải là chuỗi ký tự (string), không được truyền số";
            errors.put(fieldName, message);
        } else {
            errors.put("body", message);
        }

        return new ResponseEntity<>(new BaseErrorResponse<>(0, message, errors), headers, HttpStatus.BAD_REQUEST);
    }

    private String extractFieldName(com.fasterxml.jackson.databind.JsonMappingException ex) {
        if (ex.getPath() != null && !ex.getPath().isEmpty()) {
            StringBuilder path = new StringBuilder();
            for (int i = 0; i < ex.getPath().size(); i++) {
                if (i > 0) path.append(".");
                path.append(ex.getPath().get(i).getFieldName());
            }
            return path.toString();
        }
        return "body";
    }
}
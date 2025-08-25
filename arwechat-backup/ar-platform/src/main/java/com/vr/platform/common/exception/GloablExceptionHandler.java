package com.vr.platform.common.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vr.platform.common.bean.response.BizReturnCode;
import com.vr.platform.common.bean.response.ResponseFormat;
import com.vr.platform.common.bean.response.ReturnCode;
import com.vr.platform.common.exception.BaseException;
import com.vr.platform.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.security.spec.InvalidKeySpecException;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @author zhangchenyang
 * @date 2022/1/14 0014
 */
@ControllerAdvice
@Slf4j
public class GloablExceptionHandler {



    @ExceptionHandler(BizException.class)
    @ResponseBody
    public ResponseFormat exception(BizException e){
        log.error(e.getMessage(), e);
        return ResponseFormat.fail(e.getReturnCode(), e.getMessage());
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    @ResponseBody
    public ResponseFormat sqlIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException e){
        log.error(e.getMessage(), e);
        return ResponseFormat.fail(BizReturnCode.BIZ_SQL_INTEGRITY_CONSTRAINT_VIOLATION);
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    @ResponseBody
    public ResponseFormat InternalAuthenticationServiceException(InternalAuthenticationServiceException e){
        log.error(e.getMessage(), e);
        return ResponseFormat.fail(ReturnCode.UNI_SYSTEM_USER_PWD_ERROR);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public ResponseFormat uploadException(MaxUploadSizeExceededException e){
        log.error(e.getMessage(), e);
        return ResponseFormat.fail(BizReturnCode.BIZ_FILE_UPLOAD_SIZE_EXCEEDS_LIMIT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseFormat illegalArgumentException(IllegalArgumentException e){
        return generateResponse(e);
    }

    @ExceptionHandler(JsonProcessingException.class)
    @ResponseBody
    public ResponseFormat jsonProcessingException(JsonProcessingException e){
        return generateResponse(e);
    }

    @ExceptionHandler(InvalidKeySpecException.class)
    @ResponseBody
    public ResponseFormat invalidKeySpecException(InvalidKeySpecException e){
        log.error(e.getMessage(), e);
        return ResponseFormat.fail(ReturnCode.UNI_REQUEST_FAILED);
    }

    @ExceptionHandler(WxErrorException.class)
    @ResponseBody
    public ResponseFormat WxErrorException(WxErrorException e){
        log.error(e.getMessage(), e);
        return ResponseFormat.fail(BizReturnCode.BIZ_COLLECTION_APP_ADD_ERROR, e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseBody
    public ResponseFormat methodArgumentNotValidException(MethodArgumentNotValidException e){
        StringBuilder stringBuilder = new StringBuilder("Validation failed for arguments: ");
        BindingResult bindingResult = e.getBindingResult();
        for (ObjectError error : bindingResult.getAllErrors()) {
            if (error instanceof FieldError){
                FieldError fieldError = (FieldError)error;
                stringBuilder.append("[").append(fieldError.getField()).append(" : ").append(error.getDefaultMessage()).append("] ");
            }
        }
        log.error(e.getMessage(), e);
        log.error(stringBuilder.toString());
        return ResponseFormat.fail(ReturnCode.UNI_PARAMTER_FAILED);
    }

    private ResponseFormat generateResponse(Exception e){
        log.error(e.getMessage(),e);
        if (e instanceof BaseException){
            return ResponseFormat.fail(((BaseException)e).getReturnCode());
        }else {
            ResponseFormat<Object> fail = ResponseFormat.fail();
            if (e.getCause() != null){
                fail.setData(e.getCause().getMessage());
                return fail;
            }else {
                fail.setData(e.getMessage());
                return fail;
            }
        }
    }
}

package org.example.clearsolutionstest.controller;

import lombok.Generated;
import lombok.RequiredArgsConstructor;
import org.example.clearsolutionstest.service.TimeService;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Generated
@RequiredArgsConstructor
public class RestControllerExceptionHandler {

    private final TimeService timeService;

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleConstraintViolationException(jakarta.validation.ConstraintViolationException e) {
        return ExceptionResponse.builder()
                .withMessage(e.getMessage())
                .withHttpStatus(HttpStatus.BAD_REQUEST)
                .withDate(timeService.utcNow())
                .build();
    }

    @ExceptionHandler(ConversionFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleConversionFailedException(
            ConversionFailedException e) {
        return ExceptionResponse.builder()
                .withMessage(e.getMessage())
                .withHttpStatus(HttpStatus.BAD_REQUEST)
                .withDate(timeService.utcNow())
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {
        return ExceptionResponse.builder()
                .withMessage(e.getMessage())
                .withHttpStatus(HttpStatus.BAD_REQUEST)
                .withDate(timeService.utcNow())
                .build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionResponse handleIllegalArgumentExceptionException(
            IllegalArgumentException e) {
        return ExceptionResponse.builder()
                .withMessage(e.getMessage())
                .withHttpStatus(HttpStatus.BAD_REQUEST)
                .withDate(timeService.utcNow())
                .build();
    }

}

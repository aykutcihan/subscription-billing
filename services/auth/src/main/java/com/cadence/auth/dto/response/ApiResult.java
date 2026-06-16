package com.cadence.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> {

    private T data;
    private String message;

    public static <T> ApiResult<T> success(T data, String message) {
        return ApiResult.<T>builder().data(data).message(message).build();
    }

    public static ApiResult<Void> error(String message) {
        return ApiResult.<Void>builder().message(message).build();
    }
}

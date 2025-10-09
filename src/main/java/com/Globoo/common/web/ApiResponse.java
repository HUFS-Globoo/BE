package com.Globoo.common.web;


public class ApiResponse<T> {
    public boolean success; public T data; public String error;
    private ApiResponse(boolean s, T d, String e){ this.success=s; this.data=d; this.error=e; }
    public static <T> ApiResponse<T> ok(T d){ return new ApiResponse<>(true, d, null); }
    public static <T> ApiResponse<T> ok(){ return ok(null); }
    public static <T> ApiResponse<T> fail(String e){ return new ApiResponse<>(false, null, e); }
}

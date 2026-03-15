package cn.bg7qvu.mtrwebctc.model;

import java.util.*;

/**
 * 通用 API 响应模型
 */
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String error;
    private String errorCode;
    private long timestamp;
    
    public ApiResponse() {
        this.timestamp = System.currentTimeMillis();
    }
    
    // 静态工厂方法
    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        return response;
    }
    
    public static <T> ApiResponse<T> success() {
        return success(null);
    }
    
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.errorCode = errorCode;
        response.error = message;
        return response;
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return error("ERROR", message);
    }
    
    public static <T> ApiResponse<T> notFound(String resource) {
        return error("NOT_FOUND", resource + " not found");
    }
    
    public static <T> ApiResponse<T> unauthorized() {
        return error("UNAUTHORIZED", "Authentication required");
    }
    
    public static <T> ApiResponse<T> forbidden() {
        return error("FORBIDDEN", "Access denied");
    }
    
    public static <T> ApiResponse<T> badRequest(String message) {
        return error("BAD_REQUEST", message);
    }
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    
    /**
     * 分页响应
     */
    public static class Paged<T> {
        private List<T> items;
        private int total;
        private int page;
        private int pageSize;
        private int totalPages;
        
        public Paged(List<T> items, int total, int page, int pageSize) {
            this.items = items;
            this.total = total;
            this.page = page;
            this.pageSize = pageSize;
            this.totalPages = (int) Math.ceil((double) total / pageSize);
        }
        
        public List<T> getItems() { return items; }
        public int getTotal() { return total; }
        public int getPage() { return page; }
        public int getPageSize() { return pageSize; }
        public int getTotalPages() { return totalPages; }
        public boolean hasNext() { return page < totalPages; }
        public boolean hasPrevious() { return page > 1; }
    }
    
    /**
     * 列表响应
     */
    public static class ListResponse<T> {
        private List<T> items;
        private int count;
        
        public ListResponse(List<T> items) {
            this.items = items;
            this.count = items != null ? items.size() : 0;
        }
        
        public List<T> getItems() { return items; }
        public int getCount() { return count; }
    }
}

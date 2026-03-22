package fit.iuh.se.dtos;

public class ApiResponse {
    private boolean status;      // true = thành công, false = thất bại
    private String message;      // thông báo
    private String accessToken;  // chỉ có khi login thành công
    private String role;
    public ApiResponse() {}

    public ApiResponse(boolean status, String message) {
        this.status = status;
        this.message = message;
    }

    public ApiResponse(boolean status, String message, String accessToken, String role) {
        this.status = status;
        this.message = message;
        this.accessToken = accessToken;
        this.role = role;
    }

    // getters & setters
    public boolean isStatus() { return status; }
    public void setStatus(boolean status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

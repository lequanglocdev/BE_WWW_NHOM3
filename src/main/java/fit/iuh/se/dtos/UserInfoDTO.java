package fit.iuh.se.dtos;

public class UserInfoDTO {
    private String email;
    private String role;

    public UserInfoDTO(String email, String role) {
        this.email = email;
        this.role = role;
    }

    public String getEmail() { return email; }
    public String getRole() { return role; }
}
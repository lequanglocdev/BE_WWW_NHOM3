package fit.iuh.se.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Id;
@Entity
@Table(name = "user_accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String email;
    @JsonIgnore
    private String password;

    private String fullName;
    private String phone;
    private String address;

    private Boolean isAdmin = false;
    private Boolean isVerified = false;
    private Boolean isActive = true;

}

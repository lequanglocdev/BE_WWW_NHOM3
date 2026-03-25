package fit.iuh.se.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CloudinaryConfig {

    @Bean
    public Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "djha8h5mc",
                "api_key", "546281884979192",
                "api_secret", "Hm55-1HPrll117OqnyOhsaHRzOY",
                "secure", true));
    }
}

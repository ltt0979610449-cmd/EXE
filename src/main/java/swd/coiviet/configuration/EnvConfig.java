package swd.coiviet.configuration;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;



@Configuration
public class EnvConfig {

    @PostConstruct
    public void loadEnvVariables() {
        // Load file .env và set system properties
        Dotenv dotenv = Dotenv.configure()
                .directory("./")  // Đường dẫn đến file .env
                .filename(".env") // Tên file
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        // Set tất cả biến từ .env vào system properties
        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
        });
    }
}

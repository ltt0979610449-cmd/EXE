package swd.coiviet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CoivietApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoivietApplication.class, args);
    }

}

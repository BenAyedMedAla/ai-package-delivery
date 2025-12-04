package code;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class AiPackageDeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiPackageDeliveryApplication.class, args);
    }
}
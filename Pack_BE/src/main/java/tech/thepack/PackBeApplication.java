package tech.thepack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class PackBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(PackBeApplication.class, args);
    }

}

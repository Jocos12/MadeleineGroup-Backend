package rw.madeleinegroup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MadeleineGroupApplication {

    public static void main(String[] args) {
        SpringApplication.run(MadeleineGroupApplication.class, args);
    }
}

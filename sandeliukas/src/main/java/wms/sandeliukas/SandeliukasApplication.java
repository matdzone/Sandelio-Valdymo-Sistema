package wms.sandeliukas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SandeliukasApplication {

	public static void main(String[] args) {
		SpringApplication.run(SandeliukasApplication.class, args);
	}

}

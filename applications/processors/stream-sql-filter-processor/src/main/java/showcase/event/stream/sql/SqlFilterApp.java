package showcase.event.stream.sql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SqlFilterApp {

	public static void main(String[] args) {

		System.out.println("ENV="+System.getenv()+ " PROPERTIES"+System.getProperties());

		SpringApplication.run(SqlFilterApp.class, args);
	}

}

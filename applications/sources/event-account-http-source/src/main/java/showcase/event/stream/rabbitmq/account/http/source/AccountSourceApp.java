package showcase.event.stream.rabbitmq.account.http.source;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AccountSourceApp {

	public static void main(String[] args) {

		System.out.println("ENV="+System.getenv()+ " PROPERTIES"+System.getProperties());

		SpringApplication.run(AccountSourceApp.class, args);
	}

}

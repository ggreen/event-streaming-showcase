package showcase.streaming.event.rabbitmq.streaming.account

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AccountGeneratorSourceApp

fun main(args: Array<String>) {
	runApplication<AccountGeneratorSourceApp>(*args)
}

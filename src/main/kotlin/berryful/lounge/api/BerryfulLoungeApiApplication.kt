package berryful.lounge.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync

@EnableCaching
@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@ConfigurationPropertiesScan("berryful.lounge.api")
class BerryfulLoungeApiApplication

fun main(args: Array<String>) {
	runApplication<BerryfulLoungeApiApplication>(*args)
}
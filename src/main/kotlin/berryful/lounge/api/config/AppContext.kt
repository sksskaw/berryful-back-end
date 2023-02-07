package berryful.lounge.api.config

import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class AppContext {
    @Bean
    fun restTemplate(): RestTemplate = RestTemplateBuilder().build()
}
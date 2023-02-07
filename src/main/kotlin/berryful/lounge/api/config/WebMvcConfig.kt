package berryful.lounge.api.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfig : WebMvcConfigurer {
        @Bean
        fun jsonEscapeConverter(): MappingJackson2HttpMessageConverter {
                val objectMapper = Jackson2ObjectMapperBuilder.json().build<ObjectMapper>()
                objectMapper.factory.characterEscapes = HTMLCharacterEscapes()
                return MappingJackson2HttpMessageConverter(objectMapper)
            }
    }
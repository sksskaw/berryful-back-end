package berryful.lounge.api.externalApi.apistore

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "apistore")
data class ApiStoreProps(
    override val apiEndpoint: String,
    override val appId: String,
    override val apiKey: String,
    override val smsSendPhone: String,
    override val alimSendPhone: String
) : ApiStoreAttributes
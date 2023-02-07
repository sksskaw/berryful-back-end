package berryful.lounge.api.externalApi.apistore

interface ApiStoreAttributes {
    val apiEndpoint: String
    val appId: String
    val apiKey: String
    val smsSendPhone: String
    val alimSendPhone: String
}
package berryful.lounge.api.externalApi.awsApiGateway

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "aws-api-gateway")
data class AwsApiGatewayProps(
    override val berryfulEncodingEndpoint: String,
    override val BucketFrom: String,
    override val BucketTo: String,
    override val deleteS3ResourceEndpoint: String,
    override val clipCoverBucketName: String,
    override val profilesBucketName: String,
    override val clipEncodedBucketName: String,
) : AwsApiGatewayAttributes
package berryful.lounge.api.externalApi.awsApiGateway

interface AwsApiGatewayAttributes {
    val berryfulEncodingEndpoint: String
    val BucketFrom: String
    val BucketTo: String

    val deleteS3ResourceEndpoint: String
    val clipCoverBucketName: String
    val profilesBucketName: String
    val clipEncodedBucketName: String
}
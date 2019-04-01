package org.christchurchmayfair.api.sermons;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.entity.ContentType;
import org.christchurchmayfair.api.sermons.persistence.SermonDataStore;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler for requests to Lambda function.
 */
public class GetSignedSermonUploadURL extends SermonDataFunction implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private String bucketName;
  private String clientRegion;
  private ObjectMapper objectMapper = new ObjectMapper();
  private String targetPath;

  public GetSignedSermonUploadURL() {
    super();

    bucketName = System.getenv("UPLOAD_BUCKET_NAME");
    clientRegion = System.getenv("UPLOAD_BUCKET_REGION");
    targetPath = System.getenv("UPLOAD_TARGET_PATH");
  }

  public GetSignedSermonUploadURL(SermonDataStore sermons) {
    super(sermons);
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

    APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
    Map<String, String> headers = new HashMap<>();


//    Sermon sermon = null;
//    if (input.getIsBase64Encoded()) {
//      final byte[] bytes = Base64.getDecoder().decode(input.getBody());
//      try {
//        sermon = objectMapper.readValue(bytes, Sermon.class);
//      } catch (JsonParseException e) {
//        e.printStackTrace();
//      } catch (JsonMappingException e) {
//        e.printStackTrace();
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//    } else {
//      try {
//        sermon = objectMapper.readValue(input.getBody(), Sermon.class);
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//    }

    //Format is YYYY_MM_DD_SERVICE_PASSAGE_SPEAKER.mp3

    String objectKey = targetPath + "/" + input.getBody();

    try {
      AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
          .withCredentials(new EnvironmentVariableCredentialsProvider())
          .withRegion(clientRegion)
          .withPathStyleAccessEnabled(true)
          .build();

      // Set the pre-signed URL to expire after one hour.
      java.util.Date expiration = new java.util.Date();
      int expTimeInMinutes = 5;
      long expTimeMillis = expiration.getTime();
      expTimeMillis += 1000 * 60 * expTimeInMinutes;
      expiration.setTime(expTimeMillis);

      // Generate the pre-signed URL.
      GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectKey)
          .withContentType("audio/mpeg")
          .withMethod(HttpMethod.PUT)
          .withExpiration(expiration);
      URL url = s3Client.generatePresignedUrl(generatePresignedUrlRequest);

      // Create the connection and use it to upload the new object using the pre-signed URL.
      response.setBody("{\"signedUploadUrl\":\"" + url.toString() + "\"}");
      headers.put("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
      response.setStatusCode(200);
    } catch (AmazonServiceException e) {
      // The call was transmitted successfully, but Amazon S3 couldn't process
      // it, so it returned an error response.
      e.printStackTrace();
      response.setStatusCode(500);
      response.setBody(e.getMessage());
      return response;
    } catch (SdkClientException e) {
      // Amazon S3 couldn't be contacted for a response, or the client
      // couldn't parse the response from Amazon S3.
      e.printStackTrace();
      response.setStatusCode(500);
      response.setBody(e.getMessage());
      return response;
    }

    response.setHeaders(headers);
    return response;
  }
}

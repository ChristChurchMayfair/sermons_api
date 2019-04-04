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
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.DecryptionFailureException;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InternalServiceErrorException;
import com.amazonaws.services.secretsmanager.model.InvalidParameterException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.entity.ContentType;
import org.christchurchmayfair.api.sermons.model.GithubAccessTokenResponse;
import org.christchurchmayfair.api.sermons.model.LoginRequest;
import org.christchurchmayfair.api.sermons.model.Sermon;
import org.christchurchmayfair.api.sermons.persistence.SermonDataStore;

import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Handler for requests to Lambda function.
 */
public class GithubLogin implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  protected ObjectMapper objectMapper;
  protected Map<String, String> responseHeaders = new HashMap<>();
  protected APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
  private String githubOAuthClientSecret;
  private String clientId = System.getenv("GITHUB_CLIENT_ID");

  public GithubLogin() {
    super();

    String secretName = System.getenv("GITHUB_OAUTH_SECRET_NAME");
    String region = System.getenv("GITHUB_OAUTH_SECRET_REGION");

    AWSSecretsManager awsSecretsManager = AWSSecretsManagerClientBuilder.standard()
        .withRegion(region)
        .build();

    String secretPayload = "";
    String decodedBinarySecret = "";
    GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
        .withSecretId(secretName);
    GetSecretValueResult getSecretValueResult = null;

    try {
      getSecretValueResult = awsSecretsManager.getSecretValue(getSecretValueRequest);
    } catch (DecryptionFailureException e) {
      // Secrets Manager can't decrypt the protected secret text using the provided KMS key.
      // Deal with the exception here, and/or rethrow at your discretion.
      throw e;
    } catch (InternalServiceErrorException e) {
      // An error occurred on the server side.
      // Deal with the exception here, and/or rethrow at your discretion.
      throw e;
    } catch (InvalidParameterException e) {
      // You provided an invalid value for a parameter.
      // Deal with the exception here, and/or rethrow at your discretion.
      throw e;
    } catch (InvalidRequestException e) {
      // You provided a parameter value that is not valid for the current state of the resource.
      // Deal with the exception here, and/or rethrow at your discretion.
      throw e;
    } catch (ResourceNotFoundException e) {
      // We can't find the resource that you asked for.
      // Deal with the exception here, and/or rethrow at your discretion.
      throw e;
    }

    // Decrypts secret using the associated KMS CMK.
    // Depending on whether the secret is a string or binary, one of these fields will be populated.
    if (getSecretValueResult.getSecretString() != null) {
      secretPayload = getSecretValueResult.getSecretString();
    }
    else {
      decodedBinarySecret = new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
    }

    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new Jdk8Module());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    String allowedOrigin = System.getenv("CORS_ALLOW_ORIGIN");

    responseHeaders.put("Access-Control-Allow-Origin", allowedOrigin);
    responseHeaders.put("Access-Control-Allow-Methods", "GET,PUT,POST");
    responseHeaders.put("Access-Control-Allow-Headers", "Content-Type, Referer, User-Agent, Accept");

    try {
      githubOAuthClientSecret = objectMapper.readTree(secretPayload).get("GITHUB_OAUTH_CLIENT_SECRET").asText();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {

    LoginRequest loginRequest;
    try {
      if (input.getIsBase64Encoded()) {
        final byte[] bytes = Base64.getDecoder().decode(input.getBody());
        loginRequest = objectMapper.readValue(bytes, LoginRequest.class);
      } else {
        loginRequest = objectMapper.readValue(input.getBody(), LoginRequest.class);
      }
    } catch (Exception e) {
      responseHeaders.put("Content-Type","text/plain");
      response.setHeaders(responseHeaders);
      response.setBody("Problems converting string to json for request" + e.getMessage());
      response.setStatusCode(500);
      return response;
    }

    System.out.println(loginRequest);

    Client client = ClientBuilder.newClient();

    final WebTarget target = client.target("https://github.com/login/oauth/access_token")
        .queryParam("client_id", clientId)
        .queryParam("client_secret", githubOAuthClientSecret)
        .queryParam("code", loginRequest.getCode())
        .queryParam("state", loginRequest.getState());

    System.out.println(target);

    final Response post = target.request(MediaType.APPLICATION_JSON_TYPE).post(null);

    System.out.println(post.getStatus());
    System.out.println(post.getHeaders());
    final GithubAccessTokenResponse githubAccessTokenResponse = post.readEntity(GithubAccessTokenResponse.class);

    System.out.println(githubAccessTokenResponse);

    try {
      response.setBody(objectMapper.writeValueAsString(githubAccessTokenResponse));
      responseHeaders.put("Content-Type","application/json");
    } catch (JsonProcessingException e) {
      e.printStackTrace();
      response.setBody("Problems converting json to string");
      return response;
    }
    response.setHeaders(responseHeaders);
    return response;
  }
}

package org.christchurchmayfair.api.sermons;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.DecryptionFailureException;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.InternalServiceErrorException;
import com.amazonaws.services.secretsmanager.model.InvalidParameterException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.christchurchmayfair.api.sermons.graphql.GraphQLSermonDataStore;
import org.christchurchmayfair.api.sermons.persistence.SermonDataStore;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

public abstract class SermonDataFunction {

  protected SermonDataStore sermons;
  protected ObjectMapper objectMapper;
  protected Map<String, String> responseHeaders = new HashMap<>();
  protected APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

  public SermonDataFunction() {

    String secretName = System.getenv("GRAPHCOOL_TOKEN_SECRET_NAME");
    String region = System.getenv("GRAPHCOOL_SECRET_REGION");

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

    String graphqlEndpoint = System.getenv("GRAPHQL_ENDPOINT");
    String graphcool_token = "";
    try {
      graphcool_token = objectMapper.readTree(secretPayload).get("GRAPHCOOL_TOKEN").asText();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      sermons = new GraphQLSermonDataStore(graphqlEndpoint, graphcool_token, objectMapper);
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  public SermonDataFunction(SermonDataStore sermons) {
    this.sermons = sermons;
    objectMapper = new ObjectMapper();
    objectMapper.registerModule(new Jdk8Module());
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  public static boolean isAuthorized(APIGatewayProxyRequestEvent request) {
    final Map<String, String> headers = request.getHeaders();

    String gotToMatchThis = "48fdbe58775b2cb3976e21d0b36b4861".toUpperCase();

    if (headers == null) {
      return false;
    }

    if (! headers.containsKey("X-CCM-MAGIC-AUTH")) {
      return false;
    }

    String sharedSecret = headers.get("X-CCM-MAGIC-AUTH");

    try {
      MessageDigest md = MessageDigest.getInstance("MD5");

      md.update(sharedSecret.getBytes());

      byte[] digest = md.digest();

      String sharedSecretHash = DatatypeConverter
          .printHexBinary(digest).toUpperCase();

      return gotToMatchThis.equals(sharedSecretHash);

    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return false;
    }
  }


}

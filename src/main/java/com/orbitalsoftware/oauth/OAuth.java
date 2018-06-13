package com.orbitalsoftware.oauth;

import com.orbitalsoftware.util.QueryStringBuilder;
import com.orbitalsoftware.util.MapBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class OAuth {

    private static final String O_AUTH_VERSION = "1.0";
    private static final String O_AUTH_PARAMETER_PREFIX = "oauth_";

    private static final String O_AUTH_CONSUMER_KEY_KEY = O_AUTH_PARAMETER_PREFIX + "consumer_key";
    private static final String O_AUTH_CALLBACK_KEY = O_AUTH_PARAMETER_PREFIX + "callback";
    private static final String O_AUTH_VERSION_KEY = O_AUTH_PARAMETER_PREFIX + "version";
    private static final String O_AUTH_SIGNATURE_METHOD_KEY = O_AUTH_PARAMETER_PREFIX + "signature_method";
    private static final String O_AUTH_SIGNATURE_KEY = O_AUTH_PARAMETER_PREFIX + "signature";
    private static final String O_AUTH_TIMESTAMP_KEY = O_AUTH_PARAMETER_PREFIX + "timestamp";
    private static final String O_AUTH_NONCE_KEY = O_AUTH_PARAMETER_PREFIX + "nonce";
    private static final String O_AUTH_TOKEN_KEY = O_AUTH_PARAMETER_PREFIX + "token";
    private static final String O_AUTH_TOKEN_SECRET_KEY = O_AUTH_PARAMETER_PREFIX + "token_secret";
    private static final String O_AUTH_VERIFIER = O_AUTH_PARAMETER_PREFIX + "verifier";

    private static final String HEADER_AUTH = "Authorization";
    private static final String HEADER_AUTH_VALUE_FORMAT = "OAuth %s";
    private static final String HEADER_PARAM_FORMAT = "%s=\"%s\"";

    private static final String X_AUTH_USERNAME = "x_auth_username";
    private static final String X_AUTH_PASSWORD = "x_auth_password";
    private static final String X_AUTH_MODE = "x_auth_mode";
    private static final String X_AUTH_MODE_VALUE = "client_auth";

    // TODO: This needs to be more flexible.
    private static final String REQUEST_METHOD = "POST";

    private static final int NONCE_RADIX = 60000;

    // TODO: Move this up a level.
    private static final String HMACSHA1SignatureType = "HMAC-SHA1";
    private static final String POST_PARAMETERS_CONTENT_TYPE = "application/x-www-form-urlencoded";

    private final Random random = new Random();
    private final String baseApiUrl;
    private final String consumerKey;
    private final String consumerSecret;


    public AuthToken getAccessToken(@NonNull final String username, @NonNull final String password) throws IOException {
        // TODO: This should be  written flexibly so as to cover other OAuth scenarios (parameters in query string, etc)

        // Authorization Header
        Map<String, String> authHeaderParameters = new HashMap<>();
        MapBuilder<String, String> authHeaderBuilder = new MapBuilder(authHeaderParameters);
        authHeaderBuilder.put(O_AUTH_CONSUMER_KEY_KEY, consumerKey)
                .put(O_AUTH_VERSION_KEY, O_AUTH_VERSION)
                .put(O_AUTH_SIGNATURE_METHOD_KEY, HMACSHA1SignatureType)
                .put(O_AUTH_TIMESTAMP_KEY, generateTimeStamp())
                .put(O_AUTH_NONCE_KEY, generateNonce());

        Map<String, String> bodyParameters = new HashMap<>();
        MapBuilder<String, String> bodyBuilder = new MapBuilder(bodyParameters);
        bodyBuilder.put(X_AUTH_USERNAME, username)
                .put(X_AUTH_PASSWORD, password)
                .put(X_AUTH_MODE, X_AUTH_MODE_VALUE);

        // TODO: Remove this hard coding.
        String url = baseApiUrl + "/oauth/access_token";
        System.err.printf("URL: %s\n", url);
        String signatureBase = generateSignatureBase(url, authHeaderParameters, bodyParameters);
        System.err.printf("Signature Base: %s\n", signatureBase);
        String signature = generateSignature(signatureBase);
        System.err.printf("Signature: %s\n", signature);
        authHeaderBuilder.put(O_AUTH_SIGNATURE_KEY, signature);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost request = new HttpPost(url);
        // Add the authorization header.
        String headerParameters = generateAuthHeaderParams(authHeaderParameters);

        // Create the post body
        QueryStringBuilder postDataBuilder = new QueryStringBuilder().addParameters(bodyParameters);
        request.setHeader(HEADER_AUTH, String.format(HEADER_AUTH_VALUE_FORMAT, headerParameters));

        StringEntity postData = new StringEntity(postDataBuilder.build());
        postData.setContentType(POST_PARAMETERS_CONTENT_TYPE);
        request.setEntity(postData);
        CloseableHttpResponse response = httpclient.execute(request);
        try {
            System.out.println(response.getStatusLine().getStatusCode());
            System.out.println(response.getStatusLine().getReasonPhrase());
            Map<String, String> responseParams = QueryStringBuilder.toParameters(EntityUtils.toString(response.getEntity()));
            return AuthToken.builder().tokenKey(responseParams.get(O_AUTH_TOKEN_KEY)).tokenSecret(responseParams.get(O_AUTH_TOKEN_SECRET_KEY)).build();
        } finally {
            response.close();
        }
    }

    private String generateAuthHeaderParams(Map<String, String> authHeaderParameters) {
        return authHeaderParameters.entrySet().stream().map(e -> String.format(HEADER_PARAM_FORMAT, e.getKey(), e.getValue())).collect(Collectors.joining(", "));
    }

    private String generateNonce() {
        return Long.toString(Math.abs(random.nextLong()), NONCE_RADIX);
    }

    private String generateTimeStamp() {
        return Long.toString(System.currentTimeMillis() / 1000);
    }

    private String generateSignatureBase(String url, Map<String, String> authHeaderParameters, Map<String, String> requestParameters) {
        Map<String, String> allParameters = new TreeMap<>(authHeaderParameters);
        allParameters.putAll(requestParameters);
        QueryStringBuilder qsBuilder = new QueryStringBuilder().addParameters(allParameters);
        String normalizedRequestParameters = qsBuilder.build();
        System.err.printf("Normalized Parameters: %s\n", normalizedRequestParameters);
        return String.format("%s&%s&%s", REQUEST_METHOD, URLEncoder.encode(url, StandardCharsets.UTF_8), URLEncoder.encode(normalizedRequestParameters, StandardCharsets.UTF_8));
    }

    private String generateSignature(String message) throws UnsupportedEncodingException {
        String key = String.format("%s&", consumerSecret); // No token secret just yet.
        HMac encoder = new HMac(new SHA1Digest());
        encoder.init(new KeyParameter(key.getBytes(StandardCharsets.UTF_8)));
        byte[] msgBytes = message.getBytes(StandardCharsets.UTF_8);
        encoder.update(msgBytes, 0, msgBytes.length);
        byte[] mac = new byte[encoder.getMacSize()];
        encoder.doFinal(mac, 0);
        return Base64.getEncoder().encodeToString(mac);
    }
}

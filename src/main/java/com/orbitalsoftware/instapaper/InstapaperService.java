package com.orbitalsoftware.instapaper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitalsoftware.oauth.AuthToken;
import com.orbitalsoftware.oauth.OAuth;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;

public class InstapaperService {

    private static final String HMACSHA1SignatureType = "HMAC-SHA1";
    private static final String BASE_API_URL = "https://www.instapaper.com/api/1";

    private static final String AUTHORIZATION_URI = "/oauth/access_token";
    private static final String BOOKMARKS_LIST_URI = "/bookmarks/list";

    private static final String KEY_ELEMENT_TYPE = "type";

    private final OAuth oAuth;
    private final ObjectMapper objectMapper;

    public InstapaperService(@NonNull final String consumerKey, @NonNull final String consumerSecret) throws Exception {
        this.oAuth = new OAuth(BASE_API_URL, AUTHORIZATION_URI, consumerKey, consumerSecret);
        objectMapper = new ObjectMapper();
    }

    // TODO: Use more specific exceptions.
    public AuthToken getAuthToken(@NonNull String username, @NonNull String password) throws Exception {
        return oAuth.getAccessToken(username, password);
    }

    // TODO: The return type may need to be generalized to adjust for different response content.
    private List<Map<String, Object>> makeRequest(@NonNull AuthToken authToken, @NonNull String requestUri, @NonNull Optional<Map<String, String>> parameters) throws IOException {
        return objectMapper.readValue(oAuth.makeRequest(Optional.of(authToken), requestUri, parameters), new TypeReference<List<Map<String, Object>>>() {});
    }

    // TODO: Add parameters
    public BookmarksListResponse getBookmarks(@NonNull BookmarksListRequest request) throws IOException {
        // TODO: Create generic code to add parameters.
        return createBookmarksListResponse(makeRequest(request.getAuthToken(), BOOKMARKS_LIST_URI, Optional.empty()));
    }

    // TODO: This is likely better placed in a factory class of some sort.
    private BookmarksListResponse createBookmarksListResponse(@NonNull List<Map<String, Object>> response) throws MalformedURLException {
        BookmarksListResponse.BookmarksListResponseBuilder responseBuilder =  BookmarksListResponse.builder();
        List<Bookmark> bookmarks = new LinkedList<Bookmark>();
        responseBuilder.bookmarks(bookmarks);

        for (Map<String, Object> element : response) {
            ResponseElementHelper e = new ResponseElementHelper(element);
            String elementType = e.getAsType(KEY_ELEMENT_TYPE, String.class);

            switch (elementType) {
                case (User.TYPE):
                    responseBuilder.user(User.forResponseElement(e));
                    break;
                case (Bookmark.TYPE):
                    bookmarks.add(Bookmark.forResponseElement(e));
                    break;
                default:
                    System.err.printf("Don't know how to handle element of type \"%s\"%n", elementType);
            }
        }

        return responseBuilder.build();
    }
}

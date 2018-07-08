package com.orbitalsoftware.readitlater.alexa;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.orbitalsoftware.instapaper.ArchiveBookmarkRequest;
import com.orbitalsoftware.instapaper.Bookmark;
import com.orbitalsoftware.instapaper.BookmarksListRequest;
import com.orbitalsoftware.instapaper.BookmarksListResponse;
import com.orbitalsoftware.instapaper.DeleteBookmarkRequest;
import com.orbitalsoftware.instapaper.InstapaperService;
import com.orbitalsoftware.instapaper.StarBookmarkRequest;
import com.orbitalsoftware.oauth.AuthToken;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import lombok.Getter;
import lombok.NonNull;
import org.jsoup.Jsoup;

public class SessionManager {

  private static final String CONSUMER_TOKEN_KEY = "ConsumerToken";
  private static final String CONSUMER_SECRET_KEY = "ConsumerSecret";

  // TODO: This is hardcoded to my personal account and should be deleted when a proper,
  // user-specific auth mechanism has been created.
  private static final String AUTH_TOKEN_RESOURCE = "instapaper_auth.token";
  private static final String TOKEN_KEY = "tokenKey";
  private static final String TOKEN_SECRET = "tokenSecret";

  private static final String PROMPT_FORMAT =
      "The next story in your queue is entitled \"%s\". What would you like to do?";

  @Getter private final HandlerInput input;
  private final ObjectMapper mapper;
  private InstapaperService instapaperService;
  private final AuthToken authToken;

  private static final String KEY_CUSTOMER_STATE = "CustomerState";

  private CustomerState customerState = new CustomerState(Optional.empty(), new LinkedList<>());

  public SessionManager(@NonNull HandlerInput input) throws Exception {
    this.input = input;
    this.mapper = new ObjectMapper();
    this.mapper.registerModule(new Jdk8Module());
    String token = System.getenv(CONSUMER_TOKEN_KEY);
    String secret = System.getenv(CONSUMER_SECRET_KEY);
    instapaperService = new InstapaperService(token, secret);
    authToken = getAuthToken();
    loadCustomerState();
  }

  public boolean hasArticle() {
    return customerState.getCurrentArticle().isPresent();
  }

  private AuthToken getAuthToken() throws IOException {
    Properties authTokenProperties = new Properties();
    authTokenProperties.load(getClass().getClassLoader().getResourceAsStream(AUTH_TOKEN_RESOURCE));
    String tokenKey = authTokenProperties.getProperty(TOKEN_KEY);
    String tokenSecret = authTokenProperties.getProperty(TOKEN_SECRET);
    return AuthToken.builder().tokenKey(tokenKey).tokenSecret(tokenSecret).build();
  }

  private void setNextArticle() throws IOException {
    customerState.setCurrentArticle(getNextArticle());
    if (customerState.hasArticle()) {
      saveCustomerState();
    }
  }

  private void loadCustomerState() throws IOException {
    String rawCustomerState =
        (String) input.getAttributesManager().getPersistentAttributes().get(KEY_CUSTOMER_STATE);
    if (rawCustomerState == null) {
      this.customerState = CustomerState.emptyState();
    } else {
      try {
        this.customerState = mapper.readValue(rawCustomerState, CustomerState.class);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    if (!this.customerState.hasArticle()) {
      setNextArticle();
    }
  }

  private void saveCustomerState() throws IOException {
    Map<String, Object> persistedAttributes = new HashMap<>();
    persistedAttributes.put(KEY_CUSTOMER_STATE, mapper.writeValueAsString(customerState));
    input.getAttributesManager().setPersistentAttributes(persistedAttributes);
    input.getAttributesManager().savePersistentAttributes();
  }

  private Optional<Bookmark> bookmarkFromJson(String json) {
    try {
      return Optional.of(mapper.readValue(json, Bookmark.class));
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  private void clearCurrentArticle() {
    customerState.setCurrentArticle(Optional.empty());
  }

  private void throwIfNoCurrentArticle() {
    if (!customerState.hasArticle()) {
      throw new IllegalStateException("There are currently no articles available.");
    }
  }

  public void deleteCurrentArticle() throws IOException {
    throwIfNoCurrentArticle();
    instapaperService.deleteBookmark(
        authToken,
        DeleteBookmarkRequest.builder()
            .bookmarkId(customerState.getCurrentArticle().get().getBookmarkId())
            .build());
    clearCurrentArticle();
    setNextArticle();
  }

  public void archiveCurrentArticle() throws IOException {
    throwIfNoCurrentArticle();
    instapaperService.archiveBookmark(
        authToken,
        ArchiveBookmarkRequest.builder()
            .bookmarkId(customerState.getCurrentArticle().get().getBookmarkId())
            .build());
    clearCurrentArticle();
    setNextArticle();
  }

  public void starCurrentArticle() throws IOException {
    throwIfNoCurrentArticle();
    instapaperService.starBookmark(
        authToken,
        StarBookmarkRequest.builder()
            .bookmarkId(customerState.getCurrentArticle().get().getBookmarkId())
            .build());
    clearCurrentArticle();
    setNextArticle();
  }

  private Optional<Bookmark> getNextArticle() throws IOException {
    // TODO: Add skipped stories here.
    BookmarksListResponse response =
        instapaperService.getBookmarks(getAuthToken(), BookmarksListRequest.builder().build());
    // TODO: Add more detailed filtering.
    return response
        .getBookmarks()
        .stream()
        .filter(bookmark -> !bookmark.getUrl().startsWith("https://www.youtube.com"))
        .filter(bookmark -> !customerState.getArticlesToSkip().contains(bookmark.getBookmarkId()))
        .findFirst();
  }

  public Optional<String> getNextStoryTitle() {
    return customerState.getCurrentArticle().map(s -> s.getTitle());
  }

  public Optional<String> getNextStoryPrompt() {
    return getNextStoryTitle().map(t -> String.format(PROMPT_FORMAT, t));
  }

  // TODO: Add star, archive, or delete question at the end.
  public Optional<String> getArticleTextPrompt() throws IOException {
    Optional<String> result = Optional.empty();
    try {
      if (customerState.hasArticle()) {
        String filteredStoryText =
            Jsoup.parse(
                    instapaperService.getBookmarkText(
                        authToken, customerState.getCurrentArticle().get().getBookmarkId()))
                .text();
        result = Optional.of(filteredStoryText);
      }
    } catch (Exception e) {
      e.printStackTrace();
      // TODO: Add logging.
    }
    return result;
  }

  public void skipCurrentArticle() throws IOException {
    customerState.skipCurrentArticle();
    setNextArticle(); // Also saves state.
  }
}

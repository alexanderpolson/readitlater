package com.orbitalsoftware.readitlater.alexa.clients;

import com.orbitalsoftware.harvest.annotations.Counted;
import com.orbitalsoftware.instapaper.ArchiveBookmarkRequest;
import com.orbitalsoftware.instapaper.ArchiveBookmarkResponse;
import com.orbitalsoftware.instapaper.Bookmark;
import com.orbitalsoftware.instapaper.BookmarksListRequest;
import com.orbitalsoftware.instapaper.BookmarksListResponse;
import com.orbitalsoftware.instapaper.DeleteBookmarkRequest;
import com.orbitalsoftware.instapaper.InstapaperService;
import com.orbitalsoftware.instapaper.StarBookmarkRequest;
import com.orbitalsoftware.instapaper.StarBookmarkResponse;
import com.orbitalsoftware.instapaper.UpdateReadProgressRequest;
import com.orbitalsoftware.oauth.OAuthToken;
import com.orbitalsoftware.oauth.OAuthTokenProvider;
import com.orbitalsoftware.retry.RetryStrategy;
import java.util.concurrent.TimeUnit;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InstapaperServiceWithRetryStrategies extends InstapaperService {

  private static final int DEFAULT_MAX_TRIES = 4;
  private static final long DEFAULT_TIMEOUT_TIME_MSEC = 2500;

  private static final int UPDATE_READ_PROGRESS_MAX_TRIES = DEFAULT_MAX_TRIES;
  private static final long UPDATE_READ_PROGRESS_TIMEOUT_TIME_MSEC = 1000;

  private final RetryStrategy defaultRetryStrategy;
  private final RetryStrategy updateReadProgressRetryStrategy;

  public InstapaperServiceWithRetryStrategies(
      @NonNull OAuthTokenProvider oAuthTokenProvider,
      @NonNull OAuthTokenProvider accessTokenProvider)
      throws Exception {
    super(oAuthTokenProvider, accessTokenProvider);

    defaultRetryStrategy =
        RetryStrategy.builder()
            .maxNumTries(DEFAULT_MAX_TRIES)
            .timeOutTime(DEFAULT_TIMEOUT_TIME_MSEC)
            .onTimeout(
                (time, unit) -> {
                  log.error(
                      "Timed out after {} msec waiting for a response.", DEFAULT_TIMEOUT_TIME_MSEC);
                })
            .onFailure(
                (tryNum, e) -> {
                  log.error("Failed getting a response after try #{}.", tryNum, e);
                })
            .onGiveUp(
                (maxTries) -> log.error("Giving up getting a response after {} tries.", maxTries))
            .build();
    updateReadProgressRetryStrategy =
        RetryStrategy.builder()
            .maxNumTries(UPDATE_READ_PROGRESS_MAX_TRIES)
            .timeOutTime(UPDATE_READ_PROGRESS_TIMEOUT_TIME_MSEC)
            .onTimeout(this::onTimeout)
            .onFailure(this::onFailure)
            .onGiveUp(this::onGiveUp)
            .build();
  }

  @Counted
  private void onTimeout(long time, TimeUnit unit) {
    log.error(
        "Timed out after {} msec waiting for a response from updateReadProgress.",
        DEFAULT_TIMEOUT_TIME_MSEC);
  }

  @Counted
  private void onFailure(int tryNum, Throwable e) {
    log.error("Failed getting a response from updateReadProgress after try #{}.", tryNum, e);
  }

  @Counted
  private void onGiveUp(int maxTries) {
    log.error("Giving up getting a response from updateReadProgress after {} tries.", maxTries);
  }

  @Override
  public OAuthToken getAccessToken(String username, String password) throws Exception {
    return defaultRetryStrategy.execute(() -> super.getAccessToken(username, password));
  }

  @Override
  public BookmarksListResponse getBookmarks(BookmarksListRequest request) throws Exception {
    return defaultRetryStrategy.execute(() -> super.getBookmarks(request));
  }

  @Override
  public String getBookmarkText(Integer bookmarkId) throws Exception {
    return defaultRetryStrategy.execute(() -> super.getBookmarkText(bookmarkId));
  }

  @Override
  public ArchiveBookmarkResponse archiveBookmark(ArchiveBookmarkRequest request) throws Exception {
    return defaultRetryStrategy.execute(() -> super.archiveBookmark(request));
  }

  @Override
  public StarBookmarkResponse starBookmark(StarBookmarkRequest request) throws Exception {
    return defaultRetryStrategy.execute(() -> super.starBookmark(request));
  }

  @Override
  public void deleteBookmark(DeleteBookmarkRequest request) throws Exception {
    defaultRetryStrategy.execute(
        () -> {
          super.deleteBookmark(request);
          return Void.TYPE;
        });
  }

  @Override
  public Bookmark updateReadProgress(UpdateReadProgressRequest request) throws Exception {
    return updateReadProgressRetryStrategy.execute(() -> super.updateReadProgress(request));
  }
}

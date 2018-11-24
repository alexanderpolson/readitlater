package com.orbitalsoftware.readitlater.alexa.clients;

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
import com.orbitalsoftware.oauth.AuthToken;
import com.orbitalsoftware.oauth.CredentialsProvider;
import com.orbitalsoftware.retry.RetryStrategy;
import lombok.NonNull;

public class InstapaperServiceWithRetryStrategies extends InstapaperService {

  private static final int DEFAULT_MAX_TRIES = 4;
  private static final long DEFAULT_TIMEOUT_TIME_MSEC = 2500;

  private static final int UPDATE_READ_PROGRESS_MAX_TRIES = DEFAULT_MAX_TRIES;
  private static final long UPDATE_READ_PROGRESS_TIMEOUT_TIME_MSEC = 1000;

  private final RetryStrategy defaultRetryStrategy;
  private final RetryStrategy updateReadProgressRetryStrategy;

  public InstapaperServiceWithRetryStrategies(@NonNull CredentialsProvider credentialsProvider)
      throws Exception {
    super(credentialsProvider);

    defaultRetryStrategy =
        RetryStrategy.builder()
            .maxNumTries(DEFAULT_MAX_TRIES)
            .timeOutTime(DEFAULT_TIMEOUT_TIME_MSEC)
            .onTimeout(
                (time, unit) -> {
                  System.err.printf(
                      "Timed out after %d msec waiting for a response.\n",
                      DEFAULT_TIMEOUT_TIME_MSEC);
                })
            .onFailure(
                (tryNum, e) -> {
                  System.err.printf("Failed getting a response after try #%d.", tryNum);
                  e.printStackTrace();
                })
            .onGiveUp(
                (maxTries) ->
                    System.err.printf("Giving up getting a response after %d tries.", maxTries))
            .build();
    updateReadProgressRetryStrategy =
        RetryStrategy.builder()
            .maxNumTries(UPDATE_READ_PROGRESS_MAX_TRIES)
            .timeOutTime(UPDATE_READ_PROGRESS_TIMEOUT_TIME_MSEC)
            .onTimeout(
                (time, unit) -> {
                  System.err.printf(
                      "Timed out after %d msec waiting for a response from updateReadProgress.\n",
                      DEFAULT_TIMEOUT_TIME_MSEC);
                })
            .onFailure(
                (tryNum, e) -> {
                  System.err.printf(
                      "Failed getting a response from updateReadProgress after try #%d.", tryNum);
                  e.printStackTrace();
                })
            .onGiveUp(
                (maxTries) ->
                    System.err.printf(
                        "Giving up getting a response from updateReadProgress after %d tries.",
                        maxTries))
            .build();
  }

  @Override
  public AuthToken getAuthToken(String username, String password) throws Exception {
    return defaultRetryStrategy.execute(() -> super.getAuthToken(username, password));
  }

  @Override
  public BookmarksListResponse getBookmarks(AuthToken authToken, BookmarksListRequest request)
      throws Exception {
    return defaultRetryStrategy.execute(() -> super.getBookmarks(authToken, request));
  }

  @Override
  public String getBookmarkText(AuthToken authToken, Integer bookmarkId) throws Exception {
    return defaultRetryStrategy.execute(() -> super.getBookmarkText(authToken, bookmarkId));
  }

  @Override
  public ArchiveBookmarkResponse archiveBookmark(
      AuthToken authToken, ArchiveBookmarkRequest request) throws Exception {
    return defaultRetryStrategy.execute(() -> super.archiveBookmark(authToken, request));
  }

  @Override
  public StarBookmarkResponse starBookmark(AuthToken authToken, StarBookmarkRequest request)
      throws Exception {
    return defaultRetryStrategy.execute(() -> super.starBookmark(authToken, request));
  }

  @Override
  public void deleteBookmark(AuthToken authToken, DeleteBookmarkRequest request) throws Exception {
    defaultRetryStrategy.execute(
        () -> {
          super.deleteBookmark(authToken, request);
          return Void.TYPE;
        });
  }

  @Override
  public Bookmark updateReadProgress(AuthToken authToken, UpdateReadProgressRequest request)
      throws Exception {
    return updateReadProgressRetryStrategy.execute(
        () -> super.updateReadProgress(authToken, request));
  }
}

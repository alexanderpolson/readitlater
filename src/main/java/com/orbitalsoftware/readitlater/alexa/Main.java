package com.orbitalsoftware.readitlater.alexa;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.GetSpeechSynthesisTaskRequest;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.StartSpeechSynthesisTaskRequest;
import com.amazonaws.services.polly.model.StartSpeechSynthesisTaskResult;
import com.amazonaws.services.polly.model.SynthesisTask;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.TaskStatus;
import com.amazonaws.services.polly.model.Voice;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orbitalsoftware.harvest.ExecutionTimer;
import com.orbitalsoftware.harvest.annotations.Timed;
import com.orbitalsoftware.instapaper.ArchiveBookmarkRequest;
import com.orbitalsoftware.instapaper.Bookmark;
import com.orbitalsoftware.instapaper.BookmarkId;
import com.orbitalsoftware.instapaper.BookmarksListRequest;
import com.orbitalsoftware.instapaper.BookmarksListResponse;
import com.orbitalsoftware.instapaper.Instapaper;
import com.orbitalsoftware.instapaper.InstapaperService;
import com.orbitalsoftware.instapaper.UnarchiveBookmarkRequest;
import com.orbitalsoftware.instapaper.UpdateReadProgressRequest;
import com.orbitalsoftware.instapaper.auth.InstapaperAuthTokenProvider;
import com.orbitalsoftware.instapaper.auth.PropertiesInstapaperAuthTokenProvider;
import com.orbitalsoftware.oauth.OAuthCredentialsProvider;
import com.orbitalsoftware.oauth.PropertiesOAuthCredentialsProvider;
import com.orbitalsoftware.readitlater.alexa.article.ArticleTextPaginator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.Jsoup;

@Log4j2
public class Main {

  private static final String AUTH_TOKEN_PROPERTIES_PATH = "/Users/apolson/.instapaper_auth_token";
  private static final String O_AUTH_CREDENTIALS_PROPERTIES_PATH =
      "/Users/apolson/.instapaper_oauth_credentials";

  private static final Integer LARGE_BOOKMARK_ID = 1096290739;
  private static final Integer DELETED_ID = 1079451394;

  // Note: Takes approximately CHUNK_SIZE msec per chunk of size CHUNK_SIZE
  private static final int CHUNK_SIZE = 1000;

  private final Instapaper instapaper;
  private final AmazonPolly polly;
  private final Voice voice;

  public static final void main(final String[] args) throws Exception {
    log.info("Starting Main.");
    new Main().run();
  }

  public Main() throws Exception {

    OAuthCredentialsProvider oAuthCredentialsProvider =
        new PropertiesOAuthCredentialsProvider(
            new FileInputStream(O_AUTH_CREDENTIALS_PROPERTIES_PATH));
    InstapaperAuthTokenProvider authTokenProvider =
        new PropertiesInstapaperAuthTokenProvider(new FileInputStream(AUTH_TOKEN_PROPERTIES_PATH));
    instapaper = new InstapaperService(oAuthCredentialsProvider, authTokenProvider);
    final AWSCredentialsProvider credentialsProvider =
        new AWSCredentialsProvider() {
          @Override
          public AWSCredentials getCredentials() {
            return new AWSCredentials() {
              @Override
              public String getAWSAccessKeyId() {
                return "AKIARBLXESMS3QFYCHGP";
              }

              @Override
              public String getAWSSecretKey() {
                return "bobk8zvt+W4titcVAnxEkBhnTVXRlpYH78lG4MPI";
              }
            };
          }

          @Override
          public void refresh() {}
        };

    polly = AmazonPollyClient.builder().withCredentials(credentialsProvider).build();

    // Get a voice to generate text from.
    DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();

    // Synchronously ask Amazon Polly to describe available TTS voices.
    DescribeVoicesResult describeVoicesResult = polly.describeVoices(describeVoicesRequest);
    voice =
        describeVoicesResult.getVoices().stream()
            .filter((v) -> v.getName().equals("Salli"))
            .findFirst()
            .get();
    System.err.printf("Using voice \"%s\" for synthesizing.%n", voice.getName());
  }

  private void run() throws Exception {
    //    pollyLargeArticleChunks();
    pollyLargeArticleTask();
  }

  private void characterCounts() throws Exception {
    final List<Bookmark> bookmarks = getBookmarks();
    int characterCount = 0;
    for (final Bookmark bookmark : bookmarks) {
      int articleLength = storyText(bookmark.getBookmarkId().getId()).length();
      System.err.printf("Article is %d characters long.%n", articleLength);
      characterCount += articleLength;
    }

    System.err.printf(
        "==========%nTotal of %d characters across %d articles.%n",
        characterCount, bookmarks.size());
  }

  private void pollyLargeArticleTask() throws Exception {
    String text = getBookmarkText(BookmarkId.builder().id(LARGE_BOOKMARK_ID).build());

    long startTime = System.currentTimeMillis();
    getVoiceForText(text, new File(String.format("%d.mp3", LARGE_BOOKMARK_ID)));
    long endTime = System.currentTimeMillis();

    System.err.printf(
        "Total time to get audio for full article of length %d: %d msec%n",
        text.length(), endTime - startTime);
  }

  private void pollyLargeArticleChunks() throws Exception {
    String text = getBookmarkText(BookmarkId.builder().id(LARGE_BOOKMARK_ID).build());
    List<String> chunks = chunkifyText(filterText(text));

    int index = 0;
    long startTime = System.currentTimeMillis();
    System.err.printf(
        "Synthesizing text for %d chunk article, with chunk size %d.%n", chunks.size(), CHUNK_SIZE);
    for (String chunk : chunks) {
      getVoiceForText(chunk, new File(String.format("%d.%d.mp3", LARGE_BOOKMARK_ID, index)));
      index++;
    }
    long endTime = System.currentTimeMillis();
    System.err.printf(
        "Total time to get audio for %d chunk article: %d msec%n",
        chunks.size(), endTime - startTime);
  }

  private String filterText(final String text) {
    return Jsoup.parse(text).text();
  }

  private List<String> chunkifyText(final String text) {
    List<String> chunks = new LinkedList<>();

    // Not worried about quality of splitting up text.
    int start = 0;
    int end = CHUNK_SIZE;

    while (start < text.length()) {
      chunks.add(text.substring(start, end));
      start = end + 1;
      final int potentialNewEnd = end + CHUNK_SIZE;
      end = potentialNewEnd > text.length() ? text.length() - 1 : potentialNewEnd;
    }

    return chunks;
  }

  @Timed
  private void getVoiceForText(final String text, final File file) throws IOException {
    long startTime = System.currentTimeMillis();
    if (text.length() > 3000) {
      System.err.printf("Completed requests. Result URI: %s%n", audioForLongText(text));
    } else {
      try (InputStream voiceOutput = audioForText(text)) {
        save(voiceOutput, file);
      }
    }
    long endTime = System.currentTimeMillis();
    System.err.printf("Time to synthesize and save voice audio: %d msec%n", endTime - startTime);
  }

  private static final long POLLY_TASK_WAIT_INTERVAL = 500;

  private String audioForLongText(String text) {
    //    if (text.length() > 3000) {
    StartSpeechSynthesisTaskRequest startTaskRequest =
        new StartSpeechSynthesisTaskRequest()
            .withOutputFormat(OutputFormat.Mp3)
            .withVoiceId(voice.getId())
            .withText(text)
            .withOutputS3BucketName("orbitalsoftware.com.pollytest")
            .withOutputS3KeyPrefix(LARGE_BOOKMARK_ID.toString());
    StartSpeechSynthesisTaskResult result = polly.startSpeechSynthesisTask(startTaskRequest);
    SynthesisTask task = result.getSynthesisTask();
    //        new SynthesisTask()
    //            .withTaskId("47e259db-686e-42f3-97f0-4970816bbca5")
    //            .withTaskStatus("");

    while (!task.getTaskStatus().equals(TaskStatus.Completed.toString())) {
      System.err.printf(".");
      try {
        Thread.sleep(POLLY_TASK_WAIT_INTERVAL);
      } catch (InterruptedException e) {
        System.err.println("Sleep thread interrupted");
      }
      GetSpeechSynthesisTaskRequest taskRequest =
          new GetSpeechSynthesisTaskRequest().withTaskId(task.getTaskId());
      task = polly.getSpeechSynthesisTask(taskRequest).getSynthesisTask();
    }

    // Should be done if we've gotten here.
    return task.getOutputUri();

    //    } else {
    //      SynthesizeSpeechRequest synthReq =
    //          new SynthesizeSpeechRequest()
    //              .withText(text)
    //              .withVoiceId(voice.getId())
    //              .withOutputFormat(OutputFormat.Mp3);
    //      SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);
    //      return synthRes.getAudioStream();
    //    }
  }

  private InputStream audioForText(String text) {
    SynthesizeSpeechRequest synthReq =
        new SynthesizeSpeechRequest()
            .withText(text)
            .withVoiceId(voice.getId())
            .withOutputFormat(OutputFormat.Mp3);
    SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);
    return synthRes.getAudioStream();
  }

  private void save(InputStream in, File file) throws IOException {
    Path path = file.getAbsoluteFile().toPath();
    System.err.printf("Saving audio to %s%n", path);
    Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
  }

  private void timeCalls() throws Exception {
    BookmarksListRequest request = BookmarksListRequest.builder().build();
    BookmarksListResponse response = instapaper.getBookmarks(request);

    //    BookmarkId bookmarkId = BookmarkId.builder().id(1117369971).build();

    for (int passNum = 1; passNum <= 100; passNum++) {
      response.getBookmarks().stream()
          .forEach((bookmark) -> getBookmarkText(bookmark.getBookmarkId()));
      //      getBookmarkText(bookmarkId);
    }

    ExecutionTimer.summarize();
  }

  private String getBookmarkText(BookmarkId bookmarkId) {
    try {
      return instapaper.getBookmarkText(bookmarkId.getId());
    } catch (Exception e) {
      log.error("An error occurred while trying to get bookmark text.", e);
      return "";
    }
  }

  private void updateReadProgress() throws Exception {
    instapaper.updateReadProgress(
        UpdateReadProgressRequest.builder().bookmarkId(LARGE_BOOKMARK_ID).progress(0.0).build());
  }

  private void superLongStory() throws Exception {
    String filteredText = Jsoup.parse(storyText(LARGE_BOOKMARK_ID)).text();
    System.out.printf("Super long article character length: %d%n", filteredText.length());
    List<String> pages = ArticleTextPaginator.paginateText(filteredText, 800);
    //    log.info(pages.stream().collect(Collectors.joining("\n")));

    log.info(StringEscapeUtils.escapeXml11(pages.get(3)));
  }

  protected String storyText(final int bookmarkId) throws Exception {
    return instapaper.getBookmarkText(bookmarkId);
  }

  private void bookmarkParsing() throws Exception {
    List<Map<String, Object>> results = null;
    try (InputStream bookmarksJson =
        this.getClass().getClassLoader().getResourceAsStream("bookmarks.list.json")) {
      ObjectMapper mapper = new ObjectMapper();
      results = mapper.readValue(bookmarksJson, new TypeReference<List<Map<String, Object>>>() {});
    }
  }

  private List<Bookmark> getBookmarks() throws Exception {
    List<BookmarkId> haveBookmarks = new LinkedList<>();
    haveBookmarks.add(BookmarkId.builder().id(DELETED_ID).build());
    BookmarksListRequest request =
        BookmarksListRequest.builder()
            .limit(Optional.of(200))
            .have(Optional.of(haveBookmarks))
            .build();
    BookmarksListResponse response = instapaper.getBookmarks(request);
    return response.getBookmarks();
  }

  private void archive() throws Exception {
    ArchiveBookmarkRequest request =
        ArchiveBookmarkRequest.builder().bookmarkId(LARGE_BOOKMARK_ID).build();
    log.info("Archived bookmark: {}", instapaper.archiveBookmark(request));
    getBookmarks();
  }

  private void unarchive() throws Exception {
    UnarchiveBookmarkRequest request =
        UnarchiveBookmarkRequest.builder().bookmarkId(LARGE_BOOKMARK_ID).build();
    log.error("Unarchived bookmark: {}", instapaper.unarchiveBookmark(request));
    getBookmarks();
  }

  private boolean isBlank(String str) {
    return str == null || str.equals("");
  }
}

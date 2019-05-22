package com.orbitalsoftware.readitlater.article;

import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.orbitalsoftware.harvest.annotations.Timed;
import com.orbitalsoftware.readitlater.article.exception.ReadItLaterDependencyException;
import com.orbitalsoftware.readitlater.article.exception.ReadItLaterFatalException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
@AllArgsConstructor
public class AmazonPollyTextToSpeechEngine implements TextToSpeechEngine {

  private static final String URL_FORMAT = "https://s3.amazonaws.com/%s/%s";

  @NonNull private final AmazonPolly amazonPolly;
  @NonNull private final String voiceId;
  @NonNull private final AmazonS3 amazonS3;
  @NonNull private final String s3Bucket;

  @Timed
  @Override
  public URL textToSpeech(String prefix, String text) {
    final String fileName = prefix + ".mp3";
    if (!amazonS3.doesObjectExist(s3Bucket, fileName)) {
      try (final InputStream voiceOutput = audioForText(text)) {
        save(voiceOutput, fileName);
      } catch (final IOException e) {
        throw new ReadItLaterDependencyException(
            "Exception encountered while trying to save audio file to S3.", e);
      }
    }
    return s3UrlForFile(fileName);
  }

  private final URL s3UrlForFile(@NonNull String fileName) {
    try {
      return new URL(String.format(URL_FORMAT, s3Bucket, fileName));
    } catch (MalformedURLException e) {
      throw new ReadItLaterFatalException(
          "Exception encountered while trying to build S3 file URL.", e);
    }
  }

  @Timed
  private InputStream audioForText(String text) {
    SynthesizeSpeechRequest synthReq =
        new SynthesizeSpeechRequest()
            .withText(text)
            .withVoiceId(voiceId)
            .withOutputFormat(OutputFormat.Mp3);
    SynthesizeSpeechResult synthRes = amazonPolly.synthesizeSpeech(synthReq);
    return synthRes.getAudioStream();
  }

  @Timed
  private void save(InputStream in, String fileName) throws IOException {
    // Output the Polly input stream to a local, temp file
    final File tempFile = new File("/tmp/" + fileName);
    final Path path = tempFile.getAbsoluteFile().toPath();
    Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);

    final PutObjectRequest putRequest = new PutObjectRequest(s3Bucket, fileName, tempFile);
    amazonS3.putObject(putRequest);
  }
}

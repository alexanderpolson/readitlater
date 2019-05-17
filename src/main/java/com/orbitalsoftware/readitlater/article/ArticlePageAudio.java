package com.orbitalsoftware.readitlater.article;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Builder
@Data
public class ArticlePageAudio {

  private final ArticlePage page;

  /** The offset in the provided pageUri audio file that playback should start. */
  private final long offset;

  /** The uri that refers to this page's audio MP3 file. */
  private @NonNull final String pageUri;
}

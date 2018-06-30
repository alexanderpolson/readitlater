package com.orbitalsoftware.instapaper;

import com.amazon.ask.model.Response;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

@Builder
@Data
public class Bookmark {
    public static final String TYPE = "bookmark";

    private static final String KEY_BOOKMARK_ID = "bookmark_id";
    private static final String KEY_HASH = "hash";
    private static final String KEY_TITLE = "title";
    private static final String KEY_URL = "url";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_PRIVATE_SOURCE = "private_source";
    private static final String KEY_PROGRESS_TIMESTAMP = "progress_timestamp";
    private static final String KEY_TIME = "time";
    private static final String KEY_PROGRESS = "progress";
    private static final String KEY_STARRED = "starred";

    private final Integer bookmarkId;
    private final String hash;
    private final String title;
    private final String url;
    private final Optional<String> description;
    private final Optional<String> privateSource;
    private final Integer progressTimestamp;
    private final Integer time; // What does this represent?
    private final Double progress;
    private final Boolean isStarred;

    static Bookmark forResponseElement(@NonNull ResponseElement element) {
        if (!element.getType().equals(TYPE)) {
            throw new IllegalArgumentException("Provided element is not of type user.");
        }

        return builder()
                .bookmarkId(element.getAsType(KEY_BOOKMARK_ID, Integer.class))
                .hash(element.get(KEY_HASH))
                .title(element.get(KEY_TITLE))
                .url(element.get(KEY_URL)) // TODO: Is there any value to making this an URL object?
                .description(Optional.ofNullable(element.get(KEY_DESCRIPTION)))
                .privateSource(Optional.ofNullable(element.get(KEY_PRIVATE_SOURCE)))
                .progressTimestamp(element.getAsType(KEY_PROGRESS_TIMESTAMP, Integer.class))
                .time(element.getAsType(KEY_TIME, Integer.class))
                .isStarred(element.getAsBoolean(KEY_STARRED))
                .build();
    }
    static boolean isBookmark(@NonNull ResponseElement element) {
        return element.getType().equals(TYPE);
    }
}

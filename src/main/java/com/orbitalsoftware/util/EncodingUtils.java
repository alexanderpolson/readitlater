package com.orbitalsoftware.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import lombok.NonNull;

public class EncodingUtils {

  /**
   * This is used to maintain backwards-compatibility with Java 8, as Lambda doesn't support a Java
   * 10 runtime which is needed for {@link URLEncoder#encode(String, Charset)}
   *
   * @param value
   * @return
   */
  public static String urlEncode(@NonNull String value) {
    try {
      return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    } catch (UnsupportedEncodingException e) {
      return "";
    }
    //    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}

package wang.zhanwei.rest.proxy.path;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import wang.zhanwei.rest.proxy.path.GoogleApiPathPattern.Segment;
import wang.zhanwei.rest.proxy.path.GoogleApiPathPattern.SegmentType;

@NoArgsConstructor
public class GoogleApiPathMatcher implements PathMatcher {

  static final PatternParserCache cache = new PatternParserCache();

  public static final String DEFAULT_PATH_SEPARATOR = "/";
  private String pathSeparator = DEFAULT_PATH_SEPARATOR;
  private boolean trimTokens = false;

  @Override
  public boolean isPattern(String path) {
    GoogleApiPathPattern parser = cache.get(path);
    return parser.isPattern();
  }

  @Override
  public boolean match(String pattern, String path) {
    return doMatch(pattern, path, true, null);
  }

  @Override
  public boolean matchStart(String pattern, String path) {
    return doMatch(pattern, path, false, null);
  }

  @Override
  public String extractPathWithinPattern(String pattern, String path) {
    GoogleApiPathPattern parser = cache.get(pattern);

    if (!parser.isPattern()) {
      return "";
    }

    int pathIdx = 0;
    for (Segment segment : parser.getSegments()) {
      if (segment.getType() == SegmentType.LITERAL) {
        ++pathIdx;
      } else {
        break;
      }
    }

    String[] pathParts = StringUtils.tokenizeToStringArray(
        path, this.pathSeparator, this.trimTokens, true);

    if (pathIdx >= pathParts.length) {
      return "";
    }

    StringBuilder builder = new StringBuilder(pathParts[pathIdx++]);

    for (; pathIdx < pathParts.length; ++pathIdx) {
      builder.append(this.pathSeparator).append(pathParts[pathIdx]);
    }

    return builder.toString();
  }

  @Override
  public Map<String, String> extractUriTemplateVariables(String pattern,
                                                         String path) {
    Map<String, String> variables = new LinkedHashMap<>();

    boolean result = doMatch(pattern, path, true, variables);
    if (!result) {
      throw new IllegalStateException("Pattern \"" + pattern +
                                      "\" is not a match for \"" + path + "\"");
    }

    return variables;
  }

  @Override
  public Comparator<String> getPatternComparator(String path) {
    return new GoogleApiPathPatternComparator(path);
  }

  @Override
  public String combine(String pattern1, String pattern2) {
    StringBuilder builder = new StringBuilder(pattern1);

    if (!pattern1.endsWith("/")) {
      builder.append("/");
    }

    if (pattern2.startsWith("/")) {
      builder.append(pattern2.substring(1));
    } else {
      builder.append(pattern2);
    }

    return builder.toString();
  }

  protected boolean
  doMatch(String pattern, String path, boolean fullMatch,
          @Nullable Map<String, String> uriTemplateVariables) {
    if (!path.startsWith(this.pathSeparator)) {
      return false;
    }

    GoogleApiPathPattern parser = cache.get(pattern);

    if (!parser.isPattern()) {
      return pattern.equals(path);
    }

    String[] pathDir = tokenizePath(path);

    int endPathIdx =
        matchSegment(parser.getSegments(), pathDir, 0, uriTemplateVariables);

    if (endPathIdx < 0 || (endPathIdx < pathDir.length && fullMatch)) {
      return false;
    }

    return true;
  }

  protected int
  matchSegment(List<Segment> segments, final String[] pathDirs,
               int startPathIdx,
               @Nullable Map<String, String> uriTemplateVariables) {

    int segIdx = 0;
    int pathIdx = startPathIdx;

    while (segIdx < segments.size() && pathIdx < pathDirs.length) {
      Segment segment = segments.get(segIdx);

      switch (segment.getType()) {
      case LITERAL:
        if (!pathDirs[pathIdx].equals(segment.getValue())) {
          return -1;
        }

        ++pathIdx;
        ++segIdx;
        break;
      case STAR:
        ++pathIdx;
        ++segIdx;
        break;
      case WILDCARD:
        ++pathIdx;
        break;
      case VARIABLE:
        int endPathIdx = matchSegment(segment.getSegments(), pathDirs, pathIdx,
                                      uriTemplateVariables);
        if (endPathIdx > pathIdx && uriTemplateVariables != null) {
          uriTemplateVariables.put(segment.getValue(),
                                   buildMatch(pathDirs, pathIdx, endPathIdx));
        }

        pathIdx = endPathIdx;
        ++segIdx;
        break;
      }
    }

    if (segIdx < segments.size() &&
        segments.get(segIdx).getType() != SegmentType.WILDCARD) {
      return -1;
    }

    return pathIdx;
  }

  String buildMatch(final String[] pathDirs, int start, int end) {
    StringBuilder builder = new StringBuilder(pathDirs[start++]);

    while (start < end) {
      builder.append("/").append(pathDirs[start++]);
    }

    return builder.toString();
  }

  protected String[] tokenizePath(String path) {
    return StringUtils.tokenizeToStringArray(path, this.pathSeparator,
                                             this.trimTokens, true);
  }

  static class PatternParserCache {

    Map<String, GoogleApiPathPattern> cached = new HashMap<>();

    public synchronized GoogleApiPathPattern get(String pattern) {
      GoogleApiPathPattern parser = cached.get(pattern);

      if (parser == null) {
        parser = new GoogleApiPathPattern(pattern);
        // TODO: check capability
        cached.put(pattern, parser);
      }

      return parser;
    }
  }

  /**
   * The default {@link Comparator} implementation returned by
   * {@link #getPatternComparator(String)}.
   * <p>In order, the most "generic" pattern is determined by the following:
   * <ul>
   * <li>if it's null or a capture all pattern (i.e. it is equal to "/**")</li>
   * <li>if the other pattern is an actual match</li>
   * <li>if it's a catch-all pattern (i.e. it ends with "**"</li>
   * <li>if it's got more "*" than the other pattern</li>
   * <li>if it's got more "{foo}" than the other pattern</li>
   * <li>if it's shorter than the other pattern</li>
   * </ul>
   */
  protected static class GoogleApiPathPatternComparator
      implements Comparator<String> {
    final String path;

    public GoogleApiPathPatternComparator(String path) { this.path = path; }

    @Override
    public int compare(String o1, String o2) {
      if (o1.equals(o2)) {
        return 0;
      }

      GoogleApiPathPattern left = new GoogleApiPathPattern(o1);
      GoogleApiPathPattern right = new GoogleApiPathPattern(o2);

      if (!left.isPattern() && right.isPattern()) {
        return -1;
      }

      if (left.isPattern() && !right.isPattern()) {
        return 1;
      }

      if (left.getPattern().equals(path)) {
        return -1;
      }

      if (right.getPattern().equals(path)) {
        return 1;
      }

      if (!left.isPattern() && !right.isPattern()) {
        return 0;
      }

      List<Segment> leftSegments = left.flatSegments();
      List<Segment> rightSegments = right.flatSegments();

      int index = 0;
      for (; index < leftSegments.size() && index < rightSegments.size();
           ++index) {
        Segment leftSegment = leftSegments.get(index);
        Segment rightSegment = rightSegments.get(index);

        if (leftSegment.getType() == SegmentType.LITERAL &&
            rightSegment.getType() != SegmentType.LITERAL) {
          return -1;
        }

        if (leftSegment.getType() != SegmentType.LITERAL &&
            rightSegment.getType() == SegmentType.LITERAL) {
          return 1;
        }

        if (leftSegment.getType() == SegmentType.VARIABLE &&
            rightSegment.getType() != SegmentType.VARIABLE) {
          return -1;
        }

        if (leftSegment.getType() != SegmentType.VARIABLE &&
            rightSegment.getType() == SegmentType.VARIABLE) {
          return 1;
        }

        if (leftSegment.getType() == SegmentType.STAR &&
            rightSegment.getType() != SegmentType.STAR) {
          return -1;
        }

        if (leftSegment.getType() != SegmentType.STAR &&
            rightSegment.getType() == SegmentType.STAR) {
          return 1;
        }
      }

      if (index < leftSegments.size()) {
        return -1;
      }

      if (index < rightSegments.size()) {
        return 1;
      }

      return 0;
    }
  }
}
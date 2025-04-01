package wang.zhanwei.rest.proxy.path;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.Singular;

// HTTP Template Grammar:
// Questions:
//   - what are the constraints on LITERAL and IDENT?
//   - what is the character set for the grammar?
//
// Template = "/" Segments [ Verb ] ;
// Segments = Segment { "/" Segment } ;
// Segment  = "*" | "**" | LITERAL | Variable ;
// Variable = "{" FieldPath [ "=" Segments ] "}" ;
// FieldPath = IDENT { "." IDENT } ;
// Verb     = ":" LITERAL ;
@Getter
public class GoogleApiPathPattern {

  final String pattern;

  String verb;
  boolean valid;
  List<Segment> segments = new ArrayList<>();

  int current = 0;
  boolean wildcard = false;

  public GoogleApiPathPattern(String pattern) {
    this.pattern = pattern;
    valid = parse();
  }

  public boolean isPattern() {
    if (!valid) {
      return false;
    }

    for (Segment segment : segments) {
      if (segment.getType() != SegmentType.LITERAL) {
        return true;
      }
    }

    return false;
  }

  public List<Segment> flatSegments() { return flatSegments(segments); }

  List<Segment> flatSegments(List<Segment> segments) {
    List<Segment> retval = new ArrayList<>();

    for (Segment segment : segments) {
      retval.add(segment);

      if (segment.getType() == SegmentType.VARIABLE) {
        retval.addAll(flatSegments(segment.getSegments()));
      }
    }

    return retval;
  }

  char peek() { return pattern.charAt(current); }

  char consume() { return pattern.charAt(current++); }

  boolean consume(char target) {
    if (current >= pattern.length()) {
      return false;
    }

    if (target == consume()) {
      return true;
    }

    return false;
  }

  boolean parse() {
    if (!consume('/')) {
      return false;
    }

    if (!parseSegments(this.segments)) {
      return false;
    }

    if (current < pattern.length()) {
      if (!consume(':') || !parseVerb()) {
        return false;
      }
    }

    if (current < pattern.length()) {
      return false;
    }

    return true;
  }

  boolean parseSegments(List<Segment> segments) {
    if (current < pattern.length()) {
      if (!parseSegment(segments)) {
        return false;
      }
    }

    while (current < pattern.length() && '/' == peek()) {
      if (wildcard) {
        return false;
      }

      consume();

      if (current < pattern.length()) {
        if (!parseSegment(segments)) {
          return false;
        }
      }
    }

    return true;
  }

  boolean parseSegment(List<Segment> segments) {
    boolean retval = true;

    switch (peek()) {
    case '*':
      retval = parseStar(segments);
      break;
    case '{':
      retval = parseVariable(segments);
      break;
    default:
      if (current < pattern.length()) {
        StringBuilder literal = new StringBuilder();

        if (!parseLiteral(literal)) {
          return false;
        }

        segments.add(Segment.builder()
                         .type(SegmentType.LITERAL)
                         .value(literal.toString())
                         .build());
      }
    }

    return retval;
  }

  boolean parseStar(List<Segment> segments) {
    if (!consume('*')) {
      return false;
    }

    if (current < pattern.length() && '*' == peek()) {
      consume();
      segments.add(
          Segment.builder().type(SegmentType.WILDCARD).value("**").build());

      wildcard = true;
    } else {
      segments.add(Segment.builder().type(SegmentType.STAR).value("*").build());
    }

    return true;
  }

  boolean parseVariable(List<Segment> segments) {
    if (!consume('{')) {
      return false;
    }

    Segment.SegmentBuilder builder =
        Segment.builder().type(SegmentType.VARIABLE);

    if (!parseVariableName(builder)) {
      return false;
    }

    if (current < pattern.length() && '=' == peek()) {
      consume();

      if (!parseVariableValue(builder)) {
        return false;
      }
    } else {
      builder.segment(
          Segment.builder().type(SegmentType.STAR).value("*").build());
    }

    segments.add(builder.build());

    if (!consume('}')) {
      return false;
    }

    return true;
  }

  boolean parseVariableName(Segment.SegmentBuilder builder) {
    StringBuilder nameBuilder = new StringBuilder();

    if (!parseIdent(nameBuilder)) {
      return false;
    }

    while (current < pattern.length() && '.' == peek()) {
      consume();
      nameBuilder.append(".");

      if (!parseIdent(nameBuilder)) {
        return false;
      }
    }

    builder.value(nameBuilder.toString());

    return true;
  }

  boolean parseIdent(StringBuilder nameBuilder) {
    if (current >= pattern.length()) {
      return false;
    }

    while (current < pattern.length()) {
      char c = peek();

      switch (c) {
      case '.':
      case '}':
      case '=':
        return true;
      default:
        nameBuilder.append(c);
        consume();
      }
    }

    return true;
  }

  boolean parseVariableValue(Segment.SegmentBuilder builder) {
    if (current < pattern.length() && '/' == peek()) {
      consume();
    }

    if (current < pattern.length()) {
      List<Segment> segments = new ArrayList<>();

      if (!parseSegments(segments)) {
        return false;
      }

      if (segments.isEmpty()) {
        return false;
      }

      builder.segments(segments);
      return true;
    }

    return false;
  }

  boolean parseLiteral(StringBuilder literal) {
    if (current >= pattern.length()) {
      return false;
    }

    while (current < pattern.length()) {
      char c = peek();

      switch (c) {
      case '/':
        return true;
      default:
        literal.append(c);
        consume();
      }
    }

    return true;
  }

  boolean parseVerb() {
    if (current >= pattern.length()) {
      return false;
    }

    StringBuilder literal = new StringBuilder();

    if (!parseLiteral(literal)) {
      return false;
    }

    this.verb = literal.toString();

    return !verb.isEmpty();
  }

  public enum SegmentType { STAR, WILDCARD, LITERAL, VARIABLE }

  @Getter
  @Setter
  @Builder
  public static class Segment {
    SegmentType type;
    String value;
    @Singular List<Segment> segments;
  }
}

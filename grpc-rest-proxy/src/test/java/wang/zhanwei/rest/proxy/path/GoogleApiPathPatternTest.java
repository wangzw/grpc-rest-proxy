package wang.zhanwei.rest.proxy.path;

import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import wang.zhanwei.rest.proxy.path.GoogleApiPathPattern.Segment;
import wang.zhanwei.rest.proxy.path.GoogleApiPathPattern.SegmentType;

public class GoogleApiPathPatternTest {
  @Test
  public void testPositive() throws Exception {
    GoogleApiPathPattern pathPattern = new GoogleApiPathPattern("/");
    Assert.assertTrue(pathPattern.isValid());
    Assert.assertFalse(pathPattern.isPattern());
    Assert.assertEquals(0, pathPattern.getSegments().size());

    pathPattern = new GoogleApiPathPattern("/cab");
    Assert.assertTrue(pathPattern.isValid());
    Assert.assertFalse(pathPattern.isPattern());
    Assert.assertEquals(1, pathPattern.getSegments().size());
    Assert.assertEquals("cab", pathPattern.getSegments().get(0).getValue());

    pathPattern = new GoogleApiPathPattern("/abc/def");
    Assert.assertTrue(pathPattern.isValid());
    Assert.assertFalse(pathPattern.isPattern());
    Assert.assertEquals(2, pathPattern.getSegments().size());
    Assert.assertEquals("abc", pathPattern.getSegments().get(0).getValue());
    Assert.assertEquals("def", pathPattern.getSegments().get(1).getValue());

    pathPattern = new GoogleApiPathPattern("/v1/{message_id}");
    Assert.assertTrue(pathPattern.isValid());
    Assert.assertTrue(pathPattern.isPattern());
    List<Segment> segments = pathPattern.getSegments();
    Assert.assertEquals(2, segments.size());
    AssertLiteral(segments.get(0), "v1");
    AssertVariable(segments.get(1), "message_id", 1);
    List<Segment> subSegments = segments.get(1).getSegments();
    AssertStar(subSegments.get(0));

    pathPattern = new GoogleApiPathPattern("/v1/{message_id=*}");
    Assert.assertTrue(pathPattern.isValid());
    Assert.assertTrue(pathPattern.isPattern());
    segments = pathPattern.getSegments();
    Assert.assertEquals(2, segments.size());
    AssertLiteral(segments.get(0), "v1");
    AssertVariable(segments.get(1), "message_id", 1);
    subSegments = segments.get(1).getSegments();
    AssertStar(subSegments.get(0));

    pathPattern = new GoogleApiPathPattern("/abc/*/def");
    Assert.assertTrue(pathPattern.isValid());
    Assert.assertTrue(pathPattern.isPattern());
    Assert.assertEquals(3, pathPattern.getSegments().size());
    Assert.assertEquals("abc", pathPattern.getSegments().get(0).getValue());
    AssertStar(pathPattern.getSegments().get(1));
    Assert.assertEquals("def", pathPattern.getSegments().get(2).getValue());

    pathPattern = new GoogleApiPathPattern("/abc/*/*/def");
    Assert.assertTrue(pathPattern.isValid());
    Assert.assertTrue(pathPattern.isPattern());
    Assert.assertEquals(4, pathPattern.getSegments().size());
    Assert.assertEquals("abc", pathPattern.getSegments().get(0).getValue());
    AssertStar(pathPattern.getSegments().get(1));
    AssertStar(pathPattern.getSegments().get(2));
    Assert.assertEquals("def", pathPattern.getSegments().get(3).getValue());

    pathPattern = new GoogleApiPathPattern("/abc/**");
    Assert.assertTrue(pathPattern.isValid());
    Assert.assertTrue(pathPattern.isPattern());
    Assert.assertEquals(2, pathPattern.getSegments().size());
    Assert.assertEquals("abc", pathPattern.getSegments().get(0).getValue());
    AssertWildCard(pathPattern.getSegments().get(1));

    pathPattern = new GoogleApiPathPattern("/abc/{name=/**}");
    Assert.assertTrue(pathPattern.isValid());
    Assert.assertTrue(pathPattern.isPattern());
    segments = pathPattern.getSegments();
    Assert.assertEquals(2, segments.size());
    Assert.assertEquals("abc", segments.get(0).getValue());
    AssertVariable(pathPattern.getSegments().get(1), "name", 1);
    subSegments = segments.get(1).getSegments();
    AssertWildCard(subSegments.get(0));

    pathPattern = new GoogleApiPathPattern("/abc/{name=/abc/**}");
    Assert.assertTrue(pathPattern.isValid());
    Assert.assertTrue(pathPattern.isPattern());
    segments = pathPattern.getSegments();
    Assert.assertEquals(2, segments.size());
    Assert.assertEquals("abc", segments.get(0).getValue());
    AssertVariable(pathPattern.getSegments().get(1), "name", 2);
    subSegments = segments.get(1).getSegments();
    AssertLiteral(subSegments.get(0), "abc");
    AssertWildCard(subSegments.get(1));

    pathPattern = new GoogleApiPathPattern("/v1/messages/{message_id}");
    Assert.assertTrue(pathPattern.isValid());
    Assert.assertTrue(pathPattern.isPattern());
    segments = pathPattern.getSegments();
    Assert.assertEquals(3, segments.size());
    AssertLiteral(segments.get(0), "v1");
    AssertLiteral(segments.get(1), "messages");
    AssertVariable(segments.get(2), "message_id", 1);
    subSegments = segments.get(2).getSegments();
    AssertStar(subSegments.get(0));

    pathPattern = new GoogleApiPathPattern("/v1/{name=messages/*}");
    Assert.assertTrue(pathPattern.isValid());
    Assert.assertTrue(pathPattern.isPattern());
    segments = pathPattern.getSegments();
    Assert.assertEquals(2, segments.size());
    AssertLiteral(segments.get(0), "v1");
    AssertVariable(segments.get(1), "name", 2);
    subSegments = segments.get(1).getSegments();
    AssertLiteral(subSegments.get(0), "messages");
    AssertStar(subSegments.get(1));

    pathPattern = new GoogleApiPathPattern("/v1/messages/{message_id}/{sub.subfield}");
    Assert.assertTrue(pathPattern.isValid());
    Assert.assertTrue(pathPattern.isPattern());
    segments = pathPattern.getSegments();
    Assert.assertEquals(4, segments.size());
    AssertLiteral(segments.get(0), "v1");
    AssertLiteral(segments.get(1), "messages");
    AssertVariable(segments.get(2), "message_id", 1);
    subSegments = segments.get(2).getSegments();
    AssertStar(subSegments.get(0));
    AssertVariable(segments.get(3), "sub.subfield", 1);
    subSegments = segments.get(3).getSegments();
    AssertStar(subSegments.get(0));

    pathPattern = new GoogleApiPathPattern("/v1/messages/{message_id}/{sub.subfield.xxx}:verb");
    Assert.assertTrue(pathPattern.isValid());
    Assert.assertTrue(pathPattern.isPattern());
    segments = pathPattern.getSegments();
    Assert.assertEquals(4, segments.size());
    AssertLiteral(segments.get(0), "v1");
    AssertLiteral(segments.get(1), "messages");
    AssertVariable(segments.get(2), "message_id", 1);
    subSegments = segments.get(2).getSegments();
    AssertStar(subSegments.get(0));
    AssertVariable(segments.get(3), "sub.subfield.xxx", 1);
    subSegments = segments.get(3).getSegments();
    AssertStar(subSegments.get(0));
    Assert.assertEquals("verb", pathPattern.getVerb());
  }

  @Test
  public void testNegative() throws Exception {
    GoogleApiPathPattern pathPattern = new GoogleApiPathPattern("/abc/{def");
    Assert.assertFalse(pathPattern.isValid());
    Assert.assertFalse(pathPattern.isPattern());

    pathPattern = new GoogleApiPathPattern("/abc/*def");
    Assert.assertFalse(pathPattern.isValid());
    Assert.assertFalse(pathPattern.isPattern());

    pathPattern = new GoogleApiPathPattern("/abc/**/");
    Assert.assertFalse(pathPattern.isValid());
    Assert.assertFalse(pathPattern.isPattern());

    pathPattern = new GoogleApiPathPattern("/abc/{def=**/}");
    Assert.assertFalse(pathPattern.isValid());
    Assert.assertFalse(pathPattern.isPattern());

    pathPattern = new GoogleApiPathPattern("/abc/{def=/}");
    Assert.assertFalse(pathPattern.isValid());
    Assert.assertFalse(pathPattern.isPattern());
  }

  @Test
  public void testNestedVariable() throws Exception {
    GoogleApiPathPattern pathPattern = new GoogleApiPathPattern("/abc/{name1=/def/{name2}}");
    Assert.assertTrue(pathPattern.isValid());
    Assert.assertTrue(pathPattern.isPattern());

    List<Segment> segments = pathPattern.getSegments();
    Assert.assertEquals(2, segments.size());
    AssertLiteral(segments.get(0), "abc");
    AssertVariable(segments.get(1), "name1", 2);

    List<Segment> subSegments = segments.get(1).getSegments();
    AssertLiteral(subSegments.get(0), "def");
    AssertVariable(subSegments.get(1), "name2", 1);
    AssertStar(subSegments.get(1).getSegments().get(0));
  }

  void AssertStar(Segment segment) {
    Assert.assertEquals(SegmentType.STAR, segment.getType());
    Assert.assertEquals("*", segment.getValue());
  }

  void AssertWildCard(Segment segment) {
    Assert.assertEquals(SegmentType.WILDCARD, segment.getType());
    Assert.assertEquals("**", segment.getValue());
  }

  void AssertLiteral(Segment segment, String value) {
    Assert.assertEquals(SegmentType.LITERAL, segment.getType());
    Assert.assertEquals(value, segment.getValue());
  }

  void AssertVariable(Segment segment, String name, int size) {
    Assert.assertEquals(SegmentType.VARIABLE, segment.getType());
    Assert.assertEquals(name, segment.getValue());
    Assert.assertEquals(size, segment.getSegments().size());
  }
}

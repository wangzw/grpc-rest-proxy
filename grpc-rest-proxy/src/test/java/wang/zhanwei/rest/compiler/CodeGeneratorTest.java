package wang.zhanwei.rest.compiler;

import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Test;

public class CodeGeneratorTest {
  @Test
  public void testPositive() throws Exception {
    Files.readAllBytes(Paths.get("src", "test", "resources", "example.in")).toString();
  }
}

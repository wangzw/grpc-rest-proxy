package wang.zhanwei.rest.plugin;

import java.io.FileInputStream;
import java.nio.file.Paths;
import org.apache.commons.io.output.NullOutputStream;
import org.junit.Test;

public class PluginTest {
  @Test
  public void testPositive() throws Exception {
    FileInputStream fin =
        new FileInputStream(Paths.get("src", "test", "resources", "example.in").toFile());
    NullOutputStream fout = new NullOutputStream();

    Plugin plugin = new Plugin();
    plugin.compile(fin, fout);
  }
}

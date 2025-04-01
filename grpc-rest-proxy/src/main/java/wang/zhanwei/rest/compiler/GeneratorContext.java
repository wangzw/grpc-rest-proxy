package wang.zhanwei.rest.compiler;

import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse.File;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder
public class GeneratorContext {
  CodeGeneratorResponse.Builder responseBuilder;
  File.Builder fileBuild;
  OutputStream currentOutputStream;

  public void addNewFile(String name) {
    complete();

    fileBuild = responseBuilder.addFileBuilder();
    fileBuild.setName(name);
    currentOutputStream = new ByteArrayOutputStream();
  }

  public void complete() {
    if (fileBuild != null) {
      fileBuild.setContent(currentOutputStream.toString());
      fileBuild = null;
      currentOutputStream = null;
    }
  }

  public void write(String content) throws IOException {
    currentOutputStream.write(content.getBytes(Charset.forName("UTF-8")));
  }
}

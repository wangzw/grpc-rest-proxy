package wang.zhanwei.rest.plugin;

import com.google.api.AnnotationsProto;
import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.DescriptorProtos.FileDescriptorProto;
import com.google.protobuf.DescriptorProtos.FileDescriptorSet;
import com.google.protobuf.Descriptors.DescriptorValidationException;
import com.google.protobuf.Descriptors.FileDescriptor;
import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorRequest;
import com.google.protobuf.compiler.PluginProtos.CodeGeneratorResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import wang.zhanwei.rest.compiler.CodeGenerator;
import wang.zhanwei.rest.compiler.GeneratorContext;

public class Plugin {
  CodeGenerator codeGenerator = new CodeGenerator();

  public static void main(String[] args) throws IOException, DescriptorValidationException {
    new Plugin().compile(System.in, System.out);
  }

  void compile(InputStream inputStream, OutputStream outputStream)
      throws IOException, DescriptorValidationException {
    ExtensionRegistry registry = ExtensionRegistry.newInstance();
    registry.add(AnnotationsProto.http);

    CodeGeneratorRequest request = CodeGeneratorRequest.parseFrom(inputStream, registry);
    CodeGeneratorResponse.Builder builder = CodeGeneratorResponse.newBuilder();

    compile(request, builder);
    builder.build().writeTo(outputStream);
  }

  @VisibleForTesting
  void compile(CodeGeneratorRequest request, CodeGeneratorResponse.Builder builder)
      throws DescriptorValidationException {
    try {
      List<FileDescriptorProto> fileDescriptorProtoList = request.getProtoFileList();
      FileDescriptorSet descriptorSet =
          FileDescriptorSet.newBuilder().addAllFile(fileDescriptorProtoList).build();
      GeneratorContext context = GeneratorContext.builder().responseBuilder(builder).build();

      for (String file : request.getFileToGenerateList()) {
        for (FileDescriptorProto proto : request.getProtoFileList()) {
          if (proto.getName().equals(file)) {
            FileDescriptor descriptor = buildFileDescriptor(proto, descriptorSet);

            if (!generate(descriptor, context)) {
              return;
            }
          }
        }
      }
    } catch (IOException e) {
      builder.setError(e.getMessage());
    }
  }

  boolean generate(FileDescriptor descriptor, GeneratorContext context) throws IOException {
    if (!codeGenerator.generate(descriptor, context)) {
      CodeGeneratorResponse.Builder builder = context.getResponseBuilder();

      if (!builder.hasError()) {
        builder.setError("Failed but no error message set.");
      }

      return false;
    }

    context.complete();

    return true;
  }

  FileDescriptorProto findFileDescriptorProto(String name, FileDescriptorSet descriptorSet) {
    for (FileDescriptorProto proto : descriptorSet.getFileList()) {
      if (proto.getName().equals(name)) {
        return proto;
      }
    }

    return null;
  }

  FileDescriptor buildFileDescriptor(FileDescriptorProto proto, FileDescriptorSet descriptorSet)
      throws DescriptorValidationException {
    List<FileDescriptor> dependencies = new ArrayList<>();

    for (String p : proto.getDependencyList()) {
      FileDescriptor fileDescriptor =
          buildFileDescriptor(findFileDescriptorProto(p, descriptorSet), descriptorSet);
      dependencies.add(fileDescriptor);
    }

    return FileDescriptor.buildFrom(proto, dependencies.toArray(new FileDescriptor[0]), false);
  }
}

package wang.zhanwei.rest.proxy.translator;

import com.google.api.HttpRule;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;

public class RestResponseTranslator implements ResponseTranslator {
  protected JsonFormat.Printer printer =
      JsonFormat.printer().includingDefaultValueFields().preservingProtoFieldNames();

  public ResponseEntity<String> translate(HttpRule rule, Message responseMessage) {
    try {
      BodyBuilder builder =
          ResponseEntity.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON);

      if (responseMessage.getDescriptorForType().getFields().isEmpty()) {
        return builder.build();
      }

      return builder.body(printer.print(responseMessage));
    } catch (InvalidProtocolBufferException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
  }
}

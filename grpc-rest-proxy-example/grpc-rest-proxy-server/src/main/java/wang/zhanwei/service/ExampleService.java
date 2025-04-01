package wang.zhanwei.service;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.security.access.prepost.PreAuthorize;
import wang.zhanwei.example.proto.Empty;
import wang.zhanwei.example.proto.ExampleServiceGrpc;
import wang.zhanwei.example.proto.GetMessageRequest1;
import wang.zhanwei.example.proto.GetMessageRequest2;
import wang.zhanwei.example.proto.GetMessageRequest3;
import wang.zhanwei.example.proto.Message1;
import wang.zhanwei.example.proto.Message2;
import wang.zhanwei.example.proto.UpdateMessageRequest;

@GrpcService
public class ExampleService extends ExampleServiceGrpc.ExampleServiceImplBase {
  @Override
  public void internalMessage(Empty request, StreamObserver<Empty> responseObserver) {
    super.internalMessage(request, responseObserver);
  }

  @Override
  @PreAuthorize("hasAuthority('ROLE_SUPERVISOR')")
  public void emptyMessage(Empty request, StreamObserver<Empty> responseObserver) {
    super.emptyMessage(request, responseObserver);
  }

  @Override
  public void getMessage1(GetMessageRequest1 request, StreamObserver<Message1> responseObserver) {
    super.getMessage1(request, responseObserver);
  }

  @Override
  public void getMessage2(GetMessageRequest2 request, StreamObserver<Message1> responseObserver) {
    super.getMessage2(request, responseObserver);
  }

  @Override
  public void updateMessage1(
      UpdateMessageRequest request, StreamObserver<Message1> responseObserver) {
    super.updateMessage1(request, responseObserver);
  }

  @Override
  public void updateMessage2(Message2 request, StreamObserver<Message2> responseObserver) {
    super.updateMessage2(request, responseObserver);
  }

  @Override
  public void getMessage3(GetMessageRequest3 request, StreamObserver<Message2> responseObserver) {
    super.getMessage3(request, responseObserver);
  }
}

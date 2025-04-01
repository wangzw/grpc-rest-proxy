package wang.zhanwei.service;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import wang.zhanwei.example.proto.ExampleServiceGrpc;

@Service
public class ExampleClientService {
  @GrpcClient("example") ExampleServiceGrpc.ExampleServiceBlockingStub client;
}

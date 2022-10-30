package MyProtoBuf

object findTimeStampInLogsGrpc {
  val METHOD_FETCH_TIME_STAMP_IN_LOGS: _root_.io.grpc.MethodDescriptor[MyProtoBuf.mesageRequest, MyProtoBuf.messageResponse] =
    _root_.io.grpc.MethodDescriptor.newBuilder()
      .setType(_root_.io.grpc.MethodDescriptor.MethodType.UNARY)
      .setFullMethodName(_root_.io.grpc.MethodDescriptor.generateFullMethodName("findTimeStampInLogs", "fetchTimeStampInLogs"))
      .setSampledToLocalTracing(true)
      .setRequestMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[MyProtoBuf.mesageRequest])
      .setResponseMarshaller(_root_.scalapb.grpc.Marshaller.forMessage[MyProtoBuf.messageResponse])
      .setSchemaDescriptor(_root_.scalapb.grpc.ConcreteProtoMethodDescriptorSupplier.fromMethodDescriptor(MyProtoBuf.MyProtoBufProto.javaDescriptor.getServices().get(0).getMethods().get(0)))
      .build()
  
  val SERVICE: _root_.io.grpc.ServiceDescriptor =
    _root_.io.grpc.ServiceDescriptor.newBuilder("findTimeStampInLogs")
      .setSchemaDescriptor(new _root_.scalapb.grpc.ConcreteProtoFileDescriptorSupplier(MyProtoBuf.MyProtoBufProto.javaDescriptor))
      .addMethod(METHOD_FETCH_TIME_STAMP_IN_LOGS)
      .build()
  
  trait findTimeStampInLogs extends _root_.scalapb.grpc.AbstractService {
    override def serviceCompanion = findTimeStampInLogs
    def fetchTimeStampInLogs(request: MyProtoBuf.mesageRequest): scala.concurrent.Future[MyProtoBuf.messageResponse]
  }
  
  object findTimeStampInLogs extends _root_.scalapb.grpc.ServiceCompanion[findTimeStampInLogs] {
    implicit def serviceCompanion: _root_.scalapb.grpc.ServiceCompanion[findTimeStampInLogs] = this
    def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = MyProtoBuf.MyProtoBufProto.javaDescriptor.getServices().get(0)
    def scalaDescriptor: _root_.scalapb.descriptors.ServiceDescriptor = MyProtoBuf.MyProtoBufProto.scalaDescriptor.services(0)
    def bindService(serviceImpl: findTimeStampInLogs, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition =
      _root_.io.grpc.ServerServiceDefinition.builder(SERVICE)
      .addMethod(
        METHOD_FETCH_TIME_STAMP_IN_LOGS,
        _root_.io.grpc.stub.ServerCalls.asyncUnaryCall(new _root_.io.grpc.stub.ServerCalls.UnaryMethod[MyProtoBuf.mesageRequest, MyProtoBuf.messageResponse] {
          override def invoke(request: MyProtoBuf.mesageRequest, observer: _root_.io.grpc.stub.StreamObserver[MyProtoBuf.messageResponse]): _root_.scala.Unit =
            serviceImpl.fetchTimeStampInLogs(request).onComplete(scalapb.grpc.Grpc.completeObserver(observer))(
              executionContext)
        }))
      .build()
  }
  
  trait findTimeStampInLogsBlockingClient {
    def serviceCompanion = findTimeStampInLogs
    def fetchTimeStampInLogs(request: MyProtoBuf.mesageRequest): MyProtoBuf.messageResponse
  }
  
  class findTimeStampInLogsBlockingStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[findTimeStampInLogsBlockingStub](channel, options) with findTimeStampInLogsBlockingClient {
    override def fetchTimeStampInLogs(request: MyProtoBuf.mesageRequest): MyProtoBuf.messageResponse = {
      _root_.scalapb.grpc.ClientCalls.blockingUnaryCall(channel, METHOD_FETCH_TIME_STAMP_IN_LOGS, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): findTimeStampInLogsBlockingStub = new findTimeStampInLogsBlockingStub(channel, options)
  }
  
  class findTimeStampInLogsStub(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions = _root_.io.grpc.CallOptions.DEFAULT) extends _root_.io.grpc.stub.AbstractStub[findTimeStampInLogsStub](channel, options) with findTimeStampInLogs {
    override def fetchTimeStampInLogs(request: MyProtoBuf.mesageRequest): scala.concurrent.Future[MyProtoBuf.messageResponse] = {
      _root_.scalapb.grpc.ClientCalls.asyncUnaryCall(channel, METHOD_FETCH_TIME_STAMP_IN_LOGS, options, request)
    }
    
    override def build(channel: _root_.io.grpc.Channel, options: _root_.io.grpc.CallOptions): findTimeStampInLogsStub = new findTimeStampInLogsStub(channel, options)
  }
  
  def bindService(serviceImpl: findTimeStampInLogs, executionContext: scala.concurrent.ExecutionContext): _root_.io.grpc.ServerServiceDefinition = findTimeStampInLogs.bindService(serviceImpl, executionContext)
  
  def blockingStub(channel: _root_.io.grpc.Channel): findTimeStampInLogsBlockingStub = new findTimeStampInLogsBlockingStub(channel)
  
  def stub(channel: _root_.io.grpc.Channel): findTimeStampInLogsStub = new findTimeStampInLogsStub(channel)
  
  def javaDescriptor: _root_.com.google.protobuf.Descriptors.ServiceDescriptor = MyProtoBuf.MyProtoBufProto.javaDescriptor.getServices().get(0)
  
}
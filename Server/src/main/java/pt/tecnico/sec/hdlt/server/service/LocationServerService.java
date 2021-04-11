package pt.tecnico.sec.hdlt.server.service;

import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.communication.*;
import pt.tecnico.sec.hdlt.server.bll.LocationBL;

import java.security.InvalidParameterException;

public class LocationServerService extends LocationServerGrpc.LocationServerImplBase {

    private LocationBL locationBL;

    public LocationServerService(LocationBL locationBL) {
        this.locationBL = locationBL;
    }
    // https://grpc.github.io/grpc/core/md_doc_statuscodes.html

    @Override
    public void submitLocationReport(SubmitLocationReportRequest request, StreamObserver<SubmitLocationReportResponse> responseObserver) {
        try {
            this.locationBL.submitLocationReport(request.getEncryptedSignedLocationReport().toByteArray(), request.getUserId());

            responseObserver.onNext(SubmitLocationReportResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (InvalidParameterException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void obtainLocationReport(ObtainLocationReportRequest request, StreamObserver<ObtainLocationReportResponse> responseObserver) {
        try {
            responseObserver.onNext(this.locationBL.obtainLocationReport(request.getEncryptedSignedLocationQuery().toByteArray()));
            responseObserver.onCompleted();
//        } catch () {
//            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription(e.getMessage()).asRuntimeException());
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void obtainUsersAtLocation(ObtainUsersAtLocationRequest request, StreamObserver<ObtainUsersAtLocationResponse> responseObserver) {
        super.obtainUsersAtLocation(request, responseObserver);
    }
}

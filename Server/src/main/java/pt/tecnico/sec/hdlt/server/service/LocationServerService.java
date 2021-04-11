package pt.tecnico.sec.hdlt.server.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.communication.*;
import pt.tecnico.sec.hdlt.server.bll.LocationBL;

public class LocationServerService extends LocationServerGrpc.LocationServerImplBase {

    private LocationBL locationBL;

    public LocationServerService(LocationBL locationBL) {
        this.locationBL = locationBL;
    }
    // https://grpc.github.io/grpc/core/md_doc_statuscodes.html

    @Override
    public void submitLocationReport(SubmitLocationReportRequest request, StreamObserver<SubmitLocationReportResponse> responseObserver) {
        try {
            this.locationBL.submitLocationReport(request.getEncryptedSignedLocationReport().toByteArray());

            responseObserver.onNext(SubmitLocationReportResponse.newBuilder().build());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void obtainLocationReport(ObtainLocationReportRequest request, StreamObserver<ObtainLocationReportResponse> responseObserver) {
        super.obtainLocationReport(request, responseObserver);
    }

    @Override
    public void obtainUsersAtLocation(ObtainUsersAtLocationRequest request, StreamObserver<ObtainUsersAtLocationResponse> responseObserver) {
        super.obtainUsersAtLocation(request, responseObserver);
    }
}

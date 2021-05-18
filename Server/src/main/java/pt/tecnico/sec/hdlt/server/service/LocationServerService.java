package pt.tecnico.sec.hdlt.server.service;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.communication.*;
import pt.tecnico.sec.hdlt.server.bll.LocationBL;

// https://grpc.github.io/grpc/core/md_doc_statuscodes.html
public class LocationServerService extends LocationServerGrpc.LocationServerImplBase {

    private final LocationBL locationBL;

    public LocationServerService(LocationBL locationBL) {
        this.locationBL = locationBL;
    }

    @Override
    public void submitLocationReport(SubmitLocationReportRequest request, StreamObserver<SubmitLocationReportResponse> responseObserver) {
        try {
            SubmitLocationReportResponse response = this.locationBL.submitLocationReport(request);

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            // TODO verify if this cancels the request without answering to the client
            responseObserver.onCompleted();
        }
    }

    @Override
    public void obtainLocationReport(ObtainLocationReportRequest request, StreamObserver<ObtainLocationReportResponse> responseObserver) {
        try {
            responseObserver.onNext(this.locationBL.obtainLocationReport(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            // TODO verify if this cancels the request without answering to the client
            responseObserver.onCompleted();
        }
    }

    @Override
    public void obtainUsersAtLocation(ObtainUsersAtLocationRequest request, StreamObserver<ObtainUsersAtLocationResponse> responseObserver) {
        try {
            responseObserver.onNext(this.locationBL.obtainUsersAtLocation(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            // TODO verify if this cancels the request without answering to the client
            responseObserver.onCompleted();
        }
    }

    @Override
    public void requestMyProofs(RequestMyProofsRequest request, StreamObserver<RequestMyProofsResponse> responseObserver) {
        try {
            responseObserver.onNext(this.locationBL.requestMyProofs(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            // TODO verify if this cancels the request without answering to the client
            responseObserver.onCompleted();
        }
    }

    @Override
    public void echo(EchoRequest request, StreamObserver<EchoResponse> responseObserver) {
        try {
            responseObserver.onNext(this.locationBL.echo(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            // TODO verify if this cancels the request without answering to the client
            responseObserver.onCompleted();
        }
    }

    @Override
    public void ready(ReadyRequest request, StreamObserver<ReadyResponse> responseObserver) {
        try {
            responseObserver.onNext(this.locationBL.ready(request));
            responseObserver.onCompleted();
        } catch (Exception e) {
            // TODO verify if this cancels the request without answering to the client
            responseObserver.onCompleted();
        }
    }
}

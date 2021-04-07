package pt.tecnico.sec.hdlt.server.service;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.communication.*;
import pt.tecnico.sec.hdlt.server.utils.WriteQueue;

import java.util.concurrent.ConcurrentHashMap;

public class LocationServerService extends LocationServerGrpc.LocationServerImplBase {

    private WriteQueue<LocationReport> writeQueue;
    private ConcurrentHashMap<String, LocationReport> locationReports;

    public LocationServerService(WriteQueue<LocationReport> writeQueue, ConcurrentHashMap<String, LocationReport> locationReports) {
        this.writeQueue = writeQueue;
        this.locationReports = locationReports;
    }

    @Override
    public void submitLocationReport(SubmitLocationReportRequest request, StreamObserver<SubmitLocationReportResponse> responseObserver) {
        super.submitLocationReport(request, responseObserver);

//        writeQueue.write(LocationProof.newBuilder().setWitness("Andre").setProver("Ze").setEpoch(1).build());
//        writeQueue.write(LocationProof.newBuilder().setWitness("Manel").setProver("Ze").setEpoch(1).build());

//        try {
//
//        } catch (Exception e) {
//            responseObserver.onError(Status.DATA_LOSS.withDescription(e.getMessage()).asRuntimeException());
//            // https://grpc.github.io/grpc/core/md_doc_statuscodes.html
//        }
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

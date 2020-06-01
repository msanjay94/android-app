package com.dal.mc.servicegenie;

public class serviceRequest {
    public String RequestedServiceName;
    public String RequestedBy;
    public String RequestedByEmailId;
    public String AdditionalComment;

    public serviceRequest(){

    }
    public serviceRequest(String requestedServiceName, String requestedBy, String requestedByEmailId, String additionalComment) {
        RequestedServiceName = requestedServiceName;
        RequestedBy = requestedBy;
        RequestedByEmailId = requestedByEmailId;
        AdditionalComment = additionalComment;
    }
}

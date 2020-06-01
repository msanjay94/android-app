package com.dal.mc.servicegenie;

import java.util.Calendar;

public class Booking {

    private String RequestedServiceName,RequestStatus,RequestProfName,RequestedByEmailId,RequestTimeandDate,RequestCost;
    //private Calendar timeNDate;
    //private Float cost;

    //Getter method for service name
    public String getRequestedServiceName() {
        return RequestedServiceName;
    }

    //Setter method for service name
    public void setRequestedServiceName(String requestedServiceName) {
        RequestedServiceName = requestedServiceName;
    }

    public String getRequestStatus() {
        return RequestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        RequestStatus = requestStatus;
    }

    public String getRequestProfName() {
        return RequestProfName;
    }

    public void setRequestProfName(String requestProfName) {
        RequestProfName = requestProfName;
    }

    public String getRequestedByEmailId() {
        return RequestedByEmailId;
    }

    public void setRequestedByEmailId(String requestByEmailId) {
        RequestedByEmailId = requestByEmailId;
    }

    public String getRequestTimeandDate() {
        return RequestTimeandDate;
    }

    public void setRequestTimeandDate(String requestTimeandDate) {
        RequestTimeandDate = requestTimeandDate;
    }

    public String getRequestCost() {
        return RequestCost;
    }

    public void setRequestCost(String requestCost) {
        RequestCost = requestCost;
    }
}

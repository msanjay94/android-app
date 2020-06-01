package com.dal.mc.servicegenie;

public class User {
    private String uid;
    private String phoneNumber;
    private String displayName;
    private String streetAddress;
    private String address2;
    private String city;
    private String province;
    private String country;
    private String postalCode;
    private String profilePicEncoded;

    public User() {

    }

    public User(String uid, String phoneNumber, String displayName, String streetAddress, String address2, String city, String province, String country, String postalCode, String profilePicEncoded) {
        this.uid = uid;
        this.displayName = displayName;
        this.streetAddress = streetAddress;
        this.address2 = address2;
        this.city = city;
        this.province = province;
        this.country = country;
        this.postalCode = postalCode;
        this.profilePicEncoded = profilePicEncoded;
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getProfilePicEncoded() {
        return profilePicEncoded;
    }
    public void setDisplayName(String name) {
        this.displayName=name;
    }

    public String getUid() {
        return uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getAddress2() {
        return address2;
    }

    public String getCity() {
        return city;
    }

    public String getProvince() {
        return province;
    }

    public String getCountry() {
        return country;
    }

    public String getPostalCode() {
        return postalCode;
    }
}

package com.example.psinder.models;

public class UserSetupModel {

    private String username;
    private String fullname;
    private String country;
    private String status;
    private String gender;
    private String dob;
    private String relationshipstatus;

    public UserSetupModel() {
    }

    public UserSetupModel(String username, String fullname, String country, String status, String gender, String dob, String relationshipstatus) {
        this.username = username;
        this.fullname = fullname;
        this.country = country;
        this.status = status;
        this.gender = gender;
        this.dob = dob;
        this.relationshipstatus = relationshipstatus;
    }

    public String getUsername() {
        return username;
    }

    public UserSetupModel setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getFullname() {
        return fullname;
    }

    public UserSetupModel setFullname(String fullname) {
        this.fullname = fullname;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public UserSetupModel setCountry(String country) {
        this.country = country;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public UserSetupModel setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getGender() {
        return gender;
    }

    public UserSetupModel setGender(String gender) {
        this.gender = gender;
        return this;
    }

    public String getDob() {
        return dob;
    }

    public UserSetupModel setDob(String dob) {
        this.dob = dob;
        return this;
    }

    public String getRelationshipstatus() {
        return relationshipstatus;
    }

    public UserSetupModel setRelationshipstatus(String relationshipstatus) {
        this.relationshipstatus = relationshipstatus;
        return this;
    }
}

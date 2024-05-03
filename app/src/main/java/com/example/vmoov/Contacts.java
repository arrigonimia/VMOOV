package com.example.vmoov;

public class Contacts {
    private String contactId; // Unique ID for the contact
    private String contactName;
    private String relationship;
    private String phoneNumber;
    private String contactEmail;
    private int alertFlag;
    private int emergencyContactFlag;

    // Constructors
    public Contacts(String contactId, String contactName, String relationship, String phoneNumber, String contactEmail, Integer alertFlag, Integer emergencyContactFlag) {
        this.contactId = contactId;
        this.contactName = contactName;
        this.relationship = relationship;
        this.phoneNumber = phoneNumber;
        this.contactEmail = contactEmail;
        this.alertFlag = alertFlag;
        this.emergencyContactFlag = emergencyContactFlag;
    }
    public Contacts() {
        // Empty constructor
    }

    // Getters and Setters
    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }
    public int getAlertFlag() {
        return alertFlag;
    }

    public void setAlertFlag(int alertFlag) {
        this.alertFlag = alertFlag;
    }
    public int getEmergencyContactFlag() {
        return emergencyContactFlag;
    }

    public void setEmergencyContactFlag(int emergencyContactFlag) {
        this.emergencyContactFlag = emergencyContactFlag;
    }
}

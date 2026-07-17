package com.rituraj.sevamitra.models;

import java.util.ArrayList;

public class UserData {
    private String id;
    private String userType;
    private String fullName;
    private String email;
    private String phone, profileUrl;
    private String address;
    private String state;
    private String city;
    private String password;

    // SevaMitra specific
    private String department;
    private String designation;

    // Worker specific
    private ArrayList<String> skills;
    private String founderId;
    private String isSelected;

    // Founder specific
    private String companyName;
    private String gstNumber;
    private String officeAddress;

    // Officer specific
    private String district;
    private String division;

    public UserData() {
    }

    public UserData(String id, String userType, String fullName, String email, String phone, String address, String state, String city, String password, String department, String designation, ArrayList<String> skills, String founderId, String isSelected, String companyName, String gstNumber, String officeAddress, String district, String division) {
        this.id = id;
        this.userType = userType;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.state = state;
        this.city = city;
        this.password = password;
        this.department = department;
        this.designation = designation;
        this.skills = skills;
        this.founderId = founderId;
        this.isSelected = isSelected;
        this.companyName = companyName;
        this.gstNumber = gstNumber;
        this.officeAddress = officeAddress;
        this.district = district;
        this.division = division;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public ArrayList<String> getSkills() {
        return skills;
    }

    public void setSkills(ArrayList<String> skills) {
        this.skills = skills;
    }

    public String getFounderId() {
        return founderId;
    }

    public void setFounderId(String founderId) {
        this.founderId = founderId;
    }

    public String getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(String isSelected) {
        this.isSelected = isSelected;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }

    public String getOfficeAddress() {
        return officeAddress;
    }

    public void setOfficeAddress(String officeAddress) {
        this.officeAddress = officeAddress;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    @Override
    public String toString() {
        return fullName + " , " + phone + " , " + city;
    }
}

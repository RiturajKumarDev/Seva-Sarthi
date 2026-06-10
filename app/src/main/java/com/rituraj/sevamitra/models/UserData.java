package com.rituraj.sevamitra.models;

import java.util.ArrayList;

public class UserData {
    private String id;
    private String userType;
    private String fullName;
    private String profileUrl;
    private String email;
    private String phone;
    private String address;
    private String aadharNumber;
    private String state;
    private String city;
    private String password;

    // Officer specific
    private String department;
    private String designation;
    private String employeeId;

    // Worker specific
    private String primaryCategory;
    private ArrayList<String> categories;
    private String experience;
    private String specialization;
    private String hourlyRate;
    private String status;
    private String founderId;

    // Founder specific
    private String companyName;
    private String gstNumber;
    private String officeAddress;

    // SDM specific
    private String district;
    private String division;
    private String govtId;

    public UserData() {
    }

    public UserData(String id, String userType, String fullName, String email, String phone, String address, String aadharNumber, String state, String city, String password, String department, String designation, String employeeId, String primaryCategory, ArrayList<String> categories, String experience, String specialization, String hourlyRate, String companyName, String gstNumber, String officeAddress, String district, String division, String govtId) {
        this.id = id;
        this.userType = userType;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.aadharNumber = aadharNumber;
        this.state = state;
        this.city = city;
        this.password = password;
        this.department = department;
        this.designation = designation;
        this.employeeId = employeeId;
        this.primaryCategory = primaryCategory;
        this.categories = categories;
        this.experience = experience;
        this.specialization = specialization;
        this.hourlyRate = hourlyRate;
        this.companyName = companyName;
        this.gstNumber = gstNumber;
        this.officeAddress = officeAddress;
        this.district = district;
        this.division = division;
        this.govtId = govtId;
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

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
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

    public String getAadharNumber() {
        return aadharNumber;
    }

    public void setAadharNumber(String aadharNumber) {
        this.aadharNumber = aadharNumber;
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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getPrimaryCategory() {
        return primaryCategory;
    }

    public void setPrimaryCategory(String primaryCategory) {
        this.primaryCategory = primaryCategory;
    }

    public ArrayList<String> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<String> categories) {
        this.categories = categories;
    }

    public String getFounderId() {
        return founderId;
    }

    public void setFounderId(String founderId) {
        this.founderId = founderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExperience() {
        return experience;
    }

    public void setExperience(String experience) {
        this.experience = experience;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(String hourlyRate) {
        this.hourlyRate = hourlyRate;
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

    public String getGovtId() {
        return govtId;
    }

    public void setGovtId(String govtId) {
        this.govtId = govtId;
    }

    @Override
    public String toString() {
        return fullName + " , " + id;
    }
}

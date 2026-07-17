package com.rituraj.sevamitra.models;

public class IssueModel {
    private String id;
    private String problemTitle;
    private String problemDescription;
    private String problemType;
    private String issue;
    private String userType;
    private String priority;
    private String location;
    private long createdTimestamp, rejectTimestamp, workAssignTimestamp, workCompleteTimestamp, sevaMitraApprovedTimestamp, officerApprovedTimestamp;

    private String status; // Pending, In Progress, Resolved, Rejected
    private String assignedTo; // Worker ID
    private String founderId; // Founder ID
    private String createdBy; // SevaMitra ID
    private String approvedBy; // Officer ID
    private String resolutionNotes;

    // Constructors
    public IssueModel() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProblemTitle() {
        return problemTitle;
    }

    public void setProblemTitle(String problemTitle) {
        this.problemTitle = problemTitle;
    }

    public String getProblemDescription() {
        return problemDescription;
    }

    public void setProblemDescription(String problemDescription) {
        this.problemDescription = problemDescription;
    }

    public String getProblemType() {
        return problemType;
    }

    public void setProblemType(String problemType) {
        this.problemType = problemType;
    }

    public String getIssue() {
        return issue;
    }

    public void setIssue(String issue) {
        this.issue = issue;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public long getWorkAssignTimestamp() {
        return workAssignTimestamp;
    }

    public void setWorkAssignTimestamp(long workAssignTimestamp) {
        this.workAssignTimestamp = workAssignTimestamp;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public long getWorkCompleteTimestamp() {
        return workCompleteTimestamp;
    }

    public void setWorkCompleteTimestamp(long workCompleteTimestamp) {
        this.workCompleteTimestamp = workCompleteTimestamp;
    }

    public long getSevaMitraApprovedTimestamp() {
        return sevaMitraApprovedTimestamp;
    }

    public void setSevaMitraApprovedTimestamp(long sevaMitraApprovedTimestamp) {
        this.sevaMitraApprovedTimestamp = sevaMitraApprovedTimestamp;
    }

    public long getOfficerApprovedTimestamp() {
        return officerApprovedTimestamp;
    }

    public void setOfficerApprovedTimestamp(long officerApprovedTimestamp) {
        this.officerApprovedTimestamp = officerApprovedTimestamp;
    }

    public long getRejectTimestamp() {
        return rejectTimestamp;
    }

    public void setRejectTimestamp(long rejectTimestamp) {
        this.rejectTimestamp = rejectTimestamp;
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

    public String getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }
}
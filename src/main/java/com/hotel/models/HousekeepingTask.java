package com.hotel.models;

import java.io.Serializable;

public class HousekeepingTask implements Serializable {
    private static final long serialVersionUID = 1L;

    private int taskId;
    private int roomNumber;
    private String description;
    private String status; // "Pending", "In Progress", "Completed"
    private String assignedTo; // username of assigned staff, can be null

    public HousekeepingTask(int taskId, int roomNumber, String description, String status, String assignedTo) {
        this.taskId = taskId;
        this.roomNumber = roomNumber;
        this.description = description;
        this.status = status;
        this.assignedTo = assignedTo;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    @Override
    public String toString() {
        return "HousekeepingTask{taskId=" + taskId + ", room=" + roomNumber +
                ", status='" + status + "', assignedTo='" + assignedTo + "'}";
    }
}

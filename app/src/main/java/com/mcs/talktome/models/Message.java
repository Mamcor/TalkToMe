package com.mcs.talktome.models;

import java.util.Date;

public class Message {

    private String senderId, receiverId, message, messageDateTime;
    private Date dateObject;
    private String recentSenderId, recentSenderName, recentSenderImg;

    public String getRecentSenderId() {
        return recentSenderId;
    }

    public void setRecentSenderId(String recentSenderId) {
        this.recentSenderId = recentSenderId;
    }

    public String getRecentSenderName() {
        return recentSenderName;
    }

    public void setRecentSenderName(String recentSenderName) {
        this.recentSenderName = recentSenderName;
    }

    public String getRecentSenderImg() {
        return recentSenderImg;
    }

    public void setRecentSenderImg(String recentSenderImg) {
        this.recentSenderImg = recentSenderImg;
    }

    public Date getDateObject() {
        return dateObject;
    }

    public void setDateObject(Date dateObject) {
        this.dateObject = dateObject;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessageDateTime() {
        return messageDateTime;
    }

    public void setMessageDateTime(String messageDateTime) {
        this.messageDateTime = messageDateTime;
    }
}

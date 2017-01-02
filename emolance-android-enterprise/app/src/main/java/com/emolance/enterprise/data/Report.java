package com.emolance.enterprise.data;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;


public class Report implements Serializable {

    private Long id;
    private String type;
    private double value1;
    private double value2;
    private long timestamp;
    private String qrcode;
    private String status;
    private String name;
    private String link;
    private String age;
    private String position;
    private String email;
    private String result;
    private int profilePhotoIndex;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public String toString() {
        return "Report{" +
                "age='" + age + '\'' +
                ", id=" + id +
                ", type='" + type + '\'' +
                ", value1=" + value1 +
                ", value2=" + value2 +
                ", timestamp=" + timestamp +
                ", qrcode='" + qrcode + '\'' +
                ", status='" + status + '\'' +
                ", name='" + name + '\'' +
                ", link='" + link + '\'' +
                ", position='" + position + '\'' +
                ", email='" + email + '\'' +
                ", result='" + result + '\'' +
                ", profilePhotoIndex=" + profilePhotoIndex +
                '}';
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setValue1(double value1) {
        this.value1 = value1;
    }

    public double getValue1() {
        return value1;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @JsonIgnore
    public int getLevel() {
        if (value1 >= 0.95) {
            return 10;
        }
        int level1 = (int)(value1 * 10);

        if (value2 >= 0.95) {
            return 10;
        }
        int level2 = (int)(value1 * 10);

        int levelOverall = (level1 + level2) / 2;
        return levelOverall;
    }

    @JsonIgnore
    public int getPercent() {
        double overall = (value1 + value2) / 2;
        if (overall >= 0.95) {
            return 95;
        }
        return (int)(overall * 100) + 5;
    }

    public int getProfilePhotoIndex() {
        return profilePhotoIndex;
    }

    public void setProfilePhotoIndex(int profilePhotoIndex) {
        this.profilePhotoIndex = profilePhotoIndex;
    }

    public double getValue2() {
        return value2;
    }

    public void setValue2(double value2) {
        this.value2 = value2;
    }
}

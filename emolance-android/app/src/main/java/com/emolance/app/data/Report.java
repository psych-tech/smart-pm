package com.emolance.app.data;

import java.io.Serializable;


public class Report implements Serializable {

    private Long id;
    private String type;
    private double value;
    private String timestamp;
    private String qrcode;
    private String status;
    private String name;
    private String link;
    private String age;
    private String position;
    private String email;
    private String result;

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
                "id=" + id +
                ", type='" + type + "'" +
                ", value='" + value + "'" +
                ", timestamp='" + timestamp + "'" +
                ", qrcode='" + qrcode + "'" +
                ", status='" + status + "'" +
                ", name='" + name + "'" +
                ", link='" + link + "'" +
                '}';
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
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

    public int getLevel() {
        if (value >= 0.95) {
            return 10;
        }
        return (int)(value * 10);
    }

    public int getPercent() {
        if (value >= 0.95) {
            return 95;
        }
        return (int)(value * 100) + 5;
    }
}

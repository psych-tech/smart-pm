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
}

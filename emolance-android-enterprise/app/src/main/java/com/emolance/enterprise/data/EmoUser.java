package com.emolance.enterprise.data;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * A EmoUser.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmoUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private String email;

    private EmoUserType type;

    private Organization organization;

    private String datebirth;

    private String position;

    private String lastTestDate;

    private String profileImage;

    private String gender;

    public String getDatebirth() {
        return datebirth;
    }

    public void setDatebirth(String datebirth) {
        this.datebirth = datebirth;
    }

    public String getLastTestDate() {
        return lastTestDate;
    }

    public void setLastTestDate(String lastTestDate) {
        this.lastTestDate = lastTestDate;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public EmoUser name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public EmoUser email(String email) {
        this.email = email;
        return this;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public EmoUserType getType() {
        return type;
    }

    public EmoUser type(EmoUserType type) {
        this.type = type;
        return this;
    }

    public void setType(EmoUserType type) {
        this.type = type;
    }

    public Organization getOrganization() {
        return organization;
    }

    public EmoUser organization(Organization organization) {
        this.organization = organization;
        return this;
    }

    public void setOrganization(Organization organization) {
        this.organization = organization;
    }

    @Override
    public String toString() {
        return "EmoUser{" +
            "id=" + id +
            ", name='" + name + "'" +
            ", email='" + email + "'" +
            ", type='" + type + "'" +
            '}';
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}

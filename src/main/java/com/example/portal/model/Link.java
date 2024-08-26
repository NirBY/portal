package com.example.portal.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Base64;

@Entity
@Table(name = "links")
public class Link implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String url;
    private String passwordLink;
    private String description;

    @Lob
    private byte[] imageData;

    @ManyToOne
    @JoinColumn(name = "datacenter_id", nullable = false)
    private DataCenter dataCenter;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPasswordLink() {
        return passwordLink;
    }

    public void setPasswordLink(String passwordLink) {
        this.passwordLink = passwordLink;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DataCenter getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(DataCenter dataCenter) {
        this.dataCenter = dataCenter;
    }

    // Other getters and setters

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public String getImageAsBase64() {
        if (imageData == null) {
            return "";
        } else {
            return Base64.getEncoder().encodeToString(imageData);
        }
    }
}

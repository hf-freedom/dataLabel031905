package com.datalabel.entity;

import java.io.Serializable;

public class Application implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String appName;
    private Long organizationId;
    private Integer appType;
    private String description;
    private Integer status;
    private Integer deleted;
    
    public Application() {}
    
    public Application(Long id, String appName, Long organizationId, Integer appType) {
        this.id = id;
        this.appName = appName;
        this.organizationId = organizationId;
        this.appType = appType;
        this.status = 1;
        this.deleted = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }
    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    public Integer getAppType() { return appType; }
    public void setAppType(Integer appType) { this.appType = appType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getDeleted() { return deleted; }
    public void setDeleted(Integer deleted) { this.deleted = deleted; }
}

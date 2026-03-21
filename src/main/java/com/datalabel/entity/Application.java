package com.datalabel.entity;

import java.io.Serializable;

public class Application implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String name;
    private Long organizationId;
    private Integer type;
    private String description;
    private Integer status;
    private Boolean deleted;
    
    public Application() {}
    
    public Application(Long id, String name, Long organizationId, Integer type, String description) {
        this.id = id;
        this.name = name;
        this.organizationId = organizationId;
        this.type = type;
        this.description = description;
        this.status = 1;
        this.deleted = false;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
}

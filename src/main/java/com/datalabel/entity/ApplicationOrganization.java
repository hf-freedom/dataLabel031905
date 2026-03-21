package com.datalabel.entity;

import java.io.Serializable;

public class ApplicationOrganization implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long applicationId;
    private Long organizationId;
    
    public ApplicationOrganization() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }
    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
}

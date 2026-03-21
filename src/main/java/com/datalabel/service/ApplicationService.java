package com.datalabel.service;

import com.datalabel.entity.Application;
import com.datalabel.entity.User;
import com.datalabel.mapper.ApplicationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ApplicationService {
    
    @Autowired
    private ApplicationMapper applicationMapper;
    
    @Autowired
    private DataPermissionService dataPermissionService;
    
    public Application findById(Long id, User currentUser) {
        Application app = applicationMapper.findById(id);
        if (app == null) {
            return null;
        }
        if (!dataPermissionService.hasDataPermission(currentUser, app.getOrganizationId())) {
            return null;
        }
        return app;
    }
    
    public Application findByAppId(String appId, User currentUser) {
        Application app = applicationMapper.findByAppId(appId);
        if (app == null) {
            return null;
        }
        if (!dataPermissionService.hasDataPermission(currentUser, app.getOrganizationId())) {
            return null;
        }
        return app;
    }
    
    public List<Application> findAll(User currentUser) {
        if (dataPermissionService.isAdmin(currentUser)) {
            return applicationMapper.findAll();
        }
        List<Long> accessibleOrgIds = dataPermissionService.getAccessibleOrganizationIds(currentUser);
        return applicationMapper.findByOrganizationIds(accessibleOrgIds);
    }
    
    public List<Application> findByOrganizationId(Long orgId, User currentUser) {
        if (!dataPermissionService.hasDataPermission(currentUser, orgId)) {
            return java.util.Collections.emptyList();
        }
        return applicationMapper.findByOrganizationId(orgId);
    }
    
    public List<Application> findByAppType(Integer appType, User currentUser) {
        List<Application> apps = applicationMapper.findByAppType(appType);
        return apps.stream()
                .filter(app -> dataPermissionService.hasDataPermission(currentUser, app.getOrganizationId()))
                .collect(java.util.stream.Collectors.toList());
    }
    
    public boolean save(Application app, User currentUser) {
        if (!dataPermissionService.hasDataPermission(currentUser, app.getOrganizationId())) {
            return false;
        }
        
        if (app.getId() == null) {
            if (applicationMapper.findByAppId(app.getAppId()) != null) {
                return false;
            }
            return applicationMapper.insert(app) > 0;
        } else {
            Application existing = applicationMapper.findById(app.getId());
            if (existing == null) {
                return false;
            }
            if (!dataPermissionService.hasDataPermission(currentUser, existing.getOrganizationId())) {
                return false;
            }
            return applicationMapper.update(app) > 0;
        }
    }
    
    public boolean update(Application app, User currentUser) {
        Application existing = applicationMapper.findById(app.getId());
        if (existing == null) {
            return false;
        }
        if (!dataPermissionService.hasDataPermission(currentUser, existing.getOrganizationId())) {
            return false;
        }
        if (app.getOrganizationId() != null && 
            !app.getOrganizationId().equals(existing.getOrganizationId()) &&
            !dataPermissionService.hasDataPermission(currentUser, app.getOrganizationId())) {
            return false;
        }
        return applicationMapper.update(app) > 0;
    }
    
    public boolean deleteById(Long id, User currentUser) {
        Application existing = applicationMapper.findById(id);
        if (existing == null) {
            return false;
        }
        if (!dataPermissionService.hasDataPermission(currentUser, existing.getOrganizationId())) {
            return false;
        }
        return applicationMapper.deleteById(id) > 0;
    }
    
    public boolean updateStatus(Long id, Integer status, User currentUser) {
        Application existing = applicationMapper.findById(id);
        if (existing == null) {
            return false;
        }
        if (!dataPermissionService.hasDataPermission(currentUser, existing.getOrganizationId())) {
            return false;
        }
        existing.setStatus(status);
        return applicationMapper.update(existing) > 0;
    }
}

package com.datalabel.service;

import com.datalabel.entity.Application;
import com.datalabel.entity.User;
import com.datalabel.mapper.ApplicationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ApplicationService {
    
    @Autowired
    private ApplicationMapper applicationMapper;
    
    @Autowired
    private DataPermissionService dataPermissionService;
    
    public Application findById(Long id) {
        return applicationMapper.findById(id);
    }
    
    public List<Application> findAll() {
        return applicationMapper.findAll();
    }
    
    public List<Application> findByOrganizationId(Long orgId) {
        return applicationMapper.findByOrganizationId(orgId);
    }
    
    public List<Application> findByOrganizationIds(List<Long> orgIds) {
        if (orgIds == null || orgIds.isEmpty()) {
            return applicationMapper.findAll();
        }
        return applicationMapper.findByOrganizationIds(orgIds);
    }
    
    public List<Application> findAccessibleApplications(User currentUser) {
        if (dataPermissionService.isAdmin(currentUser)) {
            return applicationMapper.findAll();
        }
        
        List<Long> accessibleOrgIds = dataPermissionService.getAccessibleOrgIds(currentUser);
        if (accessibleOrgIds == null) {
            return applicationMapper.findAll();
        }
        if (accessibleOrgIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return applicationMapper.findByOrganizationIds(accessibleOrgIds);
    }
    
    public List<Application> findByStatus(Integer status) {
        return applicationMapper.findByStatus(status);
    }
    
    public List<Application> findByAppType(Integer appType) {
        return applicationMapper.findByAppType(appType);
    }
    
    public boolean save(Application app, User currentUser) {
        if (app.getId() == null) {
            if (!dataPermissionService.hasAccessToOrg(currentUser, app.getOrganizationId())) {
                return false;
            }
            return applicationMapper.insert(app) > 0;
        } else {
            Application existingApp = applicationMapper.findByIdIgnoreDelete(app.getId());
            if (existingApp == null) {
                return false;
            }
            if (!dataPermissionService.hasAccessToApplication(currentUser, existingApp.getOrganizationId())) {
                return false;
            }
            return applicationMapper.update(app) > 0;
        }
    }
    
    public boolean update(Application app, User currentUser) {
        Application existingApp = applicationMapper.findById(app.getId());
        if (existingApp == null) {
            return false;
        }
        
        if (!dataPermissionService.hasAccessToApplication(currentUser, existingApp.getOrganizationId())) {
            return false;
        }
        
        return applicationMapper.update(app) > 0;
    }
    
    public boolean deleteById(Long id, User currentUser) {
        Application app = applicationMapper.findById(id);
        if (app == null) {
            return false;
        }
        
        if (!dataPermissionService.canDeleteApplication(currentUser, app.getOrganizationId())) {
            return false;
        }
        
        return applicationMapper.deleteById(id) > 0;
    }
    
    public boolean updateStatus(Long id, Integer status, User currentUser) {
        Application app = applicationMapper.findById(id);
        if (app == null) {
            return false;
        }
        
        if (!dataPermissionService.hasAccessToApplication(currentUser, app.getOrganizationId())) {
            return false;
        }
        
        app.setStatus(status);
        return applicationMapper.update(app) > 0;
    }
}

package com.datalabel.service;

import com.datalabel.entity.Application;
import com.datalabel.entity.ApplicationOrganization;
import com.datalabel.mapper.ApplicationMapper;
import com.datalabel.mapper.ApplicationOrganizationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationService {
    
    @Autowired
    private ApplicationMapper applicationMapper;
    
    @Autowired
    private ApplicationOrganizationMapper applicationOrganizationMapper;
    
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
            return null;
        }
        return applicationMapper.findByOrganizationIds(orgIds);
    }
    
    public boolean save(Application application) {
        if (application.getId() == null) {
            return applicationMapper.insert(application) > 0;
        } else {
            return applicationMapper.update(application) > 0;
        }
    }
    
    public boolean update(Application application) {
        return applicationMapper.update(application) > 0;
    }
    
    public boolean deleteById(Long id) {
        return applicationMapper.deleteById(id) > 0;
    }
    
    public boolean updateStatus(Long id, Integer status) {
        Application app = applicationMapper.findById(id);
        if (app != null) {
            app.setStatus(status);
            return applicationMapper.update(app) > 0;
        }
        return false;
    }
    
    @Transactional
    public boolean bindOrganization(Long applicationId, List<Long> orgIds) {
        applicationOrganizationMapper.deleteByApplicationId(applicationId);
        if (orgIds != null) {
            for (Long orgId : orgIds) {
                ApplicationOrganization ao = new ApplicationOrganization();
                ao.setApplicationId(applicationId);
                ao.setOrganizationId(orgId);
                applicationOrganizationMapper.insert(ao);
            }
        }
        return true;
    }
    
    public List<Long> getBoundOrganizationIds(Long applicationId) {
        return applicationOrganizationMapper.findOrganizationIdsByApplicationId(applicationId);
    }
}

package com.datalabel.mapper;

import com.datalabel.cache.LocalCache;
import com.datalabel.entity.ApplicationOrganization;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ApplicationOrganizationMapper {
    
    private final LocalCache cache = LocalCache.getInstance();
    
    public ApplicationOrganization findById(Long id) {
        return (ApplicationOrganization) cache.get(id);
    }
    
    public List<ApplicationOrganization> findByApplicationId(Long applicationId) {
        return cache.getAll(ApplicationOrganization.class).stream()
                .filter(ao -> applicationId.equals(ao.getApplicationId()))
                .collect(Collectors.toList());
    }
    
    public List<ApplicationOrganization> findByOrganizationId(Long organizationId) {
        return cache.getAll(ApplicationOrganization.class).stream()
                .filter(ao -> organizationId.equals(ao.getOrganizationId()))
                .collect(Collectors.toList());
    }
    
    public List<Long> findApplicationIdsByOrganizationId(Long organizationId) {
        return cache.getAll(ApplicationOrganization.class).stream()
                .filter(ao -> organizationId.equals(ao.getOrganizationId()))
                .map(ApplicationOrganization::getApplicationId)
                .collect(Collectors.toList());
    }
    
    public List<Long> findOrganizationIdsByApplicationId(Long applicationId) {
        return cache.getAll(ApplicationOrganization.class).stream()
                .filter(ao -> applicationId.equals(ao.getApplicationId()))
                .map(ApplicationOrganization::getOrganizationId)
                .collect(Collectors.toList());
    }
    
    public ApplicationOrganization findByAppIdAndOrgId(Long applicationId, Long organizationId) {
        return cache.getAll(ApplicationOrganization.class).stream()
                .filter(ao -> applicationId.equals(ao.getApplicationId()) 
                        && organizationId.equals(ao.getOrganizationId()))
                .findFirst()
                .orElse(null);
    }
    
    public int insert(ApplicationOrganization applicationOrganization) {
        if (applicationOrganization.getId() == null) {
            applicationOrganization.setId(cache.generateId());
        }
        cache.put(applicationOrganization.getId(), applicationOrganization);
        return 1;
    }
    
    public int deleteById(Long id) {
        cache.remove(id);
        return 1;
    }
    
    public int deleteByApplicationId(Long applicationId) {
        List<ApplicationOrganization> list = findByApplicationId(applicationId);
        for (ApplicationOrganization ao : list) {
            cache.remove(ao.getId());
        }
        return list.size();
    }
    
    public int deleteByOrganizationId(Long organizationId) {
        List<ApplicationOrganization> list = findByOrganizationId(organizationId);
        for (ApplicationOrganization ao : list) {
            cache.remove(ao.getId());
        }
        return list.size();
    }
}

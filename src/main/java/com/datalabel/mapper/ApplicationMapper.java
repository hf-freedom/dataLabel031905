package com.datalabel.mapper;

import com.datalabel.cache.LocalCache;
import com.datalabel.entity.Application;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ApplicationMapper {
    
    private final LocalCache cache = LocalCache.getInstance();
    
    public Application findById(Long id) {
        Application app = (Application) cache.get(id);
        if (app != null && app.getDeleted() != null && app.getDeleted() == 1) {
            return null;
        }
        return app;
    }
    
    public Application findByAppId(String appId) {
        return cache.getAll(Application.class).stream()
                .filter(a -> appId.equals(a.getAppId()) && (a.getDeleted() == null || a.getDeleted() == 0))
                .findFirst()
                .orElse(null);
    }
    
    public List<Application> findAll() {
        return cache.getAll(Application.class).stream()
                .filter(a -> a.getDeleted() == null || a.getDeleted() == 0)
                .collect(Collectors.toList());
    }
    
    public List<Application> findByOrganizationId(Long orgId) {
        return cache.getAll(Application.class).stream()
                .filter(a -> orgId.equals(a.getOrganizationId()) && (a.getDeleted() == null || a.getDeleted() == 0))
                .collect(Collectors.toList());
    }
    
    public List<Application> findByOrganizationIds(List<Long> orgIds) {
        if (orgIds == null || orgIds.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return cache.getAll(Application.class).stream()
                .filter(a -> orgIds.contains(a.getOrganizationId()) && (a.getDeleted() == null || a.getDeleted() == 0))
                .collect(Collectors.toList());
    }
    
    public List<Application> findByAppType(Integer appType) {
        return cache.getAll(Application.class).stream()
                .filter(a -> appType.equals(a.getAppType()) && (a.getDeleted() == null || a.getDeleted() == 0))
                .collect(Collectors.toList());
    }
    
    public List<Application> findByStatus(Integer status) {
        return cache.getAll(Application.class).stream()
                .filter(a -> status.equals(a.getStatus()) && (a.getDeleted() == null || a.getDeleted() == 0))
                .collect(Collectors.toList());
    }
    
    public int insert(Application app) {
        if (app.getId() == null) {
            app.setId(cache.generateId());
        }
        if (app.getDeleted() == null) {
            app.setDeleted(0);
        }
        if (app.getStatus() == null) {
            app.setStatus(1);
        }
        cache.put(app.getId(), app);
        return 1;
    }
    
    public int update(Application app) {
        Application existing = (Application) cache.get(app.getId());
        if (existing != null && (existing.getDeleted() == null || existing.getDeleted() == 0)) {
            cache.put(app.getId(), app);
            return 1;
        }
        return 0;
    }
    
    public int deleteById(Long id) {
        Application app = (Application) cache.get(id);
        if (app != null) {
            app.setDeleted(1);
            cache.put(id, app);
            return 1;
        }
        return 0;
    }
    
    public int deletePhysically(Long id) {
        cache.remove(id);
        return 1;
    }
}

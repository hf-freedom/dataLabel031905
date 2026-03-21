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
        if (app != null && app.getDeleted() == 0) {
            return app;
        }
        return null;
    }
    
    public Application findByIdIgnoreDelete(Long id) {
        return (Application) cache.get(id);
    }
    
    public List<Application> findAll() {
        return cache.getAll(Application.class).stream()
                .filter(app -> app.getDeleted() == 0)
                .collect(Collectors.toList());
    }
    
    public List<Application> findByOrganizationId(Long orgId) {
        return cache.getAll(Application.class).stream()
                .filter(app -> orgId.equals(app.getOrganizationId()) && app.getDeleted() == 0)
                .collect(Collectors.toList());
    }
    
    public List<Application> findByOrganizationIds(List<Long> orgIds) {
        return cache.getAll(Application.class).stream()
                .filter(app -> orgIds.contains(app.getOrganizationId()) && app.getDeleted() == 0)
                .collect(Collectors.toList());
    }
    
    public List<Application> findByStatus(Integer status) {
        return cache.getAll(Application.class).stream()
                .filter(app -> status.equals(app.getStatus()) && app.getDeleted() == 0)
                .collect(Collectors.toList());
    }
    
    public List<Application> findByAppType(Integer appType) {
        return cache.getAll(Application.class).stream()
                .filter(app -> appType.equals(app.getAppType()) && app.getDeleted() == 0)
                .collect(Collectors.toList());
    }
    
    public int insert(Application app) {
        if (app.getId() == null) {
            app.setId(cache.generateId());
        }
        cache.put(app.getId(), app);
        return 1;
    }
    
    public int update(Application app) {
        if (cache.containsKey(app.getId())) {
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
}

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
        Object obj = cache.get(id);
        if (obj instanceof Application) {
            Application app = (Application) obj;
            if (app.getDeleted() == null || !app.getDeleted()) {
                return app;
            }
        }
        return null;
    }
    
    public List<Application> findAll() {
        return cache.getAll(Application.class).stream()
                .filter(app -> app.getDeleted() == null || !app.getDeleted())
                .collect(Collectors.toList());
    }
    
    public List<Application> findByOrganizationId(Long orgId) {
        return cache.getAll(Application.class).stream()
                .filter(app -> (app.getDeleted() == null || !app.getDeleted()) 
                        && orgId.equals(app.getOrganizationId()))
                .collect(Collectors.toList());
    }
    
    public List<Application> findByOrganizationIds(List<Long> orgIds) {
        return cache.getAll(Application.class).stream()
                .filter(app -> (app.getDeleted() == null || !app.getDeleted()) 
                        && orgIds.contains(app.getOrganizationId()))
                .collect(Collectors.toList());
    }
    
    public int insert(Application application) {
        if (application.getId() == null) {
            application.setId(cache.generateId());
        }
        if (application.getDeleted() == null) {
            application.setDeleted(false);
        }
        if (application.getStatus() == null) {
            application.setStatus(1);
        }
        cache.put(application.getId(), application);
        return 1;
    }
    
    public int update(Application application) {
        if (cache.containsKey(application.getId())) {
            cache.put(application.getId(), application);
            return 1;
        }
        return 0;
    }
    
    public int deleteById(Long id) {
        Application app = findById(id);
        if (app != null) {
            app.setDeleted(true);
            cache.put(id, app);
            return 1;
        }
        return 0;
    }
}

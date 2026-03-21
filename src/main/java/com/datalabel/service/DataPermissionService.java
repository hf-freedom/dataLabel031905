package com.datalabel.service;

import com.datalabel.entity.RoleOrganization;
import com.datalabel.entity.User;
import com.datalabel.mapper.RoleOrganizationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class DataPermissionService {
    
    @Autowired
    private RoleOrganizationMapper roleOrganizationMapper;
    
    @Autowired
    private OrganizationService organizationService;
    
    public boolean isAdmin(User user) {
        return user != null && user.getUserType() != null && user.getUserType() == 1;
    }
    
    public boolean hasDataPermission(User user, Long targetOrganizationId) {
        if (user == null || targetOrganizationId == null) {
            return false;
        }
        
        if (isAdmin(user)) {
            return true;
        }
        
        List<Long> accessibleOrgIds = getAccessibleOrganizationIds(user);
        return accessibleOrgIds.contains(targetOrganizationId);
    }
    
    public List<Long> getAccessibleOrganizationIds(User user) {
        List<Long> result = new ArrayList<>();
        
        if (user == null) {
            return result;
        }
        
        if (isAdmin(user)) {
            return getAllOrganizationIds();
        }
        
        Long roleId = user.getRoleId();
        if (roleId == null) {
            if (user.getOrganizationId() != null) {
                result.add(user.getOrganizationId());
            }
            return result;
        }
        
        List<Long> roleOrgIds = roleOrganizationMapper.findOrgIdsByRoleId(roleId);
        
        Set<Long> allOrgIds = new HashSet<>();
        for (Long orgId : roleOrgIds) {
            allOrgIds.add(orgId);
            allOrgIds.addAll(getChildOrganizationIds(orgId));
        }
        
        result.addAll(allOrgIds);
        return result;
    }
    
    private List<Long> getAllOrganizationIds() {
        return organizationService.findAll().stream()
                .map(org -> org.getId())
                .collect(java.util.stream.Collectors.toList());
    }
    
    private List<Long> getChildOrganizationIds(Long parentId) {
        List<Long> result = new ArrayList<>();
        List<com.datalabel.entity.Organization> children = organizationService.findByParentId(parentId);
        for (com.datalabel.entity.Organization child : children) {
            result.add(child.getId());
            result.addAll(getChildOrganizationIds(child.getId()));
        }
        return result;
    }
}

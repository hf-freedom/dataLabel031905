package com.datalabel.service;

import com.datalabel.entity.User;
import com.datalabel.mapper.RoleOrganizationMapper;
import com.datalabel.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DataPermissionService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RoleOrganizationMapper roleOrganizationMapper;
    
    public boolean isAdmin(User user) {
        return user != null && user.getUserType() != null && user.getUserType() == 1;
    }
    
    public List<Long> getAccessibleOrgIds(User user) {
        if (isAdmin(user)) {
            return null;
        }
        
        if (user == null || user.getRoleId() == null) {
            return new ArrayList<>();
        }
        
        return roleOrganizationMapper.findOrgIdsByRoleId(user.getRoleId());
    }
    
    public boolean hasAccessToOrg(User user, Long orgId) {
        if (isAdmin(user)) {
            return true;
        }
        
        List<Long> accessibleOrgIds = getAccessibleOrgIds(user);
        return accessibleOrgIds != null && accessibleOrgIds.contains(orgId);
    }
    
    public boolean hasAccessToUser(User currentUser, Long targetUserId) {
        if (isAdmin(currentUser)) {
            return true;
        }
        
        User targetUser = userMapper.findById(targetUserId);
        if (targetUser == null) {
            return false;
        }
        
        return hasAccessToOrg(currentUser, targetUser.getOrganizationId());
    }
    
    public boolean hasAccessToApplication(User user, Long appOrgId) {
        if (isAdmin(user)) {
            return true;
        }
        
        return hasAccessToOrg(user, appOrgId);
    }
    
    public boolean canModifyUser(User currentUser, Long targetUserId) {
        return hasAccessToUser(currentUser, targetUserId);
    }
    
    public boolean canDeleteUser(User currentUser, Long targetUserId) {
        if (isAdmin(currentUser)) {
            return true;
        }
        User targetUser = userMapper.findById(targetUserId);
        if (targetUser != null && targetUser.getUserType() == 1) {
            return false;
        }
        return hasAccessToUser(currentUser, targetUserId);
    }
    
    public boolean canModifyApplication(User user, Long appOrgId) {
        return hasAccessToApplication(user, appOrgId);
    }
    
    public boolean canDeleteApplication(User user, Long appOrgId) {
        return hasAccessToApplication(user, appOrgId);
    }
}

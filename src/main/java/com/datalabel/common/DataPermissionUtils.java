package com.datalabel.common;

import com.datalabel.entity.User;
import com.datalabel.service.RoleOrganizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;

@Component
public class DataPermissionUtils {
    
    @Autowired
    private RoleOrganizationService roleOrganizationService;
    
    public boolean isAdmin(User user) {
        return user != null && user.getUserType() != null && user.getUserType() == 1;
    }
    
    public List<Long> getAccessibleOrgIds(User user) {
        if (user == null) {
            return Collections.emptyList();
        }
        
        if (isAdmin(user)) {
            return null;
        }
        
        Long roleId = user.getRoleId();
        if (roleId == null) {
            return Collections.emptyList();
        }
        
        return roleOrganizationService.findOrgIdsByRoleId(roleId);
    }
    
    public boolean hasOrgPermission(User user, Long orgId) {
        if (orgId == null) {
            return false;
        }
        
        if (isAdmin(user)) {
            return true;
        }
        
        List<Long> orgIds = getAccessibleOrgIds(user);
        return orgIds.contains(orgId);
    }
    
    public User getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute("currentUser");
    }
}

package com.datalabel.service;

import com.datalabel.entity.User;
import com.datalabel.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private DataPermissionService dataPermissionService;
    
    public User login(String username, String password) {
        User user = userMapper.findByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }
    
    public User findById(Long id, User currentUser) {
        User user = userMapper.findById(id);
        if (user == null) {
            return null;
        }
        if (!dataPermissionService.hasDataPermission(currentUser, user.getOrganizationId())) {
            return null;
        }
        return user;
    }
    
    public User findById(Long id) {
        return userMapper.findById(id);
    }
    
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
    
    public List<User> findAll(User currentUser) {
        if (dataPermissionService.isAdmin(currentUser)) {
            return userMapper.findAll();
        }
        List<Long> accessibleOrgIds = dataPermissionService.getAccessibleOrganizationIds(currentUser);
        return userMapper.findAll().stream()
                .filter(u -> accessibleOrgIds.contains(u.getOrganizationId()))
                .collect(Collectors.toList());
    }
    
    public List<User> findAll() {
        return userMapper.findAll();
    }
    
    public List<User> findByOrganizationId(Long orgId, User currentUser) {
        if (!dataPermissionService.hasDataPermission(currentUser, orgId)) {
            return java.util.Collections.emptyList();
        }
        return userMapper.findByOrganizationId(orgId);
    }
    
    public List<User> findByOrganizationId(Long orgId) {
        return userMapper.findByOrganizationId(orgId);
    }
    
    public List<User> findByRoleId(Long roleId) {
        return userMapper.findByRoleId(roleId);
    }
    
    public boolean save(User user, User currentUser) {
        if (!dataPermissionService.hasDataPermission(currentUser, user.getOrganizationId())) {
            return false;
        }
        
        if (user.getId() == null) {
            return userMapper.insert(user) > 0;
        } else {
            User existing = userMapper.findById(user.getId());
            if (existing == null) {
                return false;
            }
            if (!dataPermissionService.hasDataPermission(currentUser, existing.getOrganizationId())) {
                return false;
            }
            return userMapper.update(user) > 0;
        }
    }
    
    public boolean save(User user) {
        if (user.getId() == null) {
            return userMapper.insert(user) > 0;
        } else {
            return userMapper.update(user) > 0;
        }
    }
    
    public boolean update(User user, User currentUser) {
        User existing = userMapper.findById(user.getId());
        if (existing == null) {
            return false;
        }
        if (!dataPermissionService.hasDataPermission(currentUser, existing.getOrganizationId())) {
            return false;
        }
        if (user.getOrganizationId() != null && 
            !user.getOrganizationId().equals(existing.getOrganizationId()) &&
            !dataPermissionService.hasDataPermission(currentUser, user.getOrganizationId())) {
            return false;
        }
        return userMapper.update(user) > 0;
    }
    
    public boolean update(User user) {
        return userMapper.update(user) > 0;
    }
    
    public boolean deleteById(Long id, User currentUser) {
        User existing = userMapper.findById(id);
        if (existing == null) {
            return false;
        }
        if (!dataPermissionService.hasDataPermission(currentUser, existing.getOrganizationId())) {
            return false;
        }
        return userMapper.deleteById(id) > 0;
    }
    
    public boolean deleteById(Long id) {
        return userMapper.deleteById(id) > 0;
    }
    
    public boolean bindRole(Long userId, Long roleId, User currentUser) {
        User user = userMapper.findById(userId);
        if (user == null) {
            return false;
        }
        if (!dataPermissionService.hasDataPermission(currentUser, user.getOrganizationId())) {
            return false;
        }
        user.setRoleId(roleId);
        return userMapper.update(user) > 0;
    }
    
    public boolean bindRole(Long userId, Long roleId) {
        User user = userMapper.findById(userId);
        if (user != null) {
            user.setRoleId(roleId);
            return userMapper.update(user) > 0;
        }
        return false;
    }
}

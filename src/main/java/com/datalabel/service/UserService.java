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
    
    public User findById(Long id) {
        return userMapper.findById(id);
    }
    
    public User findByUsername(String username) {
        return userMapper.findByUsername(username);
    }
    
    public List<User> findAll() {
        return userMapper.findAll();
    }
    
    public List<User> findAccessibleUsers(User currentUser) {
        if (dataPermissionService.isAdmin(currentUser)) {
            return userMapper.findAll();
        }
        
        List<Long> accessibleOrgIds = dataPermissionService.getAccessibleOrgIds(currentUser);
        if (accessibleOrgIds == null) {
            return userMapper.findAll();
        }
        if (accessibleOrgIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        return userMapper.findAll().stream()
                .filter(user -> accessibleOrgIds.contains(user.getOrganizationId()))
                .collect(Collectors.toList());
    }
    
    public List<User> findByOrganizationId(Long orgId) {
        return userMapper.findByOrganizationId(orgId);
    }
    
    public List<User> findByRoleId(Long roleId) {
        return userMapper.findByRoleId(roleId);
    }
    
    public boolean save(User user, User currentUser) {
        if (!dataPermissionService.isAdmin(currentUser)) {
            if (user.getId() == null) {
                if (!dataPermissionService.hasAccessToOrg(currentUser, user.getOrganizationId())) {
                    return false;
                }
            } else {
                User existingUser = userMapper.findById(user.getId());
                if (existingUser == null || !dataPermissionService.hasAccessToUser(currentUser, existingUser.getId())) {
                    return false;
                }
            }
        }
        
        if (user.getId() == null) {
            return userMapper.insert(user) > 0;
        } else {
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
        if (!dataPermissionService.canModifyUser(currentUser, user.getId())) {
            return false;
        }
        return userMapper.update(user) > 0;
    }
    
    public boolean update(User user) {
        return userMapper.update(user) > 0;
    }
    
    public boolean deleteById(Long id, User currentUser) {
        if (!dataPermissionService.canDeleteUser(currentUser, id)) {
            return false;
        }
        return userMapper.deleteById(id) > 0;
    }
    
    public boolean deleteById(Long id) {
        return userMapper.deleteById(id) > 0;
    }
    
    public boolean bindRole(Long userId, Long roleId, User currentUser) {
        if (!dataPermissionService.hasAccessToUser(currentUser, userId)) {
            return false;
        }
        User user = userMapper.findById(userId);
        if (user != null) {
            user.setRoleId(roleId);
            return userMapper.update(user) > 0;
        }
        return false;
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

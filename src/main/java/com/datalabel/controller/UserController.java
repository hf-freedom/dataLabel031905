package com.datalabel.controller;

import com.datalabel.annotation.RequireApiPermission;
import com.datalabel.common.DataPermissionUtils;
import com.datalabel.common.Result;
import com.datalabel.entity.User;
import com.datalabel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DataPermissionUtils dataPermissionUtils;
    
    @GetMapping("/list")
    @RequireApiPermission("user:list")
    public Result<List<User>> list(HttpSession session) {
        User currentUser = dataPermissionUtils.getCurrentUser(session);
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        List<Long> accessibleOrgIds = dataPermissionUtils.getAccessibleOrgIds(currentUser);
        List<User> users;
        
        if (accessibleOrgIds == null) {
            users = userService.findAll();
        } else {
            users = userService.findAll().stream()
                    .filter(user -> user.getOrganizationId() != null 
                            && accessibleOrgIds.contains(user.getOrganizationId()))
                    .collect(Collectors.toList());
        }
        
        return Result.success(users);
    }
    
    @GetMapping("/{id}")
    @RequireApiPermission("user:view")
    public Result<User> getById(@PathVariable Long id, HttpSession session) {
        User currentUser = dataPermissionUtils.getCurrentUser(session);
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        User user = userService.findById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        if (user.getOrganizationId() != null 
                && !dataPermissionUtils.hasOrgPermission(currentUser, user.getOrganizationId())) {
            return Result.error(403, "无权限查看该用户");
        }
        
        return Result.success(user);
    }
    
    @PostMapping("/save")
    @RequireApiPermission("user:save")
    public Result<String> save(@RequestBody User user, HttpSession session) {
        User currentUser = dataPermissionUtils.getCurrentUser(session);
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        if (user.getOrganizationId() != null 
                && !dataPermissionUtils.hasOrgPermission(currentUser, user.getOrganizationId())) {
            return Result.error(403, "无权限在该组织机构下创建用户");
        }
        
        User existUser = userService.findByUsername(user.getUsername());
        if (existUser != null && !existUser.getId().equals(user.getId())) {
            return Result.error("用户名已存在");
        }
        if (userService.save(user)) {
            return Result.success("保存成功", null);
        }
        return Result.error("保存失败");
    }
    
    @PostMapping("/update")
    @RequireApiPermission("user:update")
    public Result<String> update(@RequestBody User user, HttpSession session) {
        User currentUser = dataPermissionUtils.getCurrentUser(session);
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        if (currentUser.getUserType() == 0) {
            User updateUser = userService.findById(currentUser.getId());
            updateUser.setRealName(user.getRealName());
            updateUser.setEmail(user.getEmail());
            updateUser.setPhone(user.getPhone());
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                updateUser.setPassword(user.getPassword());
            }
            if (userService.update(updateUser)) {
                session.setAttribute("currentUser", updateUser);
                return Result.success("修改成功", null);
            }
        } else {
            User existingUser = userService.findById(user.getId());
            if (existingUser != null && existingUser.getOrganizationId() != null 
                    && !dataPermissionUtils.hasOrgPermission(currentUser, existingUser.getOrganizationId())) {
                return Result.error(403, "无权限修改该用户");
            }
            
            if (user.getOrganizationId() != null 
                    && (existingUser == null || !user.getOrganizationId().equals(existingUser.getOrganizationId()))
                    && !dataPermissionUtils.hasOrgPermission(currentUser, user.getOrganizationId())) {
                return Result.error(403, "无权限将用户移动到该组织机构");
            }
            
            if (userService.update(user)) {
                return Result.success("修改成功", null);
            }
        }
        return Result.error("修改失败");
    }
    
    @DeleteMapping("/{id}")
    @RequireApiPermission("user:delete")
    public Result<String> delete(@PathVariable Long id, HttpSession session) {
        User currentUser = dataPermissionUtils.getCurrentUser(session);
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        User user = userService.findById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        if (user.getOrganizationId() != null 
                && !dataPermissionUtils.hasOrgPermission(currentUser, user.getOrganizationId())) {
            return Result.error(403, "无权限删除该用户");
        }
        
        if (userService.deleteById(id)) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
    
    @PostMapping("/bindRole")
    @RequireApiPermission("user:bindRole")
    public Result<String> bindRole(@RequestParam Long userId, @RequestParam Long roleId, HttpSession session) {
        User currentUser = dataPermissionUtils.getCurrentUser(session);
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        User user = userService.findById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        if (user.getOrganizationId() != null 
                && !dataPermissionUtils.hasOrgPermission(currentUser, user.getOrganizationId())) {
            return Result.error(403, "无权限为该用户绑定角色");
        }
        
        if (userService.bindRole(userId, roleId)) {
            return Result.success("绑定成功", null);
        }
        return Result.error("绑定失败");
    }
}

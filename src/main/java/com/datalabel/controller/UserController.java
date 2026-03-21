package com.datalabel.controller;

import com.datalabel.annotation.RequireApiPermission;
import com.datalabel.common.Result;
import com.datalabel.entity.User;
import com.datalabel.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/list")
    @RequireApiPermission("user:list")
    public Result<List<User>> list(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(userService.findAll(currentUser));
    }
    
    @GetMapping("/{id}")
    @RequireApiPermission("user:view")
    public Result<User> getById(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        User user = userService.findById(id, currentUser);
        if (user == null) {
            return Result.error("用户不存在或无权限访问");
        }
        return Result.success(user);
    }
    
    @PostMapping("/save")
    @RequireApiPermission("user:save")
    public Result<String> save(@RequestBody User user, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        User existUser = userService.findByUsername(user.getUsername());
        if (existUser != null && !existUser.getId().equals(user.getId())) {
            return Result.error("用户名已存在");
        }
        
        if (userService.save(user, currentUser)) {
            return Result.success("保存成功", null);
        }
        return Result.error("保存失败，可能原因：无权限操作该组织机构");
    }
    
    @PostMapping("/update")
    @RequireApiPermission("user:update")
    public Result<String> update(@RequestBody User user, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        if (currentUser.getUserType() != null && currentUser.getUserType() == 0) {
            User updateUser = userService.findById(currentUser.getId(), currentUser);
            if (updateUser == null) {
                return Result.error("用户不存在");
            }
            updateUser.setRealName(user.getRealName());
            updateUser.setEmail(user.getEmail());
            updateUser.setPhone(user.getPhone());
            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                updateUser.setPassword(user.getPassword());
            }
            if (userService.update(updateUser, currentUser)) {
                session.setAttribute("currentUser", updateUser);
                return Result.success("修改成功", null);
            }
        } else {
            if (userService.update(user, currentUser)) {
                return Result.success("修改成功", null);
            }
        }
        return Result.error("修改失败，可能原因：用户不存在或无权限操作");
    }
    
    @DeleteMapping("/{id}")
    @RequireApiPermission("user:delete")
    public Result<String> delete(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        if (userService.deleteById(id, currentUser)) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败，可能原因：用户不存在或无权限操作");
    }
    
    @PostMapping("/bindRole")
    @RequireApiPermission("user:bindRole")
    public Result<String> bindRole(@RequestParam Long userId, @RequestParam Long roleId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        if (userService.bindRole(userId, roleId, currentUser)) {
            return Result.success("绑定成功", null);
        }
        return Result.error("绑定失败，可能原因：用户不存在或无权限操作");
    }
}

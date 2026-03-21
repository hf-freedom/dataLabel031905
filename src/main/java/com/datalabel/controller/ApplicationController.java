package com.datalabel.controller;

import com.datalabel.annotation.RequireApiPermission;
import com.datalabel.common.Result;
import com.datalabel.entity.Application;
import com.datalabel.entity.User;
import com.datalabel.service.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/application")
public class ApplicationController {
    
    @Autowired
    private ApplicationService applicationService;
    
    @GetMapping("/list")
    @RequireApiPermission("app:list")
    public Result<List<Application>> list(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(applicationService.findAccessibleApplications(currentUser));
    }
    
    @GetMapping("/{id}")
    @RequireApiPermission("app:view")
    public Result<Application> getById(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        Application app = applicationService.findById(id);
        if (app == null) {
            return Result.error("应用不存在");
        }
        
        return Result.success(app);
    }
    
    @PostMapping("/save")
    @RequireApiPermission("app:save")
    public Result<String> save(@RequestBody Application app, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        if (app.getAppName() == null || app.getAppName().trim().isEmpty()) {
            return Result.error("应用名称不能为空");
        }
        
        if (app.getOrganizationId() == null) {
            return Result.error("组织机构不能为空");
        }
        
        if (app.getAppType() == null || (app.getAppType() != 0 && app.getAppType() != 1)) {
            return Result.error("应用类型无效");
        }
        
        if (app.getStatus() == null) {
            app.setStatus(1);
        }
        if (app.getDeleted() == null) {
            app.setDeleted(0);
        }
        
        if (applicationService.save(app, currentUser)) {
            return Result.success("保存成功", null);
        }
        return Result.error("保存失败，可能没有权限操作该组织机构的数据");
    }
    
    @PostMapping("/update")
    @RequireApiPermission("app:update")
    public Result<String> update(@RequestBody Application app, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        if (app.getId() == null) {
            return Result.error("应用ID不能为空");
        }
        
        if (applicationService.update(app, currentUser)) {
            return Result.success("更新成功", null);
        }
        return Result.error("更新失败，可能没有权限操作该应用");
    }
    
    @PostMapping("/status")
    @RequireApiPermission("app:update")
    public Result<String> updateStatus(@RequestParam Long id, @RequestParam Integer status, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        if (status == null || (status != 0 && status != 1)) {
            return Result.error("状态值无效");
        }
        
        if (applicationService.updateStatus(id, status, currentUser)) {
            return Result.success(status == 1 ? "启用成功" : "停用成功", null);
        }
        return Result.error("操作失败，可能没有权限操作该应用");
    }
    
    @DeleteMapping("/{id}")
    @RequireApiPermission("app:delete")
    public Result<String> delete(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        if (applicationService.deleteById(id, currentUser)) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败，可能没有权限操作该应用");
    }
    
    @GetMapping("/org/{orgId}")
    @RequireApiPermission("app:list")
    public Result<List<Application>> getByOrgId(@PathVariable Long orgId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        return Result.success(applicationService.findByOrganizationId(orgId));
    }
}

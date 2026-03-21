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
@RequestMapping("/api/app")
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
        return Result.success(applicationService.findAll(currentUser));
    }
    
    @GetMapping("/{id}")
    @RequireApiPermission("app:view")
    public Result<Application> getById(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        Application app = applicationService.findById(id, currentUser);
        if (app == null) {
            return Result.error("应用不存在或无权限访问");
        }
        return Result.success(app);
    }
    
    @GetMapping("/getByAppId")
    @RequireApiPermission("app:view")
    public Result<Application> getByAppId(@RequestParam String appId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        Application app = applicationService.findByAppId(appId, currentUser);
        if (app == null) {
            return Result.error("应用不存在或无权限访问");
        }
        return Result.success(app);
    }
    
    @GetMapping("/listByOrg")
    @RequireApiPermission("app:list")
    public Result<List<Application>> listByOrg(@RequestParam Long orgId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(applicationService.findByOrganizationId(orgId, currentUser));
    }
    
    @GetMapping("/listByType")
    @RequireApiPermission("app:list")
    public Result<List<Application>> listByType(@RequestParam Integer appType, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(applicationService.findByAppType(appType, currentUser));
    }
    
    @PostMapping("/save")
    @RequireApiPermission("app:save")
    public Result<String> save(@RequestBody Application app, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        if (app.getAppId() == null || app.getAppId().trim().isEmpty()) {
            return Result.error("应用ID不能为空");
        }
        if (app.getAppName() == null || app.getAppName().trim().isEmpty()) {
            return Result.error("应用名称不能为空");
        }
        if (app.getOrganizationId() == null) {
            return Result.error("组织机构ID不能为空");
        }
        
        if (applicationService.save(app, currentUser)) {
            return Result.success("保存成功", null);
        }
        return Result.error("保存失败，可能原因：应用ID已存在或无权限操作该组织机构");
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
            return Result.success("修改成功", null);
        }
        return Result.error("修改失败，可能原因：应用不存在或无权限操作");
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
        return Result.error("删除失败，可能原因：应用不存在或无权限操作");
    }
    
    @PostMapping("/updateStatus")
    @RequireApiPermission("app:update")
    public Result<String> updateStatus(@RequestParam Long id, @RequestParam Integer status, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        if (status != 0 && status != 1) {
            return Result.error("状态值无效，只能是0(停用)或1(启用)");
        }
        
        if (applicationService.updateStatus(id, status, currentUser)) {
            return Result.success("状态更新成功", null);
        }
        return Result.error("状态更新失败，可能原因：应用不存在或无权限操作");
    }
}

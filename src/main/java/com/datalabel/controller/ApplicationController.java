package com.datalabel.controller;

import com.datalabel.annotation.RequireApiPermission;
import com.datalabel.common.DataPermissionUtils;
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
    
    @Autowired
    private DataPermissionUtils dataPermissionUtils;
    
    @GetMapping("/list")
    @RequireApiPermission("application:list")
    public Result<List<Application>> list(HttpSession session) {
        User currentUser = dataPermissionUtils.getCurrentUser(session);
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        List<Long> accessibleOrgIds = dataPermissionUtils.getAccessibleOrgIds(currentUser);
        List<Application> applications;
        
        if (accessibleOrgIds == null) {
            applications = applicationService.findAll();
        } else {
            applications = applicationService.findByOrganizationIds(accessibleOrgIds);
        }
        
        return Result.success(applications);
    }
    
    @GetMapping("/{id}")
    @RequireApiPermission("application:view")
    public Result<Application> getById(@PathVariable Long id, HttpSession session) {
        User currentUser = dataPermissionUtils.getCurrentUser(session);
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        Application app = applicationService.findById(id);
        if (app == null) {
            return Result.error("应用不存在");
        }
        
        if (!dataPermissionUtils.hasOrgPermission(currentUser, app.getOrganizationId())) {
            return Result.error(403, "无权限访问该应用");
        }
        
        return Result.success(app);
    }
    
    @PostMapping("/save")
    @RequireApiPermission("application:save")
    public Result<String> save(@RequestBody Application application, HttpSession session) {
        User currentUser = dataPermissionUtils.getCurrentUser(session);
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        if (application.getOrganizationId() != null 
                && !dataPermissionUtils.hasOrgPermission(currentUser, application.getOrganizationId())) {
            return Result.error(403, "无权限在该组织机构下创建应用");
        }
        
        if (applicationService.save(application)) {
            return Result.success("保存成功", null);
        }
        return Result.error("保存失败");
    }
    
    @PostMapping("/update")
    @RequireApiPermission("application:update")
    public Result<String> update(@RequestBody Application application, HttpSession session) {
        User currentUser = dataPermissionUtils.getCurrentUser(session);
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        Application existingApp = applicationService.findById(application.getId());
        if (existingApp == null) {
            return Result.error("应用不存在");
        }
        
        if (!dataPermissionUtils.hasOrgPermission(currentUser, existingApp.getOrganizationId())) {
            return Result.error(403, "无权限修改该应用");
        }
        
        if (application.getOrganizationId() != null 
                && !application.getOrganizationId().equals(existingApp.getOrganizationId())
                && !dataPermissionUtils.hasOrgPermission(currentUser, application.getOrganizationId())) {
            return Result.error(403, "无权限将应用移动到该组织机构");
        }
        
        if (applicationService.update(application)) {
            return Result.success("修改成功", null);
        }
        return Result.error("修改失败");
    }
    
    @DeleteMapping("/{id}")
    @RequireApiPermission("application:delete")
    public Result<String> delete(@PathVariable Long id, HttpSession session) {
        User currentUser = dataPermissionUtils.getCurrentUser(session);
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        Application app = applicationService.findById(id);
        if (app == null) {
            return Result.error("应用不存在");
        }
        
        if (!dataPermissionUtils.hasOrgPermission(currentUser, app.getOrganizationId())) {
            return Result.error(403, "无权限删除该应用");
        }
        
        if (applicationService.deleteById(id)) {
            return Result.success("删除成功", null);
        }
        return Result.error("删除失败");
    }
    
    @PostMapping("/status")
    @RequireApiPermission("application:update")
    public Result<String> updateStatus(@RequestParam Long id, @RequestParam Integer status, HttpSession session) {
        User currentUser = dataPermissionUtils.getCurrentUser(session);
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        Application app = applicationService.findById(id);
        if (app == null) {
            return Result.error("应用不存在");
        }
        
        if (!dataPermissionUtils.hasOrgPermission(currentUser, app.getOrganizationId())) {
            return Result.error(403, "无权限修改该应用状态");
        }
        
        if (applicationService.updateStatus(id, status)) {
            return Result.success("状态修改成功", null);
        }
        return Result.error("状态修改失败");
    }
    
    @PostMapping("/bindOrg")
    @RequireApiPermission("application:bindOrg")
    public Result<String> bindOrganization(@RequestParam Long applicationId, @RequestBody List<Long> orgIds, HttpSession session) {
        User currentUser = dataPermissionUtils.getCurrentUser(session);
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        Application app = applicationService.findById(applicationId);
        if (app == null) {
            return Result.error("应用不存在");
        }
        
        if (!dataPermissionUtils.hasOrgPermission(currentUser, app.getOrganizationId())) {
            return Result.error(403, "无权限绑定该应用的组织机构");
        }
        
        if (orgIds != null) {
            for (Long orgId : orgIds) {
                if (!dataPermissionUtils.hasOrgPermission(currentUser, orgId)) {
                    return Result.error(403, "无权限绑定到组织机构: " + orgId);
                }
            }
        }
        
        if (applicationService.bindOrganization(applicationId, orgIds)) {
            return Result.success("绑定成功", null);
        }
        return Result.error("绑定失败");
    }
    
    @GetMapping("/{id}/orgs")
    @RequireApiPermission("application:view")
    public Result<List<Long>> getBoundOrganizations(@PathVariable Long id, HttpSession session) {
        User currentUser = dataPermissionUtils.getCurrentUser(session);
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        Application app = applicationService.findById(id);
        if (app == null) {
            return Result.error("应用不存在");
        }
        
        if (!dataPermissionUtils.hasOrgPermission(currentUser, app.getOrganizationId())) {
            return Result.error(403, "无权限访问该应用");
        }
        
        return Result.success(applicationService.getBoundOrganizationIds(id));
    }
}

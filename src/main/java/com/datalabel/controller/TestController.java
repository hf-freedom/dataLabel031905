package com.datalabel.controller;

import com.datalabel.common.Result;
import com.datalabel.entity.*;
import com.datalabel.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {
    
    @Autowired
    private MenuService menuService;
    
    @Autowired
    private ApiPermissionService apiPermissionService;
    
    @Autowired
    private RoleMenuService roleMenuService;
    
    @Autowired
    private RoleApiService roleApiService;
    
    @Autowired
    private RoleOrganizationService roleOrganizationService;
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private DataPermissionService dataPermissionService;
    
    @Autowired
    private ApplicationService applicationService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private OrganizationService organizationService;
    
    @GetMapping("/menus")
    public Result<List<Menu>> getAllMenus() {
        return Result.success(menuService.findAll());
    }
    
    @GetMapping("/apis")
    public Result<List<ApiPermission>> getAllApis() {
        return Result.success(apiPermissionService.findAll());
    }
    
    @GetMapping("/roles")
    public Result<List<Role>> getAllRoles() {
        return Result.success(roleService.findAll());
    }
    
    @GetMapping("/role/{roleId}/menus")
    public Result<List<Long>> getRoleMenus(@PathVariable Long roleId) {
        return Result.success(roleMenuService.findMenuIdsByRoleId(roleId));
    }
    
    @GetMapping("/role/{roleId}/apis")
    public Result<List<Long>> getRoleApis(@PathVariable Long roleId) {
        return Result.success(roleApiService.findApiIdsByRoleId(roleId));
    }
    
    @GetMapping("/role/{roleId}/orgs")
    public Result<List<Long>> getRoleOrgs(@PathVariable Long roleId) {
        return Result.success(roleOrganizationService.findOrgIdsByRoleId(roleId));
    }
    
    @GetMapping("/menu/tree")
    public Result<List<?>> getMenuTree(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        List<Menu> menus;
        if (currentUser.getUserType() != null && currentUser.getUserType() == 1) {
            menus = menuService.findAll();
        } else {
            Long roleId = currentUser.getRoleId();
            if (roleId == null) {
                menus = java.util.Collections.emptyList();
            } else {
                menus = menuService.findMenusByRoleId(roleId);
            }
        }
        return Result.success(menuService.buildMenuTree(menus));
    }
    
    @GetMapping("/data-permission/info")
    public Result<DataPermissionInfo> getDataPermissionInfo(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        DataPermissionInfo info = new DataPermissionInfo();
        info.setUserId(currentUser.getId());
        info.setUsername(currentUser.getUsername());
        info.setUserType(currentUser.getUserType());
        info.setIsAdmin(dataPermissionService.isAdmin(currentUser));
        
        List<Long> accessibleOrgIds = dataPermissionService.getAccessibleOrgIds(currentUser);
        info.setAccessibleOrgIds(accessibleOrgIds);
        
        if (accessibleOrgIds != null && !accessibleOrgIds.isEmpty()) {
            List<Organization> orgs = organizationService.findAll();
            List<String> orgNames = orgs.stream()
                    .filter(o -> accessibleOrgIds.contains(o.getId()))
                    .map(Organization::getName)
                    .collect(java.util.stream.Collectors.toList());
            info.setAccessibleOrgNames(orgNames);
        }
        
        return Result.success(info);
    }
    
    @GetMapping("/data-permission/apps")
    public Result<List<Application>> getAccessibleApps(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(applicationService.findAccessibleApplications(currentUser));
    }
    
    @GetMapping("/data-permission/users")
    public Result<List<User>> getAccessibleUsers(HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        return Result.success(userService.findAccessibleUsers(currentUser));
    }
    
    @PostMapping("/data-permission/create-test-app")
    public Result<Application> createTestApp(@RequestParam Long orgId, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return Result.error(401, "未登录");
        }
        
        Application app = new Application();
        app.setAppName("测试应用_" + System.currentTimeMillis());
        app.setOrganizationId(orgId);
        app.setAppType(0);
        app.setDescription("测试应用描述");
        app.setStatus(1);
        app.setDeleted(0);
        
        if (applicationService.save(app, currentUser)) {
            return Result.success("创建成功", app);
        }
        return Result.error("创建失败，可能没有权限操作该组织机构");
    }
    
    public static class DataPermissionInfo {
        private Long userId;
        private String username;
        private Integer userType;
        private Boolean isAdmin;
        private List<Long> accessibleOrgIds;
        private List<String> accessibleOrgNames;
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public Integer getUserType() { return userType; }
        public void setUserType(Integer userType) { this.userType = userType; }
        public Boolean getIsAdmin() { return isAdmin; }
        public void setIsAdmin(Boolean isAdmin) { this.isAdmin = isAdmin; }
        public List<Long> getAccessibleOrgIds() { return accessibleOrgIds; }
        public void setAccessibleOrgIds(List<Long> accessibleOrgIds) { this.accessibleOrgIds = accessibleOrgIds; }
        public List<String> getAccessibleOrgNames() { return accessibleOrgNames; }
        public void setAccessibleOrgNames(List<String> accessibleOrgNames) { this.accessibleOrgNames = accessibleOrgNames; }
    }
}

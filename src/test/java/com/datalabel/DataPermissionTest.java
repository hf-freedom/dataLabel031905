package com.datalabel;

import com.datalabel.cache.LocalCache;
import com.datalabel.common.DataPermissionUtils;
import com.datalabel.entity.Application;
import com.datalabel.entity.Organization;
import com.datalabel.entity.Role;
import com.datalabel.entity.RoleOrganization;
import com.datalabel.entity.User;
import com.datalabel.service.ApplicationService;
import com.datalabel.service.RoleOrganizationService;
import com.datalabel.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DataPermissionTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private RoleOrganizationService roleOrganizationService;

    @Autowired
    private DataPermissionUtils dataPermissionUtils;

    private User adminUser;
    private User normalUser;
    private Organization org1;
    private Organization org2;
    private Role adminRole;
    private Role userRole;

    @BeforeEach
    void setUp() {
        LocalCache cache = LocalCache.getInstance();
        cache.clear();

        adminUser = new User(cache.generateId(), "admin", "admin123", "管理员");
        adminUser.setUserType(1);
        cache.put(adminUser.getId(), adminUser);

        normalUser = new User(cache.generateId(), "user", "user123", "普通用户");
        normalUser.setUserType(0);
        cache.put(normalUser.getId(), normalUser);

        org1 = new Organization();
        org1.setId(cache.generateId());
        org1.setName("总公司");
        org1.setCode("HQ");
        cache.put(org1.getId(), org1);

        org2 = new Organization();
        org2.setId(cache.generateId());
        org2.setName("技术部");
        org2.setCode("TECH");
        cache.put(org2.getId(), org2);

        adminRole = new Role();
        adminRole.setId(cache.generateId());
        adminRole.setName("管理员角色");
        cache.put(adminRole.getId(), adminRole);

        userRole = new Role();
        userRole.setId(cache.generateId());
        userRole.setName("普通用户角色");
        cache.put(userRole.getId(), userRole);

        RoleOrganization ro1 = new RoleOrganization();
        ro1.setId(cache.generateId());
        ro1.setRoleId(userRole.getId());
        ro1.setOrganizationId(org2.getId());
        cache.put(ro1.getId(), ro1);

        normalUser.setRoleId(userRole.getId());
        normalUser.setOrganizationId(org2.getId());
        cache.put(normalUser.getId(), normalUser);
    }

    @Test
    void testIsAdmin() {
        assertTrue(dataPermissionUtils.isAdmin(adminUser));
        assertFalse(dataPermissionUtils.isAdmin(normalUser));
    }

    @Test
    void testGetAccessibleOrgIds() {
        List<Long> adminOrgs = dataPermissionUtils.getAccessibleOrgIds(adminUser);
        assertNull(adminOrgs);

        List<Long> userOrgs = dataPermissionUtils.getAccessibleOrgIds(normalUser);
        assertNotNull(userOrgs);
        assertEquals(1, userOrgs.size());
        assertEquals(org2.getId(), userOrgs.get(0));
    }

    @Test
    void testHasOrgPermission() {
        assertTrue(dataPermissionUtils.hasOrgPermission(adminUser, org1.getId()));
        assertTrue(dataPermissionUtils.hasOrgPermission(adminUser, org2.getId()));

        assertFalse(dataPermissionUtils.hasOrgPermission(normalUser, org1.getId()));
        assertTrue(dataPermissionUtils.hasOrgPermission(normalUser, org2.getId()));
    }

    @Test
    void testApplicationCRUD() {
        Application app = new Application();
        app.setName("测试应用");
        app.setOrganizationId(org2.getId());
        app.setType(0);
        app.setDescription("测试应用描述");

        applicationService.save(app);
        assertNotNull(app.getId());

        Application found = applicationService.findById(app.getId());
        assertNotNull(found);
        assertEquals("测试应用", found.getName());

        app.setName("更新后的应用");
        applicationService.update(app);

        Application updated = applicationService.findById(app.getId());
        assertEquals("更新后的应用", updated.getName());

        applicationService.deleteById(app.getId());
        Application deleted = applicationService.findById(app.getId());
        assertNull(deleted);
    }

    @Test
    void testFindApplicationsByOrgIds() {
        LocalCache cache = LocalCache.getInstance();

        Application app1 = new Application(cache.generateId(), "应用1", org1.getId(), 0, "应用1描述");
        app1.setStatus(1);
        app1.setDeleted(false);
        cache.put(app1.getId(), app1);

        Application app2 = new Application(cache.generateId(), "应用2", org2.getId(), 1, "应用2描述");
        app2.setStatus(1);
        app2.setDeleted(false);
        cache.put(app2.getId(), app2);

        List<Long> orgIds = new ArrayList<>();
        orgIds.add(org2.getId());
        List<Application> apps = applicationService.findByOrganizationIds(orgIds);
        assertEquals(1, apps.size());
        assertEquals("应用2", apps.get(0).getName());
    }
}

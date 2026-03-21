# 测试脚本 - 验证应用功能和数据权限

$baseUrl = "http://localhost:8082"

# 1. 管理员登录
Write-Host "=== 1. 管理员登录 ===" -ForegroundColor Green
$adminLogin = Invoke-WebRequest -Uri "$baseUrl/login" -Method POST -Body @{username='admin';password='admin123';userType=1} -UseBasicParsing -SessionVariable adminSession
($adminLogin.Content | ConvertFrom-Json).data.username

# 2. 管理员查询所有应用
Write-Host "`n=== 2. 管理员查询所有应用 ===" -ForegroundColor Green
$appList = Invoke-WebRequest -Uri "$baseUrl/api/app/list" -Method GET -UseBasicParsing -WebSession $adminSession
$apps = ($appList.Content | ConvertFrom-Json).data
Write-Host "应用数量: $($apps.Count)"
$apps | ForEach-Object { Write-Host "  - $($_.appName) (Org: $($_.organizationId))" }

# 3. 管理员查询所有用户
Write-Host "`n=== 3. 管理员查询所有用户 ===" -ForegroundColor Green
$userList = Invoke-WebRequest -Uri "$baseUrl/api/user/list" -Method GET -UseBasicParsing -WebSession $adminSession
$users = ($userList.Content | ConvertFrom-Json).data
Write-Host "用户数量: $($users.Count)"
$users | ForEach-Object { Write-Host "  - $($_.username) (Org: $($_.organizationId))" }

# 4. 普通用户登录
Write-Host "`n=== 4. 普通用户(testuser)登录 ===" -ForegroundColor Green
$userLogin = Invoke-WebRequest -Uri "$baseUrl/login" -Method POST -Body @{username='testuser';password='123456';userType=0} -UseBasicParsing -SessionVariable userSession
($userLogin.Content | ConvertFrom-Json).data.username

# 5. 普通用户查询应用(只能看到技术部的应用)
Write-Host "`n=== 5. 普通用户查询应用(数据权限控制) ===" -ForegroundColor Green
$userAppList = Invoke-WebRequest -Uri "$baseUrl/api/app/list" -Method GET -UseBasicParsing -WebSession $userSession
$userApps = ($userAppList.Content | ConvertFrom-Json).data
Write-Host "应用数量: $($userApps.Count) (应该只有技术部的2个)"
$userApps | ForEach-Object { Write-Host "  - $($_.appName) (Org: $($_.organizationId))" }

# 6. 普通用户查询用户列表(只能看到技术部的用户)
Write-Host "`n=== 6. 普通用户查询用户列表(数据权限控制) ===" -ForegroundColor Green
$userUserList = Invoke-WebRequest -Uri "$baseUrl/api/user/list" -Method GET -UseBasicParsing -WebSession $userSession
$userUsers = ($userUserList.Content | ConvertFrom-Json).data
Write-Host "用户数量: $($userUsers.Count) (应该只有1个)"
$userUsers | ForEach-Object { Write-Host "  - $($_.username) (Org: $($_.organizationId))" }

# 7. 普通用户尝试访问市场部的应用(应该失败)
Write-Host "`n=== 7. 普通用户尝试查询市场部应用(无权限) ===" -ForegroundColor Green
$marketApp = $apps | Where-Object { $_.appName -like "*市场*" } | Select-Object -First 1
if ($marketApp) {
    Write-Host "市场部应用 ID: $($marketApp.id), Org: $($marketApp.organizationId)"
    try {
        $viewResult = Invoke-WebRequest -Uri "$baseUrl/api/app/$($marketApp.id)" -Method GET -UseBasicParsing -WebSession $userSession
        Write-Host "结果: $($viewResult.Content)"
    } catch {
        Write-Host "访问被拒绝(符合预期): 无权限访问该应用"
    }
}

# 8. 管理员创建新应用
Write-Host "`n=== 8. 管理员创建新应用 ===" -ForegroundColor Green
$newApp = @{
    appId = "APP004"
    appName = "测试应用"
    organizationId = 5  # 技术部
    appType = 0
    description = "测试创建应用"
    status = 1
} | ConvertTo-Json

$createResult = Invoke-WebRequest -Uri "$baseUrl/api/app/save" -Method POST -Body $newApp -ContentType "application/json" -UseBasicParsing -WebSession $adminSession
Write-Host "创建结果: $(($createResult.Content | ConvertFrom-Json).message)"

# 9. 普通用户停用应用
Write-Host "`n=== 9. 普通用户停用应用(技术部的应用，有权限) ===" -ForegroundColor Green
$techApp = $userApps | Select-Object -First 1
if ($techApp) {
    Write-Host "停用应用: $($techApp.appName) (ID: $($techApp.id))"
    $disableResult = Invoke-WebRequest -Uri "$baseUrl/api/app/updateStatus?id=$($techApp.id)&status=0" -Method POST -UseBasicParsing -WebSession $userSession
    Write-Host "结果: $(($disableResult.Content | ConvertFrom-Json).message)"
}

# 10. 验证应用状态已更新
Write-Host "`n=== 10. 验证应用状态已更新 ===" -ForegroundColor Green
$appDetail = Invoke-WebRequest -Uri "$baseUrl/api/app/$($techApp.id)" -Method GET -UseBasicParsing -WebSession $userSession
$appData = ($appDetail.Content | ConvertFrom-Json).data
Write-Host "应用状态: $($appData.status) (0=停用, 1=启用)"

# 11. 普通用户删除应用
Write-Host "`n=== 11. 普通用户删除应用(技术部的应用，有权限) ===" -ForegroundColor Green
$deleteResult = Invoke-WebRequest -Uri "$baseUrl/api/app/$($techApp.id)" -Method DELETE -UseBasicParsing -WebSession $userSession
Write-Host "删除结果: $(($deleteResult.Content | ConvertFrom-Json).message)"

# 12. 验证应用已删除(逻辑删除)
Write-Host "`n=== 12. 验证应用已删除(逻辑删除) ===" -ForegroundColor Green
try {
    $checkResult = Invoke-WebRequest -Uri "$baseUrl/api/app/$($techApp.id)" -Method GET -UseBasicParsing -WebSession $userSession
    Write-Host "结果: $($checkResult.Content)"
} catch {
    Write-Host "应用已删除，无法访问(符合预期)"
}

Write-Host "`n=== 测试完成 ===" -ForegroundColor Green

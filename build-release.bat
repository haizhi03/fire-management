@echo off
chcp 65001 >nul
echo ========================================
echo 消防设施管理系统 - 打包脚本
echo ========================================
echo.

REM 设置变量
set RELEASE_DIR=release
set TIMESTAMP=%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%

echo [1/5] 清理旧的打包文件...
if exist %RELEASE_DIR% (
    rmdir /s /q %RELEASE_DIR%
)
mkdir %RELEASE_DIR%
mkdir %RELEASE_DIR%\backend
mkdir %RELEASE_DIR%\frontend
mkdir %RELEASE_DIR%\sql
mkdir %RELEASE_DIR%\docs
echo 清理完成
echo.

echo [2/5] 打包后端 JAR 包...
cd backend
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo 错误：后端打包失败
    pause
    exit /b 1
)
copy target\fire-management-system-1.0.0.jar ..\%RELEASE_DIR%\backend\
copy application.yml ..\%RELEASE_DIR%\backend\ 2>nul
cd ..
echo 后端打包完成
echo.

echo [3/5] 打包前端静态文件...
cd web-admin
call npm run build
if %errorlevel% neq 0 (
    echo 错误：前端打包失败
    pause
    exit /b 1
)
xcopy /E /I /Y dist ..\%RELEASE_DIR%\frontend\dist
cd ..
echo 前端打包完成
echo.

echo [4/5] 复制数据库脚本和文档...
copy backend\INIT_ALL.sql %RELEASE_DIR%\sql\
copy backend\src\main\resources\sql\schema.sql %RELEASE_DIR%\sql\
copy backend\src\main\resources\sql\data.sql %RELEASE_DIR%\sql\
copy README.md %RELEASE_DIR%\docs\
copy QUICKSTART.md %RELEASE_DIR%\docs\
copy backend\DATABASE.md %RELEASE_DIR%\docs\
copy backend\SECURITY.md %RELEASE_DIR%\docs\
copy backend\PERFORMANCE.md %RELEASE_DIR%\docs\
echo 文件复制完成
echo.

echo [5/5] 创建部署说明...
(
echo # 消防设施管理系统 - 部署包
echo.
echo ## 版本信息
echo - 打包时间：%TIMESTAMP%
echo - 版本号：1.0.0
echo.
echo ## 目录结构
echo ```
echo release/
echo ├── backend/                    # 后端服务
echo │   ├── fire-management-system-1.0.0.jar
echo │   └── application.yml         # 配置文件
echo ├── frontend/                   # 前端静态文件
echo │   └── dist/
echo ├── sql/                        # 数据库脚本
echo │   ├── INIT_ALL.sql           # 一键初始化脚本
echo │   ├── schema.sql             # 表结构
echo │   └── data.sql               # 测试数据
echo └── docs/                       # 文档
echo     ├── README.md
echo     ├── QUICKSTART.md
echo     ├── DATABASE.md
echo     ├── SECURITY.md
echo     └── PERFORMANCE.md
echo ```
echo.
echo ## 快速部署
echo.
echo ### 1. 环境要求
echo - Java 11+
echo - MySQL 8.0+
echo - Nginx ^(可选^)
echo.
echo ### 2. 数据库初始化
echo ```bash
echo # 方法1：使用一键初始化脚本
echo mysql -u root -p --default-character-set=utf8mb4 ^< sql/INIT_ALL.sql
echo.
echo # 方法2：分步执行
echo mysql -u root -p --default-character-set=utf8mb4 ^< sql/schema.sql
echo mysql -u root -p --default-character-set=utf8mb4 fire_management ^< sql/data.sql
echo ```
echo.
echo ### 3. 配置后端
echo 编辑 `backend/application.yml`，修改数据库连接信息：
echo ```yaml
echo spring:
echo   datasource:
echo     url: jdbc:mysql://localhost:3306/fire_management
echo     username: root
echo     password: 你的密码
echo ```
echo.
echo ### 4. 启动后端服务
echo ```bash
echo cd backend
echo java -jar fire-management-system-1.0.0.jar
echo ```
echo.
echo 或者使用后台运行：
echo ```bash
echo nohup java -jar fire-management-system-1.0.0.jar ^> app.log 2^>^&1 ^&
echo ```
echo.
echo ### 5. 部署前端
echo.
echo #### 方法1：使用 Nginx ^(推荐^)
echo 将 `frontend/dist` 目录复制到 Nginx 的 html 目录，配置示例：
echo ```nginx
echo server {
echo     listen 80;
echo     server_name your-domain.com;
echo     
echo     location / {
echo         root /path/to/frontend/dist;
echo         try_files $uri $uri/ /index.html;
echo     }
echo     
echo     location /api/ {
echo         proxy_pass http://localhost:8080/api/;
echo         proxy_set_header Host $host;
echo         proxy_set_header X-Real-IP $remote_addr;
echo     }
echo }
echo ```
echo.
echo #### 方法2：直接访问
echo 后端已配置静态资源访问，直接访问：
echo http://localhost:8080
echo.
echo ### 6. 访问系统
echo - 前端地址：http://localhost:80 ^(Nginx^) 或 http://localhost:8080
echo - 后端API：http://localhost:8080/api
echo.
echo ### 7. 测试账号
echo - 管理员：admin / 123456
echo - 采集员：collector01 / 123456
echo - 审核员：auditor01 / 123456
echo.
echo ## 生产环境建议
echo 1. 修改 JWT 密钥 ^(application.yml 中的 jwt.secret^)
echo 2. 修改数据库密码
echo 3. 配置 HTTPS
echo 4. 配置防火墙规则
echo 5. 定期备份数据库
echo.
echo ## 故障排查
echo 详见 docs/ 目录下的文档
echo.
echo ## 技术支持
echo 如有问题，请查看文档或联系技术支持
) > %RELEASE_DIR%\DEPLOY.md

echo 部署说明创建完成
echo.

echo ========================================
echo 打包完成！
echo ========================================
echo.
echo 打包文件位置：%RELEASE_DIR%\
echo.
echo 文件清单：
dir /B %RELEASE_DIR%
echo.
echo 后续步骤：
echo 1. 将 %RELEASE_DIR% 文件夹打包成 ZIP
echo 2. 发送给需要部署的人员
echo 3. 参考 %RELEASE_DIR%\DEPLOY.md 进行部署
echo.
pause

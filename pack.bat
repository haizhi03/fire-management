@echo off
chcp 65001 >nul
echo ========================================
echo 消防设施管理系统 - 前后端打包脚本
echo ========================================
echo.

:: 检查环境
echo [检查] 验证构建环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到 Java，请安装 JDK 11+
    pause
    exit /b 1
)
mvn -version >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到 Maven
    pause
    exit /b 1
)
node -v >nul 2>&1
if errorlevel 1 (
    echo 错误: 未找到 Node.js
    pause
    exit /b 1
)
echo 环境检查通过！
echo.

:: ========== 后端打包 ==========
echo [1/3] 打包后端...
cd backend
call mvn clean package -DskipTests
if errorlevel 1 (
    echo 错误: 后端打包失败！
    cd ..
    pause
    exit /b 1
)

:: 复制 JAR 和配置文件到 release/backend
if not exist ..\release\backend mkdir ..\release\backend
copy target\fire-management-system-1.0.0.jar ..\release\backend\
copy src\main\resources\application.yml ..\release\backend\application.yml
cd ..
echo 后端打包完成！

:: ========== 前端打包 ==========
echo.
echo [2/3] 打包前端...
cd release\web-admin
call npm install
call npm run build
if errorlevel 1 (
    echo 错误: 前端打包失败！
    cd ..\..
    pause
    exit /b 1
)
cd ..\..
echo 前端打包完成！

:: ========== 创建启动脚本 ==========
echo.
echo [3/3] 更新启动脚本...

:: 后端启动脚本
(
echo @echo off
echo chcp 65001 ^>nul
echo title 消防设施管理系统 - 后端服务
echo echo ========================================
echo echo    消防设施管理系统 - 后端服务
echo echo ========================================
echo echo.
echo echo 配置文件: application.yml
echo echo 如需修改数据库等配置，请编辑 application.yml 文件
echo echo.
echo echo ----------------------------------------
echo echo 启动前请确保：
echo echo 1. 已安装 JDK 11+
echo echo 2. MySQL 服务已启动
echo echo 3. 数据库已初始化（执行 ../sql/fire_management_full.sql）
echo echo 4. application.yml 中的数据库密码已修改
echo echo ----------------------------------------
echo echo.
echo echo 按任意键启动服务...
echo pause ^>nul
echo echo.
echo echo 正在启动后端服务，请稍候...
echo echo 服务地址: http://localhost:9090
echo echo.
echo java -jar fire-management-system-1.0.0.jar --spring.config.location=application.yml
echo echo.
echo echo 服务已停止
echo pause
) > release\backend\start.bat

echo.
echo ========================================
echo 打包完成！
echo ========================================
echo.
echo 发布文件位置: release\
echo.
echo 目录结构:
echo   release\backend\              后端程序
echo     - fire-management-system-1.0.0.jar
echo     - application.yml           配置文件（可修改）
echo     - start.bat                 启动脚本
echo   release\web-admin\            前端项目
echo     - start.bat                 启动脚本
echo     - dist\                     构建后的静态文件
echo   release\sql\                  数据库脚本
echo   release\docs\                 文档
echo.
echo 使用步骤:
echo 1. 修改 release\backend\application.yml 中的数据库密码
echo 2. 执行 release\sql\fire_management_full.sql 初始化数据库
echo 3. 运行 release\backend\start.bat 启动后端
echo 4. 运行 release\web-admin\start.bat 启动前端
echo.
pause
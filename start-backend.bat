@echo off
echo ========================================
echo 消防设施管理系统 - 后端启动脚本
echo ========================================
echo.

echo [1/3] 检查Java环境...
java -version
if errorlevel 1 (
    echo 错误: 未找到Java，请先安装JDK 11+
    pause
    exit /b 1
)
echo.

echo [2/3] 检查MySQL连接...
mysql -u root -proot -e "SELECT 1" >nul 2>&1
if errorlevel 1 (
    echo 警告: 无法连接MySQL，请确保MySQL已启动
    echo 请手动检查MySQL服务状态
    pause
)
echo.

echo [3/3] 启动Spring Boot应用...
cd backend
mvn spring-boot:run

pause

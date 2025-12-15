@echo off
chcp 65001 >nul
echo ========================================
echo 消防设施管理系统 - 一键启动
echo ========================================
echo.

REM 检查 Java
echo [检查] Java 环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误：未安装 Java 或 Java 不在 PATH 中
    echo 请安装 Java 11 或更高版本
    pause
    exit /b 1
)
echo Java 环境正常
echo.

REM 检查 MySQL
echo [检查] MySQL 服务...
sc query MySQL80 | find "RUNNING" >nul
if %errorlevel% neq 0 (
    echo MySQL 服务未运行，正在启动...
    net start MySQL80
    if %errorlevel% neq 0 (
        echo 警告：无法启动 MySQL 服务
        echo 请手动启动 MySQL
        pause
    )
)
echo MySQL 服务正常
echo.

REM 启动后端
echo [启动] 后端服务...
cd backend
start "消防系统后端" java -jar fire-management-system-1.0.0.jar
echo 后端服务已启动（端口 8080）
echo.

REM 等待后端启动
echo 等待后端服务启动...
timeout /t 10 /nobreak >nul

REM 打开浏览器
echo [启动] 打开浏览器...
start http://localhost:8080
echo.

echo ========================================
echo 系统启动完成！
echo ========================================
echo.
echo 访问地址：http://localhost:8080
echo 测试账号：admin / 123456
echo.
echo 按任意键关闭此窗口（后端服务将继续运行）
pause >nul

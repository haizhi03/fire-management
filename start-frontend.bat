@echo off
echo ========================================
echo 消防设施管理系统 - 前端启动脚本
echo ========================================
echo.

echo [1/2] 检查Node.js环境...
node -v
if errorlevel 1 (
    echo 错误: 未找到Node.js，请先安装Node.js 16+
    pause
    exit /b 1
)
echo.

echo [2/2] 启动前端开发服务器...
cd web-admin
call npm install
call npm run dev

pause

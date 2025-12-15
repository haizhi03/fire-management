@echo off
chcp 65001 >nul
echo ========================================
echo 创建源代码压缩包
echo ========================================
echo.

REM 设置变量
set TIMESTAMP=%date:~0,4%%date:~5,2%%date:~8,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%
set ZIP_NAME=fire-management-source-code-%TIMESTAMP%.zip
set TEMP_DIR=temp_source

echo [1/3] 准备临时目录...
if exist %TEMP_DIR% (
    rmdir /s /q %TEMP_DIR%
)
mkdir %TEMP_DIR%
echo 临时目录创建完成
echo.

echo [2/3] 复制源代码文件...
echo 正在复制，请稍候...

REM 复制后端源码
echo - 复制后端源码...
xcopy /E /I /Y backend\src %TEMP_DIR%\backend\src >nul
xcopy /E /I /Y backend\pom.xml %TEMP_DIR%\backend\ >nul
copy backend\*.sql %TEMP_DIR%\backend\ >nul 2>nul
copy backend\*.md %TEMP_DIR%\backend\ >nul 2>nul

REM 复制前端源码
echo - 复制前端源码...
xcopy /E /I /Y web-admin\src %TEMP_DIR%\web-admin\src >nul
copy web-admin\package.json %TEMP_DIR%\web-admin\ >nul
copy web-admin\package-lock.json %TEMP_DIR%\web-admin\ >nul 2>nul
copy web-admin\vite.config.js %TEMP_DIR%\web-admin\ >nul
copy web-admin\index.html %TEMP_DIR%\web-admin\ >nul
copy web-admin\*.md %TEMP_DIR%\web-admin\ >nul 2>nul

REM 复制移动端源码
echo - 复制移动端源码...
if exist mobile-app (
    xcopy /E /I /Y mobile-app\src %TEMP_DIR%\mobile-app\src >nul
    copy mobile-app\package.json %TEMP_DIR%\mobile-app\ >nul 2>nul
    copy mobile-app\capacitor.config.json %TEMP_DIR%\mobile-app\ >nul 2>nul
    copy mobile-app\vite.config.js %TEMP_DIR%\mobile-app\ >nul 2>nul
    copy mobile-app\index.html %TEMP_DIR%\mobile-app\ >nul 2>nul
)

REM 复制根目录文档和脚本
echo - 复制文档和脚本...
copy README.md %TEMP_DIR%\ >nul
copy QUICKSTART.md %TEMP_DIR%\ >nul 2>nul
copy START.md %TEMP_DIR%\ >nul 2>nul
copy START_HERE.md %TEMP_DIR%\ >nul 2>nul
copy PROJECT_SUMMARY.md %TEMP_DIR%\ >nul 2>nul
copy TEST_API.md %TEMP_DIR%\ >nul 2>nul
copy TROUBLESHOOTING.md %TEMP_DIR%\ >nul 2>nul
copy HOW_TO_ADD_TEST_DATA.md %TEMP_DIR%\ >nul 2>nul
copy .gitignore %TEMP_DIR%\ >nul 2>nul
copy *.bat %TEMP_DIR%\ >nul 2>nul

REM 复制中文文档
copy *.md %TEMP_DIR%\ >nul 2>nul

echo 文件复制完成
echo.

echo [3/3] 创建压缩包...
powershell -Command "Compress-Archive -Path '%TEMP_DIR%\*' -DestinationPath '%ZIP_NAME%' -Force"

if %errorlevel% neq 0 (
    echo 错误：压缩失败
    rmdir /s /q %TEMP_DIR%
    pause
    exit /b 1
)

REM 清理临时目录
echo 清理临时文件...
rmdir /s /q %TEMP_DIR%

echo.
echo ========================================
echo 源代码打包完成！
echo ========================================
echo.
echo 压缩文件：%ZIP_NAME%
echo.

REM 显示文件信息
powershell -Command "Get-Item '%ZIP_NAME%' | Select-Object @{Name='文件名';Expression={$_.Name}}, @{Name='大小(MB)';Expression={[math]::Round($_.Length / 1MB, 2)}}, @{Name='创建时间';Expression={$_.LastWriteTime}} | Format-List"

echo.
echo 压缩包包含：
echo ✓ 后端完整源码（backend/src + pom.xml）
echo ✓ 前端完整源码（web-admin/src + package.json）
echo ✓ 移动端源码（如果存在）
echo ✓ 数据库脚本
echo ✓ 所有文档
echo ✓ 启动脚本
echo.
echo 已排除：
echo ✗ .kiro 目录
echo ✗ node_modules 目录
echo ✗ target 目录（编译产物）
echo ✗ dist 目录（构建产物）
echo ✗ .git 目录
echo ✗ .idea 目录
echo ✗ .vscode 目录
echo.
pause

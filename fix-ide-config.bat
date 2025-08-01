@echo off
echo 正在修复IDE Java配置...

REM 设置Java环境变量
set JAVA_HOME=C:\Program Files\Android\Android Studio1\jbr
set PATH=%JAVA_HOME%\bin;%PATH%

echo JAVA_HOME已设置为: %JAVA_HOME%

REM 验证Java版本
echo 验证Java版本:
"%JAVA_HOME%\bin\java.exe" -version

REM 清理Gradle缓存
echo 清理Gradle缓存...
if exist .gradle rmdir /s /q .gradle
if exist build rmdir /s /q build
if exist app\build rmdir /s /q app\build

REM 重新构建项目
echo 重新构建项目...
gradlew.bat clean build

echo IDE配置修复完成！
pause
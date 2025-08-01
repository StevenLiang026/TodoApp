@echo off
echo 设置Java环境变量...

REM 设置JAVA_HOME
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.13.11-hotspot

REM 更新PATH
set PATH=%JAVA_HOME%\bin;%PATH%

REM 清除可能冲突的Android Studio JDK设置
set ANDROID_STUDIO_JDK=

echo JAVA_HOME已设置为: %JAVA_HOME%
echo 验证Java版本:
java -version

echo.
echo Java环境设置完成！
pause
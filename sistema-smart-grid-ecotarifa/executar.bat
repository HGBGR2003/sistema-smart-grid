@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"

where javaw >nul 2>nul
if errorlevel 1 (
  where java >nul 2>nul
  if errorlevel 1 (
    echo Instale o JDK 21 ou mais recente e abra este arquivo novamente.
    pause
    exit /b 1
  )
)

if exist "dist\smart-grid-ecotarifa.jar" (
  java -Dfile.encoding=UTF-8 -jar "dist\smart-grid-ecotarifa.jar" %*
) else if exist "target\smart-grid-ecotarifa.jar" (
  java -Dfile.encoding=UTF-8 -jar "target\smart-grid-ecotarifa.jar" %*
) else (
  echo JAR executavel nao encontrado. Execute "mvn package" primeiro.
  pause
  exit /b 1
)

if errorlevel 1 (
  echo.
  echo Ocorreu um erro ao executar o sistema. Verifique a versao com java -version.
  pause
  exit /b 1
)

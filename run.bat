@echo off
echo Starting Smart Examination Anti-Cheating System...
echo.

REM Check if Maven is available
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven (mvn) is not found in PATH
    echo Please install Maven or use your IDE to run the application
    echo.
    echo Alternative: Use your IDE's "Run" feature on AntiCheatingApplication.java
    echo The application will start on http://localhost:8080
    echo API documentation will be available at http://localhost:8080/swagger-ui.html
    pause
    exit /b 1
)

echo Compiling and starting the application...
mvn clean compile spring-boot:run

echo.
echo Application should be running on http://localhost:8080
echo API documentation: http://localhost:8080/swagger-ui.html
pause
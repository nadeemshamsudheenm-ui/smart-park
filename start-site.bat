@echo off
title SMART PARK Web Site Launcher
echo ========================================================
echo Launching SMART PARK Web Platform...
echo ========================================================
echo.

where python >nul 2>nul
if %ERRORLEVEL% equ 0 (
    echo Starting local web server on port 8000...
    start "" http://localhost:8000
    python -m http.server 8000
) else (
    echo Opening index.html in your default web browser...
    start "" "%~dp0index.html"
)

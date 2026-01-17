# Script khởi chạy deploy Railway
# Chạy script này để bắt đầu deploy

Write-Host "🚀 Bắt Đầu Deploy Lên Railway" -ForegroundColor Green
Write-Host "==============================" -ForegroundColor Green

Write-Host "`n📋 Các file đã được chuẩn bị:" -ForegroundColor Cyan
Write-Host "   ✅ RAILWAY_ENV_VARS_READY.txt - Env vars đã điền sẵn" -ForegroundColor Green
Write-Host "   ✅ deploy-railway-auto.ps1 - Script hướng dẫn tự động" -ForegroundColor Green
Write-Host "   ✅ railway-db-helper.ps1 - Helper tạo DB connection" -ForegroundColor Green
Write-Host "   ✅ DEPLOY_NOW.md - Hướng dẫn chi tiết" -ForegroundColor Green

Write-Host "`n🎯 Chọn cách deploy:" -ForegroundColor Yellow
Write-Host "   1. Chạy script tự động (khuyên dùng)" -ForegroundColor White
Write-Host "   2. Xem hướng dẫn chi tiết" -ForegroundColor White
Write-Host "   3. Mở file env vars để copy" -ForegroundColor White
Write-Host "   4. Thoát" -ForegroundColor White

$choice = Read-Host "`nChọn (1-4)"

switch ($choice) {
    "1" {
        Write-Host "`n🚀 Đang chạy script tự động..." -ForegroundColor Cyan
        & ".\deploy-railway-auto.ps1"
    }
    "2" {
        Write-Host "`n📖 Đang mở hướng dẫn chi tiết..." -ForegroundColor Cyan
        notepad "DEPLOY_NOW.md"
    }
    "3" {
        Write-Host "`n📄 Đang mở file env vars..." -ForegroundColor Cyan
        notepad "RAILWAY_ENV_VARS_READY.txt"
        Write-Host "`n💡 Copy tất cả nội dung và paste vào Railway Dashboard → Service → Variables → Raw Editor" -ForegroundColor Yellow
    }
    "4" {
        Write-Host "`n👋 Tạm biệt!" -ForegroundColor Cyan
        exit
    }
    default {
        Write-Host "`n❌ Lựa chọn không hợp lệ!" -ForegroundColor Red
    }
}

Write-Host "`n✨ Hoàn thành!" -ForegroundColor Green

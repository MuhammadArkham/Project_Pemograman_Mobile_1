# ============================================================
#  FITME — Deploy Script
#  Jalankan dari folder root project FITME
#  Contoh: cd "C:\Users\NamaAnda\FITME" lalu .\deploy_fitme.ps1
# ============================================================

$root = Split-Path -Parent $MyInvocation.MyCommand.Path

# ── Path tujuan di project ───────────────────────────────────
$pathJavaBmi      = "$root\app\src\main\java\com\fitme\app\ui\bmi"
$pathJavaHome     = "$root\app\src\main\java\com\fitme\app\ui\home"
$pathJavaHistory  = "$root\app\src\main\java\com\fitme\app\ui\history"
$pathJavaSplash   = "$root\app\src\main\java\com\fitme\app\ui\splash"
$pathLayout       = "$root\app\src\main\res\layout"
$pathDrawable     = "$root\app\src\main\res\drawable"

# ── Folder sumber (ubah path ini sesuai lokasi file download) ─
$src = "$root\outputs"

# ── Warna output ────────────────────────────────────────────
function Write-OK   { param($msg) Write-Host "  [OK] $msg" -ForegroundColor Green }
function Write-SKIP { param($msg) Write-Host "  [--] $msg" -ForegroundColor DarkGray }
function Write-ERR  { param($msg) Write-Host "  [!!] $msg" -ForegroundColor Red }

function CopyFile {
    param($file, $dest)
    $srcPath  = "$src\$file"
    $destPath = "$dest\$file"
    if (Test-Path $srcPath) {
        Copy-Item -Path $srcPath -Destination $destPath -Force
        Write-OK "$file → $dest"
    } else {
        Write-SKIP "$file (tidak ditemukan di $src)"
    }
}

Write-Host ""
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "  FITME Deploy Script" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""

# ── 1. Java — ui/bmi ────────────────────────────────────────
Write-Host "[ Java — ui/bmi ]" -ForegroundColor Yellow
CopyFile "ResultActivity.java"          $pathJavaBmi
CopyFile "ChatAdapter.java"             $pathJavaBmi
CopyFile "GenderSelectionActivity.java" $pathJavaBmi
CopyFile "InputDataActivity.java"       $pathJavaBmi

# ── 2. Java — ui/home ───────────────────────────────────────
Write-Host ""
Write-Host "[ Java — ui/home ]" -ForegroundColor Yellow
CopyFile "HomeActivity.java" $pathJavaHome

# ── 3. Java — ui/history ────────────────────────────────────
Write-Host ""
Write-Host "[ Java — ui/history ]" -ForegroundColor Yellow
CopyFile "HistoryActivity.java" $pathJavaHistory
CopyFile "HistoryAdapter.java"  $pathJavaHistory

# ── 4. Java — ui/splash ─────────────────────────────────────
Write-Host ""
Write-Host "[ Java — ui/splash ]" -ForegroundColor Yellow
CopyFile "SplashActivity.java"  $pathJavaSplash
CopyFile "SplashViewModel.java" $pathJavaSplash
CopyFile "SplashState.java"     $pathJavaSplash

# ── 5. Layout XML ────────────────────────────────────────────
Write-Host ""
Write-Host "[ Layout XML — res/layout ]" -ForegroundColor Yellow
CopyFile "activity_result.xml"           $pathLayout
CopyFile "activity_gender_selection.xml" $pathLayout
CopyFile "activity_input_data.xml"       $pathLayout
CopyFile "activity_history.xml"          $pathLayout
CopyFile "activity_home.xml"             $pathLayout
CopyFile "item_history.xml"              $pathLayout
CopyFile "item_chat_ai.xml"              $pathLayout
CopyFile "item_chat_user.xml"            $pathLayout

# ── 6. Drawable XML ──────────────────────────────────────────
Write-Host ""
Write-Host "[ Drawable XML — res/drawable ]" -ForegroundColor Yellow
CopyFile "bg_header_pattern.xml" $pathDrawable
CopyFile "bg_bubble_ai.xml"      $pathDrawable
CopyFile "bg_bubble_user.xml"    $pathDrawable
CopyFile "bg_input_chat.xml"     $pathDrawable
CopyFile "bg_send_btn.xml"       $pathDrawable
CopyFile "bg_chip.xml"           $pathDrawable

Write-Host ""
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host "  Selesai! Buka Android Studio → Build → Rebuild Project" -ForegroundColor Cyan
Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host ""

# ============================================================
# fix_header_layouts.ps1
# Jalankan dari: PS C:\Users\Arhmm\AndroidStudioProjects\FITME
# ============================================================

$projectRoot = $PSScriptRoot
if (-not $projectRoot) { $projectRoot = Get-Location }

Write-Host "=== FitMe Header Fix Script ===" -ForegroundColor Cyan
Write-Host "Project root: $projectRoot" -ForegroundColor Gray

# ---- Daftar file yang perlu diubah ----
$files = @(
    "app\src\main\res\layout\layout_gender_screen.xml",
    "app\src\main\res\layout\layout_input_screen.xml",
    "app\src\main\res\layout\layout_result_screen.xml"
)

$fixCount = 0

foreach ($rel in $files) {
    $path = Join-Path $projectRoot $rel

    if (-not (Test-Path $path)) {
        Write-Host "[SKIP] File tidak ditemukan: $rel" -ForegroundColor Yellow
        continue
    }

    $content = Get-Content $path -Raw -Encoding UTF8

    $changed = $false

    # ----------------------------------------------------------
    # FIX 1: scaleType="centerCrop" -> scaleType="fitXY"
    # ----------------------------------------------------------
    if ($content -match 'android:scaleType="centerCrop"') {
        $content = $content -replace 'android:scaleType="centerCrop"', 'android:scaleType="fitXY"'
        Write-Host "  [OK] scaleType fixed" -ForegroundColor Green
        $changed = $true
    }

    # ----------------------------------------------------------
    # FIX 2: Tinggi layoutHeader 280dp / 300dp / 310dp -> 340dp
    # ----------------------------------------------------------
    # Cocokkan hanya pada FrameLayout dengan id layoutHeader
    # Pattern: android:layout_height="(280|290|300|310|320)dp" di dekat id layoutHeader
    # Pendekatan aman: ganti semua kemunculan nilai lama dalam blok header
    $heightPattern = '(?<=android:id="@\+id/layoutHeader"[\s\S]{0,200})android:layout_height="(280|290|300|310|320)dp"'
    if ($content -match $heightPattern) {
        $content = [regex]::Replace($content, $heightPattern, 'android:layout_height="340dp"')
        Write-Host "  [OK] layoutHeader height -> 340dp" -ForegroundColor Green
        $changed = $true
    } else {
        # Fallback: cari pola FrameLayout id layoutHeader lalu baris height-nya
        $lines     = $content -split "`n"
        $inHeader  = $false
        $headerIdx = -1

        for ($i = 0; $i -lt $lines.Count; $i++) {
            if ($lines[$i] -match 'android:id="@\+id/layoutHeader"') {
                $inHeader  = $true
                $headerIdx = $i
            }
            if ($inHeader -and ($i - $headerIdx) -le 6) {
                if ($lines[$i] -match 'android:layout_height="(2[89]\d|3[012]\d)dp"') {
                    $lines[$i] = $lines[$i] -replace 'android:layout_height="\d+dp"', 'android:layout_height="340dp"'
                    Write-Host "  [OK] layoutHeader height (fallback) -> 340dp" -ForegroundColor Green
                    $changed = $true
                    $inHeader = $false
                }
            }
            if ($inHeader -and ($i - $headerIdx) -gt 10) { $inHeader = $false }
        }

        if ($changed) { $content = $lines -join "`n" }
    }

    if ($changed) {
        Set-Content $path -Value $content -Encoding UTF8 -NoNewline
        Write-Host "[DONE] $rel" -ForegroundColor Green
        $fixCount++
    } else {
        Write-Host "[INFO] Tidak ada perubahan diperlukan: $rel" -ForegroundColor Gray
    }
}

Write-Host ""
Write-Host "=== Selesai: $fixCount file diperbarui ===" -ForegroundColor Cyan
Write-Host "Sync Gradle / Rebuild project di Android Studio." -ForegroundColor White

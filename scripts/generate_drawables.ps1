$dir = "C:\Users\Arhmm\AndroidStudioProjects\FITME\app\src\main\res\drawable"

function Write-XML($name, $content) {
    $path = Join-Path $dir $name
    $content | Out-File -FilePath $path -Encoding utf8 -Force
    Write-Host "OK $name" -ForegroundColor Green
}

Write-XML "bg_male_icon_box.xml" @"
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#1A7A4A"/>
    <corners android:radius="13dp"/>
</shape>
"@

Write-XML "bg_male_icon_box_inactive.xml" @"
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#F3F5F4"/>
    <corners android:radius="13dp"/>
</shape>
"@

Write-XML "bg_female_icon_box_active.xml" @"
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#C2185B"/>
    <corners android:radius="13dp"/>
</shape>
"@

Write-XML "bg_female_icon_box_inactive.xml" @"
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#F3F5F4"/>
    <corners android:radius="13dp"/>
</shape>
"@

Write-XML "bg_check_active_dark.xml" @"
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="#1A7A4A"/>
</shape>
"@

Write-XML "bg_check_active_pink.xml" @"
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="#C2185B"/>
</shape>
"@

Write-XML "bg_check_empty.xml" @"
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="@android:color/transparent"/>
    <stroke android:width="2dp" android:color="#DDE6E3"/>
</shape>
"@

Write-XML "bg_field_icon_filled.xml" @"
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#EAF6F0"/>
    <corners android:radius="11dp"/>
</shape>
"@

Write-XML "bg_field_icon_empty.xml" @"
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#F3F5F4"/>
    <corners android:radius="11dp"/>
</shape>
"@

Write-XML "bg_unit_badge.xml" @"
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#F3F5F4"/>
    <corners android:radius="8dp"/>
</shape>
"@

Write-XML "bg_tip_card.xml" @"
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#FFFCF0"/>
    <stroke android:width="1dp" android:color="#F0E8C0"/>
    <corners android:radius="13dp"/>
</shape>
"@

Write-XML "bg_button_main.xml" @"
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_enabled="false">
        <shape>
            <solid android:color="#0A2E1E"/>
            <corners android:radius="16dp"/>
        </shape>
    </item>
    <item android:state_pressed="true">
        <shape>
            <solid android:color="#042E22"/>
            <corners android:radius="16dp"/>
        </shape>
    </item>
    <item>
        <shape>
            <solid android:color="#0A2E1E"/>
            <corners android:radius="16dp"/>
        </shape>
    </item>
</selector>
"@

Write-XML "bg_progress_track.xml" @"
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#33FFFFFF"/>
    <corners android:radius="2dp"/>
</shape>
"@

Write-XML "bg_progress_fill.xml" @"
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#2ECC71"/>
    <corners android:radius="2dp"/>
</shape>
"@

Write-XML "bg_back_btn.xml" @"
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#1AFFFFFF"/>
    <corners android:radius="10dp"/>
</shape>
"@

Write-XML "bg_circle_decor.xml" @"
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="#FFFFFF"/>
</shape>
"@

Write-Host "`nSelesai! 16 file dibuat dengan isi lengkap." -ForegroundColor Cyan

$step1Path = "app\src\main\res\layout\activity_step1_gender.xml"
$step2Path = "app\src\main\res\layout\activity_step2_data.xml"

$step1Xml = '<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/rootLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#EEF7F4">

    <LinearLayout
        android:id="@+id/layoutHeader"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:background="@drawable/bg_splash_gradient"
        android:paddingStart="20dp"
        android:paddingEnd="20dp"
        android:paddingTop="48dp"
        android:paddingBottom="20dp"
        app:layout_constraintTop_toTopOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginBottom="14dp">

            <ImageButton
                android:id="@+id/btnBack"
                android:layout_width="38dp"
                android:layout_height="38dp"
                android:src="@drawable/ic_arrow_back"
                android:background="@drawable/bg_circle_translucent"
                android:padding="10dp"
                app:tint="#FFFFFF"
                android:contentDescription="Kembali"
                android:visibility="gone"/>

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="6dp"
                android:layout_weight="1"
                android:layout_marginEnd="12dp"
                android:orientation="horizontal"
                android:background="@drawable/bg_badge_pill"
                android:backgroundTint="#33FFFFFF"
                android:clipToOutline="true">
                <View
                    android:layout_width="0dp"
                    android:layout_height="match_parent"
                    android:layout_weight="33"
                    android:background="#FFFFFF"/>
                <View
                    android:layout_width="0dp"
                    android:layout_height="match_parent"
                    android:layout_weight="67"/>
            </LinearLayout>

            <TextView
                android:id="@+id/tvStepCount"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="1 / 3"
                android:textColor="#FFFFFF"
                android:textSize="11sp"
                android:fontFamily="@font/poppins_bold"
                android:background="@drawable/bg_circle_translucent"
                android:paddingStart="10dp"
                android:paddingEnd="10dp"
                android:paddingTop="5dp"
                android:paddingBottom="5dp"/>
        </LinearLayout>

        <TextView
            android:id="@+id/tvTitle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Pilih Gender"
            android:textColor="#FFFFFF"
            android:textSize="20sp"
            android:fontFamily="@font/poppins_bold"/>

        <TextView
            android:id="@+id/tvDescription"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="Pilih gender untuk perhitungan BMI yang presisi."
            android:textColor="#CCF0E4"
            android:textSize="13sp"
            android:fontFamily="@font/poppins_regular"
            android:lineSpacingMultiplier="1.25"/>
    </LinearLayout>

    <com.google.android.material.card.MaterialCardView
        android:id="@+id/wrapperCard"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="20dp"
        android:layout_marginEnd="20dp"
        android:layout_marginTop="16dp"
        app:cardCornerRadius="28dp"
        app:cardElevation="0dp"
        app:cardBackgroundColor="#FFFFFF"
        app:strokeWidth="0dp"
        app:layout_constraintTop_toBottomOf="@id/layoutHeader"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:paddingTop="20dp"
            android:paddingBottom="16dp"
            android:paddingStart="16dp"
            android:paddingEnd="16dp">

            <LinearLayout
                android:id="@+id/characterArea"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:orientation="horizontal"
                android:weightSum="2">

                <com.google.android.material.card.MaterialCardView
                    android:id="@+id/cardMale"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:layout_marginEnd="6dp"
                    app:cardCornerRadius="20dp"
                    app:cardElevation="0dp"
                    app:cardBackgroundColor="#F5FAF8"
                    app:strokeWidth="2dp"
                    app:strokeColor="#E0EEE9"
                    android:clickable="true"
                    android:focusable="true">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:gravity="center"
                        android:paddingTop="16dp"
                        android:paddingBottom="16dp"
                        android:paddingStart="8dp"
                        android:paddingEnd="8dp">

                        <com.google.android.material.card.MaterialCardView
                            android:layout_width="100dp"
                            android:layout_height="100dp"
                            app:cardCornerRadius="50dp"
                            app:cardElevation="0dp"
                            app:strokeWidth="0dp"
                            app:cardBackgroundColor="#E8F4F0"
                            android:layout_marginBottom="14dp">
                            <ImageView
                                android:id="@+id/imgMale"
                                android:layout_width="match_parent"
                                android:layout_height="match_parent"
                                android:src="@drawable/img_male_character"
                                android:scaleType="centerCrop"/>
                        </com.google.android.material.card.MaterialCardView>

                        <LinearLayout
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:orientation="horizontal"
                            android:gravity="center_vertical"
                            android:background="@drawable/bg_gender_card_idle"
                            android:paddingStart="12dp"
                            android:paddingEnd="14dp"
                            android:paddingTop="6dp"
                            android:paddingBottom="6dp">
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="&#9794;"
                                android:textSize="14sp"
                                android:textColor="#5B8A7D"
                                android:layout_marginEnd="6dp"/>
                            <TextView
                                android:id="@+id/txtMale"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="Pria"
                                android:textSize="16sp"
                                android:fontFamily="@font/poppins_bold"
                                android:textColor="#2D6A5A"/>
                        </LinearLayout>
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

                <com.google.android.material.card.MaterialCardView
                    android:id="@+id/cardFemale"
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:layout_marginStart="6dp"
                    app:cardCornerRadius="20dp"
                    app:cardElevation="0dp"
                    app:cardBackgroundColor="#F5FAF8"
                    app:strokeWidth="2dp"
                    app:strokeColor="#E0EEE9"
                    android:clickable="true"
                    android:focusable="true">

                    <LinearLayout
                        android:layout_width="match_parent"
                        android:layout_height="wrap_content"
                        android:orientation="vertical"
                        android:gravity="center"
                        android:paddingTop="16dp"
                        android:paddingBottom="16dp"
                        android:paddingStart="8dp"
                        android:paddingEnd="8dp">

                        <com.google.android.material.card.MaterialCardView
                            android:layout_width="100dp"
                            android:layout_height="100dp"
                            app:cardCornerRadius="50dp"
                            app:cardElevation="0dp"
                            app:strokeWidth="0dp"
                            app:cardBackgroundColor="#E8F4F0"
                            android:layout_marginBottom="14dp">
                            <ImageView
                                android:id="@+id/imgFemale"
                                android:layout_width="match_parent"
                                android:layout_height="match_parent"
                                android:src="@drawable/img_female_character"
                                android:scaleType="centerCrop"/>
                        </com.google.android.material.card.MaterialCardView>

                        <LinearLayout
                            android:layout_width="wrap_content"
                            android:layout_height="wrap_content"
                            android:orientation="horizontal"
                            android:gravity="center_vertical"
                            android:background="@drawable/bg_gender_card_idle"
                            android:paddingStart="12dp"
                            android:paddingEnd="14dp"
                            android:paddingTop="6dp"
                            android:paddingBottom="6dp">
                            <TextView
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="&#9792;"
                                android:textSize="14sp"
                                android:textColor="#5B8A7D"
                                android:layout_marginEnd="6dp"/>
                            <TextView
                                android:id="@+id/txtFemale"
                                android:layout_width="wrap_content"
                                android:layout_height="wrap_content"
                                android:text="Wanita"
                                android:textSize="16sp"
                                android:fontFamily="@font/poppins_bold"
                                android:textColor="#2D6A5A"/>
                        </LinearLayout>
                    </LinearLayout>
                </com.google.android.material.card.MaterialCardView>

            </LinearLayout>
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

    <com.google.android.material.card.MaterialCardView
        android:id="@+id/cardWawasan"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:layout_marginStart="20dp"
        android:layout_marginEnd="20dp"
        app:cardCornerRadius="18dp"
        app:cardElevation="0dp"
        app:strokeWidth="0dp"
        app:cardBackgroundColor="#E4F5EE"
        app:layout_constraintTop_toBottomOf="@id/wrapperCard"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="14dp">

            <ImageView
                android:layout_width="20dp"
                android:layout_height="20dp"
                android:src="@drawable/ic_lightbulb"
                android:layout_marginEnd="12dp"
                android:layout_marginTop="2dp"
                app:tint="#147B4D"/>

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical">

                <TextView
                    android:id="@+id/tvTipTitle"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="WAWASAN MEDIS"
                    android:textColor="#147B4D"
                    android:textSize="11sp"
                    android:fontFamily="@font/poppins_bold"
                    android:letterSpacing="0.08"
                    android:layout_marginBottom="3dp"/>

                <TextView
                    android:id="@+id/tvTip"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Gender memengaruhi akurasi perhitungan BMI karena perbedaan komposisi alami massa otot dan lemak."
                    android:textColor="#0D5C3E"
                    android:textSize="12sp"
                    android:fontFamily="@font/poppins_regular"
                    android:lineSpacingMultiplier="1.3"/>
            </LinearLayout>
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

    <LinearLayout
        android:id="@+id/layoutBottom"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:paddingStart="20dp"
        android:paddingEnd="20dp"
        android:paddingBottom="36dp"
        android:paddingTop="8dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnContinue"
            android:layout_width="match_parent"
            android:layout_height="56dp"
            android:text="Lanjutkan"
            android:textColor="#FFFFFF"
            android:textSize="16sp"
            android:fontFamily="@font/poppins_bold"
            android:textAllCaps="false"
            android:enabled="false"
            app:cornerRadius="16dp"
            app:backgroundTint="#B2DDD6"
            app:elevation="0dp"/>

        <TextView
            android:id="@+id/tvStepHint"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:text="Pilih salah satu gender terlebih dahulu"
            android:textColor="#A0B8B2"
            android:textSize="12sp"
            android:fontFamily="@font/poppins_regular"
            android:gravity="center"
            android:layout_marginTop="10dp"/>
    </LinearLayout>

</androidx.constraintlayout.widget.ConstraintLayout>'

$step2Xml = '<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#EEF7F4">

    <LinearLayout
        android:id="@+id/layoutHeader"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:background="@drawable/bg_splash_gradient"
        android:paddingStart="20dp"
        android:paddingEnd="20dp"
        android:paddingTop="48dp"
        android:paddingBottom="20dp"
        app:layout_constraintTop_toTopOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:gravity="center_vertical"
            android:layout_marginBottom="14dp">

            <ImageButton
                android:id="@+id/btnBack"
                android:layout_width="38dp"
                android:layout_height="38dp"
                android:src="@drawable/ic_arrow_back"
                android:background="@drawable/bg_circle_translucent"
                android:padding="10dp"
                app:tint="#FFFFFF"
                android:contentDescription="Kembali"
                android:layout_marginEnd="12dp"/>

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="6dp"
                android:layout_weight="1"
                android:layout_marginEnd="12dp"
                android:orientation="horizontal"
                android:background="@drawable/bg_badge_pill"
                android:backgroundTint="#33FFFFFF"
                android:clipToOutline="true">

                <View
                    android:id="@+id/progressFill"
                    android:layout_width="0dp"
                    android:layout_height="match_parent"
                    android:layout_weight="66"
                    android:background="#FFFFFF"/>

                <View
                    android:layout_width="0dp"
                    android:layout_height="match_parent"
                    android:layout_weight="34"/>
            </LinearLayout>

            <TextView
                android:id="@+id/tvStepCount"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="2 / 3"
                android:textColor="#FFFFFF"
                android:textSize="11sp"
                android:fontFamily="@font/poppins_bold"
                android:background="@drawable/bg_circle_translucent"
                android:paddingStart="10dp"
                android:paddingEnd="10dp"
                android:paddingTop="5dp"
                android:paddingBottom="5dp"/>
        </LinearLayout>

        <TextView
            android:id="@+id/tvTitle"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Isi Data Diri"
            android:textColor="#FFFFFF"
            android:textSize="20sp"
            android:fontFamily="@font/poppins_bold"/>

        <TextView
            android:id="@+id/tvDescription"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="Masukkan informasi fisik Anda untuk perhitungan yang akurat."
            android:textColor="#CCF0E4"
            android:textSize="13sp"
            android:fontFamily="@font/poppins_regular"
            android:lineSpacingMultiplier="1.25"/>
    </LinearLayout>

    <androidx.cardview.widget.CardView
        android:id="@+id/cardDataDiri"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="20dp"
        android:layout_marginEnd="20dp"
        android:layout_marginTop="16dp"
        app:cardCornerRadius="24dp"
        app:cardElevation="0dp"
        app:cardBackgroundColor="#FFFFFF"
        app:layout_constraintTop_toBottomOf="@id/layoutHeader"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:paddingTop="12dp"
            android:paddingBottom="12dp">

            <LinearLayout
                android:id="@+id/rowUsia"
                android:layout_width="match_parent"
                android:layout_height="72dp"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:paddingStart="20dp"
                android:paddingEnd="20dp"
                android:background="?attr/selectableItemBackground"
                android:clickable="true"
                android:focusable="true">

                <LinearLayout
                    android:id="@+id/iconUsia"
                    android:layout_width="44dp"
                    android:layout_height="44dp"
                    android:background="@drawable/bg_field_icon_empty"
                    android:gravity="center"
                    android:layout_marginEnd="16dp">
                    <ImageView
                        android:layout_width="20dp"
                        android:layout_height="20dp"
                        android:src="@drawable/ic_age_clock"
                        app:tint="#5A8576"/>
                </LinearLayout>

                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:orientation="vertical">
                    <TextView
                        android:id="@+id/tvLabelUsia"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="USIA"
                        android:textColor="#5A8576"
                        android:textSize="12sp"
                        android:fontFamily="@font/poppins_bold"
                        android:letterSpacing="0.08"
                        android:layout_marginBottom="2dp"/>
                    <TextView
                        android:id="@+id/tvUsiaValue"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Ketuk untuk isi"
                        android:textSize="16sp"
                        android:fontFamily="@font/poppins_regular"
                        android:textColor="#9AADA7"/>
                </LinearLayout>

                <TextView
                    android:id="@+id/unitUsia"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="tahun"
                    android:textSize="12sp"
                    android:fontFamily="@font/poppins_bold"
                    android:textColor="#5A8576"
                    android:background="@drawable/bg_unit_badge"
                    android:paddingStart="12dp"
                    android:paddingEnd="12dp"
                    android:paddingTop="6dp"
                    android:paddingBottom="6dp"
                    android:layout_marginEnd="10dp"/>

                <ImageView
                    android:layout_width="18dp"
                    android:layout_height="18dp"
                    android:src="@drawable/ic_chevron_right"
                    app:tint="#C2D0CB"/>
            </LinearLayout>

            <View
                android:layout_width="match_parent"
                android:layout_height="1dp"
                android:background="#F1F5F4"
                android:layout_marginStart="20dp"
                android:layout_marginEnd="20dp"/>

            <LinearLayout
                android:id="@+id/rowBerat"
                android:layout_width="match_parent"
                android:layout_height="72dp"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:paddingStart="20dp"
                android:paddingEnd="20dp"
                android:background="?attr/selectableItemBackground"
                android:clickable="true"
                android:focusable="true">

                <LinearLayout
                    android:id="@+id/iconBerat"
                    android:layout_width="44dp"
                    android:layout_height="44dp"
                    android:background="@drawable/bg_field_icon_empty"
                    android:gravity="center"
                    android:layout_marginEnd="16dp">
                    <ImageView
                        android:layout_width="20dp"
                        android:layout_height="20dp"
                        android:src="@drawable/ic_weight"
                        app:tint="#5A8576"/>
                </LinearLayout>

                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:orientation="vertical">
                    <TextView
                        android:id="@+id/tvLabelBerat"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="BERAT BADAN"
                        android:textColor="#5A8576"
                        android:textSize="12sp"
                        android:fontFamily="@font/poppins_bold"
                        android:letterSpacing="0.08"
                        android:layout_marginBottom="2dp"/>
                    <TextView
                        android:id="@+id/tvBeratValue"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Ketuk untuk isi"
                        android:textSize="16sp"
                        android:fontFamily="@font/poppins_regular"
                        android:textColor="#9AADA7"/>
                </LinearLayout>

                <TextView
                    android:id="@+id/unitBerat"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="kg"
                    android:textSize="12sp"
                    android:fontFamily="@font/poppins_bold"
                    android:textColor="#5A8576"
                    android:background="@drawable/bg_unit_badge"
                    android:paddingStart="12dp"
                    android:paddingEnd="12dp"
                    android:paddingTop="6dp"
                    android:paddingBottom="6dp"
                    android:layout_marginEnd="10dp"/>

                <ImageView
                    android:layout_width="18dp"
                    android:layout_height="18dp"
                    android:src="@drawable/ic_chevron_right"
                    app:tint="#C2D0CB"/>
            </LinearLayout>

            <View
                android:layout_width="match_parent"
                android:layout_height="1dp"
                android:background="#F1F5F4"
                android:layout_marginStart="20dp"
                android:layout_marginEnd="20dp"/>

            <LinearLayout
                android:id="@+id/rowTinggi"
                android:layout_width="match_parent"
                android:layout_height="72dp"
                android:orientation="horizontal"
                android:gravity="center_vertical"
                android:paddingStart="20dp"
                android:paddingEnd="20dp"
                android:background="?attr/selectableItemBackground"
                android:clickable="true"
                android:focusable="true">

                <LinearLayout
                    android:id="@+id/iconTinggi"
                    android:layout_width="44dp"
                    android:layout_height="44dp"
                    android:background="@drawable/bg_field_icon_empty"
                    android:gravity="center"
                    android:layout_marginEnd="16dp">
                    <ImageView
                        android:layout_width="20dp"
                        android:layout_height="20dp"
                        android:src="@drawable/ic_height"
                        app:tint="#5A8576"/>
                </LinearLayout>

                <LinearLayout
                    android:layout_width="0dp"
                    android:layout_height="wrap_content"
                    android:layout_weight="1"
                    android:orientation="vertical">
                    <TextView
                        android:id="@+id/tvLabelTinggi"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="TINGGI BADAN"
                        android:textColor="#5A8576"
                        android:textSize="12sp"
                        android:fontFamily="@font/poppins_bold"
                        android:letterSpacing="0.08"
                        android:layout_marginBottom="2dp"/>
                    <TextView
                        android:id="@+id/tvTinggiValue"
                        android:layout_width="wrap_content"
                        android:layout_height="wrap_content"
                        android:text="Ketuk untuk isi"
                        android:textSize="16sp"
                        android:fontFamily="@font/poppins_regular"
                        android:textColor="#9AADA7"/>
                </LinearLayout>

                <TextView
                    android:id="@+id/unitTinggi"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="cm"
                    android:textSize="12sp"
                    android:fontFamily="@font/poppins_bold"
                    android:textColor="#5A8576"
                    android:background="@drawable/bg_unit_badge"
                    android:paddingStart="12dp"
                    android:paddingEnd="12dp"
                    android:paddingTop="6dp"
                    android:paddingBottom="6dp"
                    android:layout_marginEnd="10dp"/>

                <ImageView
                    android:layout_width="18dp"
                    android:layout_height="18dp"
                    android:src="@drawable/ic_chevron_right"
                    app:tint="#C2D0CB"/>
            </LinearLayout>

        </LinearLayout>
    </androidx.cardview.widget.CardView>

    <com.google.android.material.card.MaterialCardView
        android:id="@+id/cardTips"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="20dp"
        android:layout_marginEnd="20dp"
        android:layout_marginTop="16dp"
        app:cardCornerRadius="18dp"
        app:cardElevation="0dp"
        app:strokeWidth="0dp"
        app:cardBackgroundColor="#F8F3E6"
        app:layout_constraintTop_toBottomOf="@id/cardDataDiri"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="14dp">

            <ImageView
                android:layout_width="20dp"
                android:layout_height="20dp"
                android:src="@drawable/ic_lightbulb"
                android:layout_marginEnd="12dp"
                android:layout_marginTop="2dp"
                app:tint="#D4A017"/>

            <LinearLayout
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:orientation="vertical">

                <TextView
                    android:id="@+id/tvTipTitle"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="TIPS AKURASI"
                    android:textColor="#B8860B"
                    android:textSize="11sp"
                    android:fontFamily="@font/poppins_bold"
                    android:letterSpacing="0.08"
                    android:layout_marginBottom="3dp"/>

                <TextView
                    android:id="@+id/tvTip"
                    android:layout_width="match_parent"
                    android:layout_height="wrap_content"
                    android:text="Gunakan timbangan dan meteran yang presisi untuk mendapatkan hasil BMI yang akurat."
                    android:textColor="#8B6508"
                    android:textSize="12sp"
                    android:fontFamily="@font/poppins_regular"
                    android:lineSpacingMultiplier="1.3"/>
            </LinearLayout>
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>

    <LinearLayout
        android:id="@+id/layoutBottom"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:paddingStart="20dp"
        android:paddingEnd="20dp"
        android:paddingBottom="36dp"
        android:paddingTop="12dp"
        android:background="#EEF7F4"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <com.google.android.material.button.MaterialButton
            android:id="@+id/btnCalculate"
            android:layout_width="match_parent"
            android:layout_height="56dp"
            android:text="Hitung Hasil"
            android:textColor="#FFFFFF"
            android:textSize="16sp"
            android:fontFamily="@font/poppins_bold"
            android:textAllCaps="false"
            android:enabled="false"
            app:cornerRadius="16dp"
            app:backgroundTint="#B2DDD6"
            app:elevation="0dp"/>

        <TextView
            android:id="@+id/tvBtnSubtext"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="center_horizontal"
            android:text="Lengkapi semua data terlebih dahulu"
            android:textColor="#A0B8B2"
            android:textSize="12sp"
            android:fontFamily="@font/poppins_regular"
            android:gravity="center"
            android:layout_marginTop="10dp"/>
    </LinearLayout>

</androidx.constraintlayout.widget.ConstraintLayout>'

$enc = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText((Join-Path (Get-Location) $step1Path), $step1Xml, $enc)
Write-Host "OK: $step1Path"
[System.IO.File]::WriteAllText((Join-Path (Get-Location) $step2Path), $step2Xml, $enc)
Write-Host "OK: $step2Path"
Write-Host "Selesai. Kedua file berhasil diperbarui."

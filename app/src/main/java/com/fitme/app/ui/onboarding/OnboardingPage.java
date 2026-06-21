package com.fitme.app.ui.onboarding;

public class OnboardingPage {
    public final int    iconRes;
    public final String title;
    public final String description;
    public final String bgColor;
    public final String accentColor;

    public OnboardingPage(int iconRes, String title, String description,
                          String bgColor, String accentColor) {
        this.iconRes     = iconRes;
        this.title       = title;
        this.description = description;
        this.bgColor     = bgColor;
        this.accentColor = accentColor;
    }
}
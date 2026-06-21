package com.fitme.app.ui.splash;

public class SplashState {
    public String  greetingLine1;
    public String  greetingCountry;
    public String  greetingLocation;
    public String  tagline;
    public int     flagResId;
    public boolean isLocationFound;
    public boolean isFinished;
    public String  countryCode;

    public SplashState(String line1, String country, String location,
                       String tagline, int flag, boolean found, boolean finished,
                       String countryCode) {
        this.greetingLine1    = line1;
        this.greetingCountry  = country;
        this.greetingLocation = location;
        this.tagline          = tagline;
        this.flagResId        = flag;
        this.isLocationFound  = found;
        this.isFinished       = finished;
        this.countryCode      = countryCode;
    }
}
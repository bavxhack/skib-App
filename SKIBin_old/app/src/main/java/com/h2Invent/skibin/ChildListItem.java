package com.h2Invent.skibin;

public class ChildListItem {
    private String mName;
    private String mSchule;
    private int mKlasse;
    private boolean mCheckin;
    private String mDetailUrl;
    private int mSchulId;
    private boolean mhasBirthday;
    private String mCheckinUrl;

    public ChildListItem(String name, String schule, int klasse, boolean checkin, String url, int schulId, boolean hasBirthday, String checkinUrl) {
        mName = name;
        mSchule = schule;
        mKlasse = klasse;
        mCheckin = checkin;
        mDetailUrl = url;
        mSchulId = schulId;
        mhasBirthday = hasBirthday;
        mCheckinUrl = checkinUrl;
    }

    public String getmName() {
        return mName;
    }

    public String getmSchule() {
        return mSchule;
    }

    public int getmKlasse() {
        return mKlasse;
    }

    public boolean ismCheckin() {
        return mCheckin;
    }

    public void setmCheckin(boolean mCheckin) {
        this.mCheckin = mCheckin;
    }

    public String getmDetailUrl() {
        return mDetailUrl;
    }

    public int getmSchulId() {
        return mSchulId;
    }

    public void setmSchulId(int mSchulId) {
        this.mSchulId = mSchulId;
    }

    public void setmDetailUrl(String mDetailUrl) {
        this.mDetailUrl = mDetailUrl;
    }

    public boolean isMhasBirthday() {
        return mhasBirthday;
    }

    public void setMhasBirthday(boolean mhasBirthday) {
        this.mhasBirthday = mhasBirthday;
    }

    public String getmCheckinUrl() {
        return mCheckinUrl;
    }

    public void setmCheckinUrl(String mCheckinUrl) {
        this.mCheckinUrl = mCheckinUrl;
    }
}

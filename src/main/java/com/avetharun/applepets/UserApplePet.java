package com.avetharun.applepets;

import java.util.UUID;

public class UserApplePet {
    private int variant = 0;
    private String display = "";


    public int getVariant() {
        return variant;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public void setVariant(int variant) {
        this.variant = variant;
    }
    public String RegistryUUID;
}

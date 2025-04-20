package com.example.cropmate;

public class Language {
    private String name;
    private boolean isSelected;

    public Language(String name) {
        this.name = name;
        this.isSelected = false; // Default: Not selected
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}

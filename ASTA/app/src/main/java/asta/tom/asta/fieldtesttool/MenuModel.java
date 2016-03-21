package com.example.fieldtesttool;

/**
 * A model class for menu item.
 */
public class MenuModel {

    private int m_nIcon;
    private String m_sTitle;
    private String m_sCounter;
 
    private boolean m_bIsGroupHeader = false;
    // constructor for headline:
    public MenuModel(String title) {
        this(-1,title,null);
        m_bIsGroupHeader = true;
    }

    /**
     * constructor for item with icon and counter
     * @param icon for item
     * @param title for item
     * @param counter for item (for now, not used)
     */
    public MenuModel(int icon, String title, String counter) {
        super();
        this.m_nIcon = icon;
        this.m_sTitle = title;
        this.m_sCounter = counter;
    }
    public MenuModel(int icon, String title) {
        this(icon, title, null);
    }
    //getters:
    public boolean isGroupHeader() {
        return m_bIsGroupHeader;
    }
    public int getIcon() {
        return m_nIcon;
    }
    public String getTitle() {
        return m_sTitle;
    }
    public String getCounter() {
        return m_sCounter;
    }
}

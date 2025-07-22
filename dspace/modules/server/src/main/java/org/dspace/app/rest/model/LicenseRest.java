/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.app.rest.model;

/**
 * The License text REST resource.
 *
 * @author Luigi Andrea Pascarelli (luigiandrea.pascarelli at 4science.it)
 */
public class LicenseRest implements RestModel {
    public static final String NAME = "license";
    public static final String PLURAL_NAME = "licenses";

    private boolean custom = false;
    private String text;

    // TAMU Customization - proxy license step
    private final String name;
    private final String label;
    // END TAMU Customization - proxy license step

    // TAMU Customization - proxy license step
    private LicenseRest(String name, String label, String text, boolean custom) {
        super();
        this.name = name;
        this.label = label;
        this.text = text;
        this.custom = custom;
    }
    // END TAMU Customization - proxy license step

    public boolean isCustom() {
        return custom;
    }

    public void setCustom(boolean custom) {
        this.custom = custom;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    // TAMU Customization - proxy license step
    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }
    // END TAMU Customization - proxy license step

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    public String getTypePlural() {
        return PLURAL_NAME;
    }

    // TAMU Customization - proxy license step
    public static LicenseRest of(String name, String label, String text, boolean custom) {
        return new LicenseRest(name, label, text, custom);
    }
    // END TAMU Customization - proxy license step

}

package com.zutubi.tove.annotations;

/**
 * Constants for field types.
 */
public interface FieldType
{
    static final String CHECKBOX = "checkbox";
    static final String COMBOBOX = "combobox";
    static final String CONTROLLING_CHECKBOX = "controlling-checkbox";
    static final String CONTROLLING_SELECT = "controlling-select";
    static final String DROPDOWN = "dropdown";
    static final String FILE = "file";
    /**
     * A field used to carry an internal value.
     */
    static final String HIDDEN = "hidden";
    static final String ITEM_PICKER = "itempicker";
    /**
     * A text field where the value is not echoed.
     */
    static final String PASSWORD = "password";
    static final String SELECT = "select";
    static final String SUBMIT = "submit";
    /**
     * Text field type, represents a plain string value.
     */
    static final String TEXT = "text";
    static final String TEXTAREA = "textarea";
}

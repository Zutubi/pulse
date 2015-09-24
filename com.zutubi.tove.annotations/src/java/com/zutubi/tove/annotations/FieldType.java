package com.zutubi.tove.annotations;

/**
 * Constants for field types.
 */
public interface FieldType
{
    String CHECKBOX = "checkbox";
    String COMBOBOX = "combobox";
    String CONTROLLING_CHECKBOX = "controlling-checkbox";
    String CONTROLLING_SELECT = "controlling-select";
    String DROPDOWN = "dropdown";
    String FILE = "file";
    /**
     * A field used to carry an internal value.
     */
    String HIDDEN = "hidden";
    String ITEM_PICKER = "itempicker";
    /**
     * A text field where the value is not echoed.
     */
    String PASSWORD = "password";
    String SUBMIT = "submit";
    String STRING_LIST = "stringlist";
    /**
     * Text field type, represents a plain string value.
     */
    String TEXT = "text";
    String TEXTAREA = "textarea";
}

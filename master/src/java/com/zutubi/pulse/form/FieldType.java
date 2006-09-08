package com.zutubi.pulse.form;

/**
 * <class-comment/>
 */
public interface FieldType
{
    /**
     * Text field type, represents a plain string value.
     */
    public static final String TEXT = "text";

    /**
     * Email field is similar to a text field, but adds validation to ensure that the field only accepts
     * value emails.
     */
    public static final String EMAIL = "email";

    public static final String PASSWORD = "password";

    public static final String HIDDEN = "hidden";

    public static final String URL = "url";

    public static final String FILE = "file";

    public static final String DIRECTORY = "directory";

    public static final String DATE = "date";

    public static final String INTEGER = "int";

    /**
     * A project field is one that allows you to select one of the configured projects.
     *
     */
    public static final String PROJECT = "project";    
}

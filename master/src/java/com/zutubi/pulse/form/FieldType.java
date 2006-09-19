package com.zutubi.pulse.form;

import java.util.List;

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
     *
     * Note: This type is not yet supported.
     */
    public static final String EMAIL = "email";

    /**
     *
     */
    public static final String PASSWORD = "password";

    /**
     *
     */
    public static final String HIDDEN = "hidden";

    /**
     * Note: This type is not yet supported.
     */
    public static final String URL = "url";

    /**
     * Note: This type is not yet supported.
     */
    public static final String FILE = "file";

    /**
     * Note: This type is not yet supported.
     */
    public static final String DIRECTORY = "directory";

    /**
     * Note: This type is not yet supported.
     */
    public static final String DATE = "date";

    /**
     * Note: This type is not yet supported.
     */
    public static final String INTEGER = "int";

    /**
     * A project field is one that allows you to select one of the configured projects.
     *
     * Note: This type is not yet supported.
     */
    public static final String PROJECT = "project";

    public static final String RADIO = "radio";

    public static final String TEXTAREA = "textarea";

    public static final String CHECKBOX = "checkbox";
}

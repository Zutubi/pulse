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
    static final String TEXT = "text";

    /**
     * Email field is similar to a text field, but adds validation to ensure that the field only accepts
     * value emails.
     *
     * Note: This type is not yet supported.
     */
    static final String EMAIL = "email";

    /**
     *
     */
    static final String PASSWORD = "password";

    /**
     *
     */
    static final String HIDDEN = "hidden";

    /**
     * Note: This type is not yet supported.
     */
    static final String URL = "url";

    /**
     * Note: This type is not yet supported.
     */
    static final String FILE = "file";

    /**
     * Note: This type is not yet supported.
     */
    static final String DIRECTORY = "directory";

    /**
     * Note: This type is not yet supported.
     */
    static final String DATE = "date";

    /**
     * Note: This type is not yet supported.
     */
    static final String INTEGER = "int";

    /**
     * A project field is one that allows you to select one of the configured projects.
     *
     * Note: This type is not yet supported.
     */
    static final String PROJECT = "project";

    static final String RADIO = "radio";

    static final String TEXTAREA = "textarea";

    static final String CHECKBOX = "checkbox";

    static final String SELECT = "select";
}

package com.zutubi.pulse.form.descriptor;

/**
 * The Action Descriptor defines the action to be taken when the HTML form is submitted.
 *
 */
public interface ActionDescriptor extends Descriptor
{
    /**
     * The save action requests that the submitted form data be saved to persistent storage.
     */
    public static final String SAVE = "save";

    /**
     * The cancel action requests that the submitted form data be ignored.
     */
    public static final String CANCEL = "cancel";

    /**
     * The reset action requests that the submitted form data be ignored AND that the persisted values be reset to
     * their default values.
     */
    public static final String RESET = "reset";

    /**
     * Get the action string.  The value should be one of #SAVE, #CANCEL or #RESET
     *
     * @return the action string.
     */
    String getAction();
}

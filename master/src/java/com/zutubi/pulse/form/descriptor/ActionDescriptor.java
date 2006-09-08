package com.zutubi.pulse.form.descriptor;

/**
 * <class-comment/>
 */
public interface ActionDescriptor extends Descriptor
{
    public static final String SAVE = "save";

    public static final String CANCEL = "cancel";

    public static final String RESET = "reset";

    String getAction();
}

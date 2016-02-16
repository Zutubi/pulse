package com.zutubi.tove.ui.format;

import com.zutubi.i18n.Messages;

/**
 * Interface for classes that can be wired with a {@link com.zutubi.i18n.Messages}
 * instance.
 */
public interface MessagesAware
{
    void setMessages(Messages messages);
}

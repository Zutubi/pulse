package com.zutubi.pulse.master.tove.webwork;

import com.opensymphony.util.TextUtils;
import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.tove.format.StateDisplayManager;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.tove.type.CompositeType;

import java.util.Collection;

/**
 * A layer on top of the {@link com.zutubi.pulse.master.tove.format.StateDisplayManager}
 * which returns state as rendered fragments of HTML for the web interface.
 */
public class StateDisplayRenderer
{
    private static final Messages I18N = Messages.getInstance(StateDisplayRenderer.class);
    
    private static final int COLLECTION_LIMIT = 3;

    private StateDisplayManager stateDisplayManager;

    /**
     * Calls {@link com.zutubi.pulse.master.tove.format.StateDisplayManager#format(String, com.zutubi.tove.config.api.Configuration)}
     * to format the specified field of the given instance, then renders the
     * returned result as an HTML fragment.
     *
     * @param fieldName name of the state field to render
     * @param instance  instance to render the field for
     * @return an HTML fragment suitable for inclusion in a state table value
     *         cell
     */
    public String render(String fieldName, Configuration instance)
    {
        return renderFormatted(stateDisplayManager.format(fieldName, instance));
    }

    /**
     * Calls {@link com.zutubi.pulse.master.tove.format.StateDisplayManager#formatCollection(String, com.zutubi.tove.type.CompositeType, java.util.Collection, com.zutubi.tove.config.api.Configuration)}
     * to format the specified field of the given collection, then renders the
     * returned result as an HTML fragment.
     *
     * @param fieldName      name of the state field to render
     * @param type           target type of the collection
     * @param collection     collection to render the field for
     * @param parentInstance instance that owns the collection
     * @return an HTML fragment suitable for inclusion in a state table value
     *         cell
     */
    public String renderCollection(String fieldName, CompositeType type, Collection<? extends Configuration> collection, Configuration parentInstance)
    {
        return renderFormatted(stateDisplayManager.formatCollection(fieldName, type, collection, parentInstance));
    }

    private String renderFormatted(Object formatted)
    {
        if (formatted instanceof Collection)
        {
            return renderCollection((Collection) formatted);
        }
        else
        {
            return TextUtils.htmlEncode(formatted.toString());
        }
    }

    private String renderCollection(Collection collection)
    {
        StringBuilder result = new StringBuilder();
        boolean hasExcess = collection.size() - COLLECTION_LIMIT > 1;

        result.append("<ul");
        if (hasExcess)
        {
            result.append(" onclick='toggleStateList(event);'");
        }
        result.append(">");

        int count = 0;
        for (Object o: collection)
        {
            count++;
            result.append("<li");
            if (hasExcess && count > COLLECTION_LIMIT)
            {
                result.append(" class='excess'");
            }
            result.append(">");
            result.append(TextUtils.htmlEncode(o.toString()));
            result.append("</li>");

            if (hasExcess && count == COLLECTION_LIMIT)
            {
                result.append("<li class='details expansion'/>");
                result.append(I18N.format("collection.more", collection.size() - COLLECTION_LIMIT));
                result.append("</li>");
            }
        }

        if (hasExcess)
        {
            result.append("<li class='details excess'/>");
            result.append(I18N.format("collection.all", collection.size()));
            result.append("</li>");
        }

        result.append("</ul>");

        return result.toString();
    }

    public void setStateDisplayManager(StateDisplayManager stateDisplayManager)
    {
        this.stateDisplayManager = stateDisplayManager;
    }
}

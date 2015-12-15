package com.zutubi.pulse.master.rest.model;

import com.zutubi.i18n.Messages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Models configuration state information.
 */
public class StateModel
{
    public static final String KEY_HEADING = "state.heading";
    public static final String KEY_LABEL = "label";

    private String label;
    private List<KeyValueModel> fields;

    public StateModel(Map<String, Object> state, Messages messages)
    {
        if (messages.isKeyDefined(KEY_HEADING))
        {
            label = messages.format(KEY_HEADING);
        }
        else if (messages.isKeyDefined(KEY_LABEL))
        {
            label = messages.format(KEY_LABEL) + " state";
        }
        else
        {
            label = "state";
        }

        if (state.size() > 0)
        {
            fields = new ArrayList<>(state.size());
            for (Map.Entry<String, Object> entry: state.entrySet())
            {
                Object value = entry.getValue();
                if (value instanceof Map)
                {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> nested = (Map<String, Object>) value;
                    List<KeyValueModel> nestedValue = new ArrayList<>(nested.size());
                    for (Map.Entry<String, Object> nestedEntry: nested.entrySet())
                    {
                        nestedValue.add(new KeyValueModel(nestedEntry.getKey(), nestedEntry.getValue()));
                    }

                    value = nestedValue;
                }

                fields.add(new KeyValueModel(entry.getKey(), messages.format(entry.getKey() + ".label"), value));
            }
        }
    }

    public String getLabel()
    {
        return label;
    }

    public List<KeyValueModel> getFields()
    {
        return fields;
    }
}

/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.tove.ui.model;

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

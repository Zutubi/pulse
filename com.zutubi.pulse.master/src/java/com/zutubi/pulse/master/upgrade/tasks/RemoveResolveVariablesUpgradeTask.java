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

package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.squeezer.SqueezeException;
import com.zutubi.tove.squeezer.squeezers.BooleanSqueezer;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.zutubi.pulse.master.upgrade.tasks.RecordLocators.newPathPattern;

/**
 * Removes the "resolveVariables" flag from resource properties.  This requires
 * updates to resource properties that did not have "resolve variables" enabled
 * previously and did have values that contain backslashes before special
 * characters.  Now that resolve variables is always on, even though escaping
 * is lenient, backslashes could be swallowed if they are followed by
 * backslashes or dollar signs.
 */
public class RemoveResolveVariablesUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final BooleanSqueezer BOOLEAN_SQUEEZER = new BooleanSqueezer();

    private static final String PROPERTY_RESOLVE_VARIABLES = "resolveVariables";
    private static final String PROPERTY_VALUE = "value";

    private Map<String, TemplatedScopeDetails> detailsByScope = new TreeMap<String, TemplatedScopeDetails>();

    public boolean haltOnFailure()
    {
        // Minor incompatibilities are possible, but this is not fatal.
        return false;
    }

    @Override
    protected RecordLocator getRecordLocator()
    {
        return newPathPattern(
                "agents/*/resources/*/properties/*",
                "agents/*/resources/*/versions/*/properties/*",
                "projects/*/properties/*",
                "projects/*/stages/*/properties/*",
                "projects/*/triggers/*/properties/*"
        );
    }

    @Override
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(new RecordUpgrader()
        {
            public void upgrade(String path, MutableRecord record)
            {
                String value = (String) record.get(PROPERTY_VALUE);
                // Checking the value of resolveVariables is expensive due to
                // templates, so leave it until we know it makes a difference.
                if (value != null && (value.contains("\\\\") || value.contains("$")) && resolveVariablesDisabled(path, record))
                {
                    StringBuilder escaped = new StringBuilder(value.length() * 2);
                    for (int i = 0; i < value.length(); i++)
                    {
                        char c = value.charAt(i);
                        if (c == '\\')
                        {
                            // x is just an arbitrary non-special character
                            char peek = i < value.length() - 1 ? value.charAt(i + 1) : 'x';
                            if (peek == '\\' || peek == '$')
                            {
                                escaped.append('\\');
                            }
                        }
                        else if (c == '$')
                        {
                            escaped.append('\\');
                        }

                        escaped.append(c);
                    }

                    record.put(PROPERTY_VALUE, escaped.toString());
                }
            }
        }, RecordUpgraders.newDeleteProperty(PROPERTY_RESOLVE_VARIABLES));
    }

    private TemplatedScopeDetails scopeDetailsForPath(String path)
    {
        String scope = PathUtils.getElement(path, 0);
        TemplatedScopeDetails details = detailsByScope.get(scope);
        if (details == null)
        {
            details = new TemplatedScopeDetails(scope, recordManager);
            detailsByScope.put(scope, details);
        }

        return details;
    }

    private boolean resolveVariablesDisabled(String path, Record record)
    {
        TemplatedScopeDetails details = scopeDetailsForPath(path);
        while (record != null && record.get(PROPERTY_RESOLVE_VARIABLES) == null)
        {
            path = details.getAncestorPath(path);
            record = path == null ? null : recordManager.select(path);
        }

        if (record == null)
        {
            return true;
        }
        else
        {
            try
            {
                String value = (String) record.get(PROPERTY_RESOLVE_VARIABLES);
                return !BOOLEAN_SQUEEZER.unsqueeze(value);
            }
            catch (SqueezeException e)
            {
                return true;
            }
        }
    }
}

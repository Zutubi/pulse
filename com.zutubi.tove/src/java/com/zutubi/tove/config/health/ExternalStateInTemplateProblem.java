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

package com.zutubi.tove.config.health;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

/**
 * Identifies a non-zero external state identifier in a template.  Only
 * concrete instances should refer to external state.
 */
public class ExternalStateInTemplateProblem extends HealthProblemSupport
{
    static final String NO_EXTERNAL_STATE = "0";
    
    private String key;

    /**
     * Creates a new problem marking the given key of the given record to be
     * cleared.
     * 
     * @param path    path of the record containing the value
     * @param message description of this problem
     * @param key     key of the non-zero external state property
     */
    public ExternalStateInTemplateProblem(String path, String message, String key)
    {
        super(path, message);
        this.key = key;
    }

    public void solve(RecordManager recordManager)
    {
        Record record = recordManager.select(getPath());
        if (record != null)
        {
            Object value = record.get(key);
            if (value == null || value instanceof String && !value.equals(NO_EXTERNAL_STATE))
            {
                MutableRecord mutable = record.copy(false, true);
                mutable.put(key, NO_EXTERNAL_STATE);
                recordManager.update(getPath(), mutable);
            }
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        ExternalStateInTemplateProblem that = (ExternalStateInTemplateProblem) o;

        if (!key.equals(that.key))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + key.hashCode();
        return result;
    }
}

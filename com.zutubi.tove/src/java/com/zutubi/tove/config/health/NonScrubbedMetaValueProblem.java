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
import com.zutubi.tove.type.record.RecordUtils;

/**
 * Identifies a meta value that should be scrubbed as it is identical to the
 * parent.
 */
public class NonScrubbedMetaValueProblem extends HealthProblemSupport
{
    private String key;
    private Object value;

    /**
     * Creates a new problem marking a meta value at the given key of the
     * given record that should be scrubbed.
     *
     * @param path    path of the record containing the value
     * @param message description of this problem
     * @param key     key where the value is found
     * @param value   inherited value of the property - if it is equal to this
     *                it should be removed
     */
    public NonScrubbedMetaValueProblem(String path, String message, String key, Object value)
    {
        super(path, message);
        this.key = key;
        this.value = value;
    }

    public void solve(RecordManager recordManager)
    {
        // Scrubs out the meta value by removing it from the record, after
        // double-checking it still matches the inherited value.
        Record record = recordManager.select(getPath());
        if (record != null && RecordUtils.valuesEqual(value, record.getMeta(key)))
        {
            MutableRecord mutable = record.copy(false, true);
            mutable.removeMeta(key);
            recordManager.update(getPath(), mutable);
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

        NonScrubbedMetaValueProblem that = (NonScrubbedMetaValueProblem) o;

        if (key != null ? !key.equals(that.key) : that.key != null)
        {
            return false;
        }
        if (value != null ? !value.equals(that.value) : that.value != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}

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

package com.zutubi.tove.config;

import com.zutubi.tove.type.ComplexType;
import com.zutubi.tove.type.TypeProperty;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.Record;

import java.util.ArrayList;

/**
 * Base for reference walking functions that update references in place.
 * Returns a mutable record as input.
 */
abstract class ReferenceUpdatingFunction extends ReferenceWalkingFunction
{
    public ReferenceUpdatingFunction(ComplexType type, MutableRecord record, String path)
    {
        super(type, record, path);
    }

    protected void handleReferenceList(String path, Record record, TypeProperty property, String[] value)
    {
        ArrayList<String> newValue = new ArrayList<String>(value.length);
        for (String reference : value)
        {
            String newReference = updateReference(reference);
            if (newReference != null)
            {
                newValue.add(newReference);
            }
        }

        ((MutableRecord) record).put(property.getName(), newValue.toArray(new String[newValue.size()]));
    }

    protected void handleReference(String path, Record record, TypeProperty property, String value)
    {
        MutableRecord mutableRecord = (MutableRecord) record;
        String newValue = updateReference(value);
        if (newValue == null)
        {
            mutableRecord.remove(property.getName());
        }
        else
        {
            mutableRecord.put(property.getName(), newValue);
        }
    }

    protected abstract String updateReference(String value);
}

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

import com.zutubi.tove.type.CompositeType;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.RecordManager;

/**
 * A function that walks a record tree de-canonicalising all references found.  This changes the
 * referenced handles to point to the referee in the same owner (as opposed to a canonical
 * reference which uses the handle of the template owner of the referee).
 */
public class DecanonicaliseReferencesFunction extends ReferenceUpdatingFunction
{
    private String templateOwnerPath;
    private RecordManager recordManager;
    private ConfigurationReferenceManager configurationReferenceManager;

    public DecanonicaliseReferencesFunction(CompositeType type, MutableRecord record, String path, RecordManager recordManager, ConfigurationReferenceManager configurationReferenceManager)
    {
        super(type, record, path);
        templateOwnerPath = PathUtils.getPrefix(path, 2);
        this.recordManager = recordManager;
        this.configurationReferenceManager = configurationReferenceManager;
    }

    @Override
    protected String updateReference(String value)
    {
        long handle = Long.parseLong(value);
        if (handle == 0)
        {
            return "0";
        }
        else
        {
            String referencedPath = configurationReferenceManager.getReferencedPathForHandle(templateOwnerPath, handle);
            handle = recordManager.select(referencedPath).getHandle();
            return Long.toString(handle);
        }
    }
}

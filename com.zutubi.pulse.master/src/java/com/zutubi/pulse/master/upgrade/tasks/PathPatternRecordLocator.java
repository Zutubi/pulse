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

import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.Map;
import java.util.HashMap;

/**
 * A locator that finds all records matching a set of path patterns.  Path patterns are
 * defined by the {@link com.zutubi.tove.type.record.RecordManager#selectAll(String)}
 * method.
 */
class PathPatternRecordLocator implements RecordLocator
{
    private String[] pathPatterns;

    /**
     * @param pathPatterns patterns to use for finding records, may include
     *                    wildcards
     * @see com.zutubi.tove.type.record.RecordManager#selectAll(String)
     */
    public PathPatternRecordLocator(String... pathPatterns)
    {
        this.pathPatterns = pathPatterns;
    }

    public Map<String, Record> locate(RecordManager recordManager)
    {
        Map<String, Record> result = new HashMap<String, Record>();
        for (String pathPattern : pathPatterns)
        {
            result.putAll(recordManager.selectAll(pathPattern));
        }
        return result;
    }
}

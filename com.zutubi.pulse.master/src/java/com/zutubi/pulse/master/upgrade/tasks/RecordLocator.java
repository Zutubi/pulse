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

/**
 * Interface for objects that can locate records.  To obtain instances of
 * locators, see the {@link RecordLocators} class.
 */
public interface RecordLocator
{
    /**
     * Locate and return records of interest along with their paths.
     *
     * @param recordManager record manager from which the records are found
     * @return a mapping from path to record for all located records
     */
    Map<String, Record> locate(RecordManager recordManager);
}

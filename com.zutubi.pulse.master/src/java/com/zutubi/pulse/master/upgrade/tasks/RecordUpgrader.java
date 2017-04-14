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

import com.zutubi.tove.type.record.MutableRecord;

/**
 * Interface representing a single upgrade on a single record.  Multiple such
 * upgrades may be composed and applied to multiple records in a single upgrade
 * task.  To obtain instances of upgraders, see the {@link RecordUpgraders}
 * class.
 */
public interface RecordUpgrader
{
    /**
     * Perform a simple upgrade on the given record.  Note that updates may
     * only be made to simple values directly within the record.  Changes to
     * nested records are not supported and will have an affect between none
     * and disastrous.
     *
     * @param path   path at which the record is stored
     * @param record the record to upgrade
     */
    void upgrade(String path, MutableRecord record);
}

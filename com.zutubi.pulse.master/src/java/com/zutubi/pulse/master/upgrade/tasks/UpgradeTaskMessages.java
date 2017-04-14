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

import com.zutubi.i18n.Messages;

/**
 * An I18N support class that understands the format of the properties classes
 * used by the upgrade tasks, providing convenience methods to access the I18N
 * strings.
 */
public class UpgradeTaskMessages
{
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";

    private final Messages I18N;

    public UpgradeTaskMessages(Class cls)
    {
        I18N = Messages.getInstance(cls);
    }

    public String getName()
    {
        return format(KEY_NAME);
    }

    public String getDescription()
    {
        return format(KEY_DESCRIPTION);
    }

    public String format(String name)
    {
        return I18N.format(name);
    }
}

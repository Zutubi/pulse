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

package com.zutubi.pulse.acceptance;

import com.zutubi.util.Condition;

import java.io.File;

/**
 * A condition to test for the existence of a file.
 */
public class FileExistsCondition implements Condition
{
    private File file;

    public FileExistsCondition(File file)
    {
        this.file = file;
    }

    public boolean satisfied()
    {
        return file.exists();
    }
}

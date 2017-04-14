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

package com.zutubi.pulse.core.commands.make;

import com.zutubi.pulse.core.resources.api.SimpleBinaryResourceLocator;

/**
 * Locates a make resource by finding the make binary on the path.
 */
public class MakeResourceLocator extends SimpleBinaryResourceLocator
{
    public MakeResourceLocator()
    {
        super("make");
    }
}

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

package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.WorkingCopyContext;
import com.zutubi.pulse.core.ui.api.UserInterface;
import com.zutubi.util.config.Config;

import java.io.File;

/**
 * A simple implementation of {@link com.zutubi.pulse.core.scm.api.WorkingCopyContext}.
 */
public class WorkingCopyContextImpl implements WorkingCopyContext
{
    private File base;
    private Config config;
    private UserInterface ui;

    public WorkingCopyContextImpl(File base, Config config, UserInterface ui)
    {
        this.base = base;
        this.config = config;
        this.ui = ui;
    }

    public File getBase()
    {
        return base;
    }

    public Config getConfig()
    {
        return config;
    }

    public UserInterface getUI()
    {
        return ui;
    }
}

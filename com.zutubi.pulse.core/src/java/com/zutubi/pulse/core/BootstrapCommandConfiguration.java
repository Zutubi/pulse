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

package com.zutubi.pulse.core;

import com.zutubi.pulse.core.commands.api.CommandConfigurationSupport;

/**
 */
public class BootstrapCommandConfiguration extends CommandConfigurationSupport
{
    public static final String COMMAND_NAME = "bootstrap";
    public static final String OUTPUT_NAME = "bootstrap output";
    public static final String FILES_FILE = "files.txt";

    private Bootstrapper bootstrapper;

    public BootstrapCommandConfiguration(Bootstrapper bootstrapper)
    {
        super(BootstrapCommand.class);
        this.bootstrapper = bootstrapper;
        setName(COMMAND_NAME);
    }

    public Bootstrapper getBootstrapper()
    {
        return bootstrapper;
    }
}

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

package com.zutubi.pulse.core.dependency.ivy;

import org.apache.ivy.util.AbstractMessageLogger;

/**
 * An implementation of the ivy message logger interface that ignores all log requests.
 */
public class NullMessageLogger extends AbstractMessageLogger
{
    protected void doProgress()
    {

    }

    protected void doEndProgress(String msg)
    {

    }

    public void log(String msg, int level)
    {

    }

    public void rawlog(String msg, int level)
    {

    }
}

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

package com.zutubi.pulse.core.commands.api;

/**
 * Basic interface for classes capable of capturing artifacts as part of a command
 * result.  Examples of such artifacts include built packages and HTML reports.
 */
public interface Artifact
{
    /**
     * Called to capture the output, after a command has executed.  To capture
     * the output, register it against the given context.
     *
     * @param context context in which the associated command executed, used to
     *                register captured outputs
     */
    void capture(CommandContext context);
}

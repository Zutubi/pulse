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

package com.zutubi.pulse.master.model;

/**
 * Describes why a build occurred (i.e. why the request was triggered).
 */
public interface BuildReason extends Cloneable
{
    boolean isUser();

    String getSummary();

    /**
     * @return the name of the configured trigger that fired to cause the build when there is one
     *         and it is known, null otherwise (e.g. remote API trigger, indirect trigger via
     *         dependencies)
     */
    String getTriggerName();

    Object clone() throws CloneNotSupportedException;
}

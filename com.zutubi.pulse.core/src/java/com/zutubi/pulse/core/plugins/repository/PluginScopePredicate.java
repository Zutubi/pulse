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

package com.zutubi.pulse.core.plugins.repository;

import com.google.common.base.Predicate;

/**
 * A predicate to test if plugins are in a given scope.  Note that a plugin is
 * considered part of its specified scope or any higher scope.
 */
public class PluginScopePredicate implements Predicate<PluginInfo>
{
    private PluginRepository.Scope scope;

    public PluginScopePredicate(PluginRepository.Scope scope)
    {
        this.scope = scope;
    }

    public boolean apply(PluginInfo pluginInfo)
    {
        return pluginInfo.getScope().ordinal() <= scope.ordinal();
    }
}

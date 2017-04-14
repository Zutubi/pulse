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

package com.zutubi.pulse.master.upgrade;

import java.util.List;

/**
 * The UpgradeableComponentSource is a factory for upgradeable components.
 */
public interface UpgradeableComponentSource
{
    /**
     * Returns true if this source has some components that require upgrading.
     * @return true if an upgrade is required
     */
    boolean isUpgradeRequired();

    /**
     * Indicates whether any upgradeable component in this soure requires an upgrade to move between the given builds.
     *
     * @param fromBuildNumber the original build number
     * @param toBuildNumber the target build number
     * @return true if an upgrade is required, false otherwise.
     */
    boolean isUpgradeRequired(int fromBuildNumber, int toBuildNumber);

    /**
     * Return the list of upgradeable components that require upgrading.
     * @return a list of upgrade components.
     */
    List<UpgradeableComponent> getUpgradeableComponents();
}

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
 * An upgrade component represents part of the system that can be upgraded.
 *
 * Each upgradeable component is asked whether or not an upgrade is required.  
 */
public interface UpgradeableComponent
{
    /**
     * Indicates whether or not this upgradeable component requires an upgrade.
     *
     * @return true if an upgrade is required, false otherwise.
     */
    boolean isUpgradeRequired();

    /**
     * Indicates whether or not this upgradeable component requires an upgrade to move between the given builds.
     *
     * @param fromBuildNumber the original build number
     * @param toBuildNumber the target build number
     * @return true if an upgrade is required, false otherwise.
     */
    boolean isUpgradeRequired(int fromBuildNumber, int toBuildNumber);

    /**
     * The list of configured upgrade tasks that need to be executed in order to
     * carry out the upgrade.
     *
     * @return a list of upgrade tasks, or an empty list if no upgrades are required.
     */
    List<UpgradeTask> getUpgradeTasks();

    /**
     * Callback triggered at the start of the execution of the upgrade tasks associated with this component.
     */
    void upgradeStarted();

    /**
     * Callback triggered when the execution of the upgrade tasks associated with this component are completed.
     */
    void upgradeCompleted();

    /**
     * Callback triggered when the execution of the upgrade tasks associated with this component are aborted.
     */
    void upgradeAborted();
}

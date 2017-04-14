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

package com.zutubi.pulse.servercore.services;

/**
 */
public enum UpgradeState
{
    /**
     * Not currently upgrading, or waiting for the first message from the
     * agent.
     */
    INITIAL,
    /**
     * Underway.
     */
    STARTED,
    /**
     * Downloading a new package.
     */
    DOWNLOADING,
    /**
     * Applying a new package.
     */
    APPLYING,
    /**
     * The plugins are being brought into line with the master.
     */
    SYNCHRONISING_PLUGINS,
    /**
     * Rebooting.  Expect to be able to ping me in the morning!
     */
    REBOOTING,
    /**
     * The upgrade completed without the need for a reboot.
     */
    COMPLETE,
    /**
     * Upgrade failed because of changes detected to non-upgradeable
     * components.  Manual upgrade is required.
     */
    FAILED,
    /**
     * Some unexpected error occurred during the upgrade.
     */
    ERROR
}

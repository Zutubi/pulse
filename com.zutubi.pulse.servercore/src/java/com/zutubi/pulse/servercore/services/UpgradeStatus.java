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
 * Data object that carries upgrade status information from agents back to the
 * master.
 */
public class UpgradeStatus
{
    // NOTE: These fields cannot change, as the old agent (running the upgrade)
    // code relies on thier names.  The handle in particular no longer holds a
    // handle (it is the host id as exposed via the API).
    private long handle;
    private UpgradeState state;
    private int progress;
    private String message;

    public UpgradeStatus(long hostId, UpgradeState state, int progress, String message)
    {
        this.handle = hostId;
        this.state = state;
        this.progress = progress;
        this.message = message;
    }

    public long getHandle()
    {
        return handle;
    }

    public UpgradeState getState()
    {
        return state;
    }

    public int getProgress()
    {
        return progress;
    }

    public String getMessage()
    {
        return message;
    }
}

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

import org.apache.ivy.util.Message;

import java.util.logging.Level;

/**
 * A set of utility methods to help with adapting the ivy logging system to
 * Pulse.
 */
public class IvyUtils
{
    /**
     * The character used to indicate that an ivy action has made some sort of progress.
     */
    public static final byte[] PROGRESS_CHARACTER = ".".getBytes();

    /**
     * Convert an ivy message level to a comparable java logging level.
     *
     * @param level ivy level
     * @return java logging level.
     * @see org.apache.ivy.util.Message#MSG_INFO
     * @see org.apache.ivy.util.Message#MSG_DEBUG
     * @see org.apache.ivy.util.Message#MSG_ERR
     * @see org.apache.ivy.util.Message#MSG_VERBOSE
     * @see org.apache.ivy.util.Message#MSG_WARN
     */
    public static Level toLevel(int level)
    {
        switch (level)
        {
            case Message.MSG_INFO:
                return Level.INFO;
            case Message.MSG_DEBUG:
                return Level.FINEST;
            case Message.MSG_ERR:
                return Level.SEVERE;
            case Message.MSG_VERBOSE:
                return Level.FINE;
            case Message.MSG_WARN:
                return Level.WARNING;
        }
        throw new RuntimeException("Unknown logging level: " + level);
    }

}

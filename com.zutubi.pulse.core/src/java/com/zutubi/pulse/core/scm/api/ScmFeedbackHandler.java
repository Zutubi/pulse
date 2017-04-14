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

package com.zutubi.pulse.core.scm.api;

/**
 * A callback interface for receiving information about a checkout in progress.
 */
public interface ScmFeedbackHandler
{
    /**
     * Called to report a simple free form status message.
     *
     * @param message status information about the checkout operation
     */
    void status(String message);

    /**
     * Called periodically to check if the operation is cancelled.  If this
     * method throws, the checkout operation will exit as soon as
     * possible with an error.
     *
     * @throws ScmCancelledException if the operation should be cancelled
     */
    void checkCancelled() throws ScmCancelledException;
}

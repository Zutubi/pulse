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

package com.zutubi.pulse.acceptance;

/**
 * A selenium browser factory provides the acceptance tests access
 * to selenium browser instances.  Each factory implementation is
 * responsible for the creating and cleaning up any browser instances
 * that it creates.
 */
public interface SeleniumBrowserFactory
{
    /**
     * Get a new selenium browser instance.
     *
     * @return a selenium browser instance
     */
    SeleniumBrowser newBrowser();

    /**
     * Cleanup any browser instances that have been returned by
     * calls to {@link #newBrowser()} since the last time cleanup
     * was called.
     *
     * Cleanup should be called whenever the client of this factory
     * no longer requires the browser instances.
     */
    void cleanup();

    /**
     * Stop should be called when the client of this factory no longer
     * needs it.  If any browsers have not been cleaned up, they will
     * also be cleaned up {@link #cleanup()}.
     */
    void stop();
}

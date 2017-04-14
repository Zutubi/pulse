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

package com.zutubi.pulse.master.security;

import java.util.concurrent.ThreadFactory;

/**
 * A factory used to create system threads that have the privilege to do as
 * they please.
 */
public class PulseThreadFactory implements ThreadFactory
{
    public Thread newThread(Runnable r)
    {
        return new Thread(new DelegatingRunnable(r));
    }

    public Thread newThread(Runnable r, String name)
    {
        return new Thread(new DelegatingRunnable(r), name);
    }

    private class DelegatingRunnable implements Runnable
    {
        private Runnable delegate;

        public DelegatingRunnable(Runnable delegate)
        {
            this.delegate = delegate;
        }

        public void run()
        {
            SecurityUtils.loginAsSystem();
            delegate.run();
        }
    }
}

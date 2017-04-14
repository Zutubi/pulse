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

import org.apache.ivy.util.url.CredentialsStore;

import java.util.concurrent.Callable;

/**
 * This is a helper class that handles setting up the authentication credentials
 * for ivy related actions that require remote server authorisation.
 */
public class AuthenticatedAction
{
    public static final String USER = "pulse";
    public static final String REALM = "Pulse";

    public static synchronized <T> T execute(String host, String password, Callable<T> function) throws Exception
    {
        CredentialsStore.INSTANCE.addCredentials(REALM, host, USER, password);
        try
        {
            return function.call();
        }
        finally
        {
            CredentialsStore.INSTANCE.addCredentials(REALM, host, USER, "");
        }
    }
}

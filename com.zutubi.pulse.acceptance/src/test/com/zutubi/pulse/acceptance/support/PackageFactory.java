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

package com.zutubi.pulse.acceptance.support;

import java.io.Closeable;
import java.io.File;

/**
 * A factory for PulsePackage instances.
 */
public interface PackageFactory extends Closeable
{
    /**
     * Create a new PulsePackage instance from the pulse distribution file.
     *
     * @param pkg   a pulse package.
     * @return  a handle to a Pulse package implementation.
     */
    public PulsePackage createPackage(File pkg);
}

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

import java.io.File;

/**
 * Abstraction over the Python test code for access via Jython.
 */
public interface PulseTestFactory
{
    /**
     * Creates and returns a handle to a Python PulsePackage instance.  These
     * instances represent built Pulse packages that may be extracted and used
     * to run Pulse.
     * 
     * @param pkg the package file
     * @return a handle to a Python PulsePackage instance
     */
    public PulsePackage createPackage(File pkg);

    /**
     * Creates and returns a handle to a Python Pulse instance.  This instances
     * represent an unpacked Pulse installation and may be used to start and
     * stop Pulse.
     * 
     * @param pulseHome path of the pulse home directory to point at
     * @return a handle to a Python Pulse instance
     */
    public Pulse createPulse(String pulseHome);
}

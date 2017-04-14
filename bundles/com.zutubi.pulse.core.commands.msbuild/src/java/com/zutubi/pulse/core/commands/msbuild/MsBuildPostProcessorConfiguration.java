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

package com.zutubi.pulse.core.commands.msbuild;

import com.zutubi.pulse.core.commands.core.RegexPostProcessorConfiguration;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A pre-canned configuration for a regex post-processor for msbuild output.
 * Attempts to capture features from MsBuild itself (e.g. "Build failed") \
 * and from tools it commonly invokes (such as the C# compiler).
 */
@SymbolicName("zutubi.msbuildPostProcessorConfig")
public class MsBuildPostProcessorConfiguration extends RegexPostProcessorConfiguration
{
    public MsBuildPostProcessorConfiguration()
    {
        // MsBuild's own build failed messages:

        // C:\Program Files\Microsoft Visual Studio 9.0\VC>msbuild
        // Microsoft (R) Build Engine Version 3.5.21022.8
        // [Microsoft .NET Framework, Version 2.0.50727.1434]
        // Copyright (C) Microsoft Corporation 2007. All rights reserved.
        //
        // MSBUILD : error MSB1003: Specify a project or solution file. The current working
        // directory does not contain a project or solution file.

        // C:\Program Files\Microsoft Visual Studio 9.0\VC>msbuild nosuchfile
        // Microsoft (R) Build Engine Version 3.5.21022.8
        // [Microsoft .NET Framework, Version 2.0.50727.1434]
        // Copyright (C) Microsoft Corporation 2007. All rights reserved.
        //
        // MSBUILD : error MSB1009: Project file does not exist.
        // Switch: nosuchfile

        // $ MSBuild.exe trivial.xml /t:nosuchtarget
        // Microsoft (R) Build Engine Version 3.5.21022.8
        // [Microsoft .NET Framework, Version 2.0.50727.1434]
        // Copyright (C) Microsoft Corporation 2007. All rights reserved.
        //
        // Build started 27/09/2008 2:58:34 PM.
        // Project "C:\tools\trivial.xml" on node 0 (nosuchtarget target(s)).
        // C:\tools\trivial.xml : error MSB4057: The target "nosuchtarget" does not exist in the project.
        // Done Building Project "C:\tools\trivial.xml" (nosuchtarget target(s)) -- FAILED.
        //
        //
        // Build FAILED.
        //
        //  "C:\tools\trivial.xml" (nosuchtarget target) (1) ->
        //   C:\tools\trivial.xml : error MSB4057: The target "nosuchtarget" does not exist in the project.
        //
        //     0 Warning(s)
        //     1 Error(s)
        //
        // Time Elapsed 00:00:00.01

        addErrorRegexes("^Build FAILED");

        // Tool error messages follow a similar format, examples include:
        //
        // MSBUILD : error MSB1009: Project file does not exist.
        // trivial.xml : error MSB4057: The target "nosuchtarget" does not exist in the project.
        // CSC : error CS2001: Source file 'Test.cs' could not be found
        // CSC : fatal error CS2008: No inputs specified
        // Test.cs(5,16): error CS0103: The name 'i' does not exist in the current context
        //
        // The format appears to be:
        // <tool/file> : [fatal] error <code>: <message>
        // The file name and location are given if the message can be pinned to
        // a file, otherwise the tool name is used as a prefix.
        addErrorRegexes("^\\w.*:( fatal)? error [A-Z]*[0-9]*:");

        // Tool warning messages have a similar format, with "warning" in place
        // of "error".
        addWarningRegexes("^\\w.*: warning [A-Z]*[0-9]*:");

        // Use the exit code to determine the command result.
        setFailOnError(false);
    }

    public MsBuildPostProcessorConfiguration(String name)
    {
        this();
        setName(name);
    }
}

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

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

public class PostProcessCommandAcceptanceTest extends DevToolsTestBase
{
    private static final String INPUT_FILE_NAME = "in.txt";
    private static final String PULSE_FILE_NAME = "pulse.xml";

    private static final String INPUT = "this line is ok\n" +
            "ERROR: this line is not\n";
    
    private static final String PULSE_FILE = "<?xml version=\"1.0\"?>\n" +
            "<project>\n" +
            "  <regex.pp name=\"errors.pp\">" +
            "    <pattern category=\"error\" expression=\"ERROR\"/>\n" +
            "  </regex.pp>\n" +
            "</project>\n";
    
    private static final String PROCESSOR_NAME = "errors.pp";
    private static final String OUTPUT_FOUND_FEATURE = "Found 1 feature";


    public void testPluginSyncOnInstall() throws Exception
    {
        createInputAndPulseFiles();

        String output = runCommandWithInput(getServerSetupInputLines(), COMMAND_PROCESS, PROCESSOR_NAME, INPUT_FILE_NAME);
        assertThat(output, containsString(PROMPT_NO_PLUGINS));
        assertThat(output, containsString(OUTPUT_SYNC_COMPLETE));
        assertThat(output, containsString(OUTPUT_FOUND_FEATURE));
    }

    public void testNoSyncRequiredWhenPluginsInstalled() throws Exception
    {
        runPluginSync();
        createInputAndPulseFiles();
        
        String output = runCommand(COMMAND_PROCESS, PROCESSOR_NAME, INPUT_FILE_NAME);
        assertThat(output, not(containsString(PROMPT_NO_PLUGINS)));
        assertThat(output, not(containsString(OUTPUT_SYNC_COMPLETE)));
        assertThat(output, containsString(OUTPUT_FOUND_FEATURE));
    }

    private void createInputAndPulseFiles() throws IOException
    {
        File inputFile = new File(tmpDir, INPUT_FILE_NAME);
        Files.write(INPUT, inputFile, Charset.defaultCharset());
        File pulseFile = new File(tmpDir, PULSE_FILE_NAME);
        Files.write(PULSE_FILE, pulseFile, Charset.defaultCharset());
    }
}

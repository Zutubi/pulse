package com.zutubi.pulse.acceptance;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

public class ExpandCommandAcceptanceTest extends DevToolsTestBase
{
    private static final String INPUT_FILE_NAME = "in.xml";
    
    private static final String INPUT_PULSE_FILE = "<?xml version=\"1.0\"?>\n" +
            "<project>\n" +
            "  <property name=\"ant.name\" value=\"Atom\"/>" +
            "  <recipe name=\"default\">\n" +
            "    <ant name=\"$(ant.name)\"/>\n" +
            "  </recipe>\n" +
            "</project>\n";
    
    private static final String EXPANDED_PULSE_FILE_FRAGMENT = "<ant name=\"Atom\"/>";

    
    public void testPluginSyncOnInstall() throws Exception
    {
        createInputPulseFile();

        String output = runCommandWithInput(getServerSetupInputLines(), COMMAND_EXPAND, INPUT_FILE_NAME);
        assertThat(output, containsString(PROMPT_NO_PLUGINS));
        assertThat(output, containsString(OUTPUT_SYNC_COMPLETE));
        assertThat(output, containsString(EXPANDED_PULSE_FILE_FRAGMENT));
    }

    public void testNoSyncRequiredWhenPluginsInstalled() throws Exception
    {
        runPluginSync();
        createInputPulseFile();

        String output = runCommand(COMMAND_EXPAND, INPUT_FILE_NAME);
        assertThat(output, not(containsString(PROMPT_NO_PLUGINS)));
        assertThat(output, not(containsString(OUTPUT_SYNC_COMPLETE)));
        assertThat(output, containsString(EXPANDED_PULSE_FILE_FRAGMENT));
    }

    private void createInputPulseFile() throws IOException
    {
        File pulseFile = new File(tmpDir, INPUT_FILE_NAME);
        Files.write(INPUT_PULSE_FILE, pulseFile, Charset.defaultCharset());
    }
}

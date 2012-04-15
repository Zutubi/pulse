package com.zutubi.pulse.acceptance;

import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;

import java.io.File;
import java.io.IOException;

import static com.zutubi.pulse.core.commands.api.OutputProducingCommandSupport.OUTPUT_FILE;
import static com.zutubi.pulse.core.commands.api.OutputProducingCommandSupport.OUTPUT_NAME;
import static com.zutubi.pulse.dev.local.LocalBuildOptions.DEFAULT_OUTPUT_DIRECTORY;
import static com.zutubi.pulse.dev.local.LocalBuildOptions.DEFAULT_PULSE_FILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

public class LocalBuildAcceptanceTest extends DevToolsTestBase
{
    private static final String TRIVIAL_PULSE_FILE = "<?xml version=\"1.0\"?>\n" +
            "<project default-recipe=\"default\">\n" +
            "  <recipe name=\"default\">\n" +
            "    <print name=\"hello\" message=\"Hello, Pulse!\"/>\n" +
            "  </recipe>\n" +
            "</project>\n";

    
    public void testPluginSyncOnInstall() throws Exception
    {
        createTrivialPulseFile();

        String output = runCommandWithInput(getServerSetupInputLines(), COMMAND_LOCAL);
        assertThat(output, containsString(PROMPT_NO_PLUGINS));
        assertThat(output, containsString(OUTPUT_SYNC_COMPLETE));
        assertThat(output, containsString("[hello]"));

        assertTrivialCommandOutput();
    }

    public void testNoSyncRequiredWhenPluginsInstalled() throws Exception
    {
        runPluginSync();
        createTrivialPulseFile();

        String output = runCommand(COMMAND_LOCAL);
        assertThat(output, not(containsString(PROMPT_NO_PLUGINS)));
        assertThat(output, not(containsString(OUTPUT_SYNC_COMPLETE)));
        assertThat(output, containsString("[hello]"));

        assertTrivialCommandOutput();
    }

    private void createTrivialPulseFile() throws IOException
    {
        File pulseFile = new File(tmpDir, DEFAULT_PULSE_FILE);
        FileSystemUtils.createFile(pulseFile, TRIVIAL_PULSE_FILE);
    }

    private void assertTrivialCommandOutput() throws IOException
    {
        File commandOutput = new File(tmpDir, FileSystemUtils.composeFilename(DEFAULT_OUTPUT_DIRECTORY, "00000001-hello", OUTPUT_NAME, OUTPUT_FILE));
        assertTrue(commandOutput.exists());
        String output = normaliseLineEndings(IOUtils.fileToString(commandOutput));
        assertEquals("Hello, Pulse!\n", output);
    }

    private String normaliseLineEndings(String s)
    {
        return s.replaceAll("\\r\\n", "\n");
    }

}

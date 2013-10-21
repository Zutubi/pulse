package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.dev.ui.ConsoleUI;
import com.zutubi.pulse.dev.util.AbstractDevTestCase;
import org.apache.commons.cli.ParseException;

import java.io.IOException;

import static com.zutubi.util.CollectionUtils.asPair;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

public class PersonalBuildClientFactoryTest extends AbstractDevTestCase
{
    private PersonalBuildClientFactory factory = new PersonalBuildClientFactory();
    
    public void testStandardOptions() throws ParseException
    {
        PersonalBuildClient client = factory.newInstance(
                "-r", "test.project",
                "-s", "test.server",
                "-u", "test.user",
                "-p", "test.password",
                "-f", "test.file",
                "-e", "test.revision",
                "-a", "test.reason"
        );

        assertStandardOptions(client);
    }

    public void testStandardLongOptions() throws ParseException
    {
        PersonalBuildClient client = factory.newInstance(
                "--project", "test.project",
                "--server", "test.server",
                "--user", "test.user",
                "--password", "test.password",
                "--file", "test.file",
                "--revision", "test.revision",
                "--reason", "test.reason"
        );

        assertStandardOptions(client);
    }

    private void assertStandardOptions(PersonalBuildClient client)
    {
        assertProperties(client.getConfig(),
                asPair(PersonalBuildConfig.PROPERTY_PROJECT, "test.project"),
                asPair(PersonalBuildConfig.PROPERTY_PULSE_URL, "test.server"),
                asPair(PersonalBuildConfig.PROPERTY_PULSE_USER, "test.user"),
                asPair(PersonalBuildConfig.PROPERTY_PULSE_PASSWORD, "test.password"),
                asPair(PersonalBuildConfig.PROPERTY_PATCH_FILE, "test.file"),
                asPair(PersonalBuildConfig.PROPERTY_REVISION, "test.revision"),
                asPair(PersonalBuildConfig.PROPERTY_REASON, "test.reason")
        );
    }

    public void testBaseDirectory() throws ParseException
    {
        PersonalBuildClient client = factory.newInstance("-b", baseDir.getAbsolutePath());
        assertEquals(baseDir, client.getConfig().getBase());
    }

    public void testInvalidBaseDirectory() throws ParseException
    {
        try
        {
            factory.newInstance("-b", "nosuchdir");
            fail("Base dir should be validated");
        }
        catch (ParseException e)
        {
            assertThat(e.getMessage(), containsString("Base directory specified 'nosuchdir' is not a directory"));
        }
    }

    public void testFiles() throws ParseException
    {
        PersonalBuildClient client = factory.newInstance("-v", "file1", "file2");
        assertEquals(asList("file1", "file2"), asList(client.getConfig().getFiles()));
    }

    public void testVerbosity() throws ParseException
    {
        assertVerbosity(ConsoleUI.Verbosity.NORMAL);
        assertVerbosity(ConsoleUI.Verbosity.QUIET, "-q");
        assertVerbosity(ConsoleUI.Verbosity.VERBOSE, "-v");
    }

    private void assertVerbosity(ConsoleUI.Verbosity expectedVerbosity, String... args) throws ParseException
    {
        PersonalBuildClient client = factory.newInstance(args);
        ConsoleUI ui = (ConsoleUI) client.getUI();
        assertEquals(expectedVerbosity, ui.getVerbosity());
    }

    public void testDefine() throws ParseException
    {
        PersonalBuildClient client = factory.newInstance("-d", "foo=bar");
        assertProperties(client.getConfig(), asPair("foo", "bar"));
    }

    public void testMultipleDefines() throws ParseException
    {
        PersonalBuildClient client = factory.newInstance("-d", "foo=bar", "-d", "baz=quux");
        assertProperties(client.getConfig(), asPair("foo", "bar"), asPair("baz", "quux"));
    }

    public void testRequestOption() throws ParseException
    {
        assertOption(PersonalBuildConfig.PROPERTY_SEND_REQUEST, null);
        assertOption(PersonalBuildConfig.PROPERTY_SEND_REQUEST, true, "--send-request");
        assertOption(PersonalBuildConfig.PROPERTY_SEND_REQUEST, false, "--no-send-request");
    }

    public void testUpdateOption() throws ParseException
    {
        assertOption(PersonalBuildConfig.PROPERTY_UPDATE, null);
        assertOption(PersonalBuildConfig.PROPERTY_UPDATE, true, "--update");
        assertOption(PersonalBuildConfig.PROPERTY_UPDATE, false, "--no-update");
    }

    private void assertOption(String name, Object expectedValue, String... args) throws ParseException
    {
        PersonalBuildConfig config = factory.newInstance(args).getConfig();
        if (expectedValue == null)
        {
            assertFalse(config.hasProperty(name));
        }
        else
        {
            assertEquals(expectedValue.toString(), config.getProperty(name));
        }
    }

    public void testPrecedence() throws ParseException, IOException
    {
        createProperties(userHomeDir, asPair(PersonalBuildConfig.PROPERTY_PROJECT, "infile"));
        assertOption(PersonalBuildConfig.PROPERTY_PROJECT, "infile");
        assertOption(PersonalBuildConfig.PROPERTY_PROJECT, "inargs", "-r", "inargs");
    }

    public void testInvalidOption() throws ParseException
    {
        try
        {
            factory.newInstance("--nosuchoption");
            fail("Shouldn't parse invalid option");
        }
        catch (ParseException e)
        {
            assertThat(e.getMessage(), containsString("Unrecognized option: --nosuchoption"));
        }
    }

    public void testMissingOptionArg() throws ParseException
    {
        try
        {
            factory.newInstance("-r", "-s", "http://foo");
            fail("Shouldn't allow no project name");
        }
        catch (ParseException e)
        {
            assertThat(e.getMessage(), containsString("no argument for:r"));
        }
    }

    public void testOptionAfterArgs() throws ParseException
    {
        PersonalBuildClient client = factory.newInstance("file1", "-r", "test.project");
        PersonalBuildConfig config = client.getConfig();
        assertEquals(asList("file1"), asList(config.getFiles()));
        assertEquals("test.project", config.getProject());
    }

    public void testOverride() throws ParseException
    {
        PersonalBuildClient client = factory.newInstance("-o", "foo=bar");
        assertProperties(client.getConfig().getOverrides(), asPair("foo", "bar"));
    }

    public void testMultipleOverrides() throws ParseException
    {
        PersonalBuildClient client = factory.newInstance("-o", "foo=bar", "--override", "baz=quux");
        assertProperties(client.getConfig().getOverrides(), asPair("foo", "bar"), asPair("baz", "quux"));
    }
}

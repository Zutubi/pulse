package com.zutubi.pulse.dev.personal;

import com.zutubi.pulse.core.scm.api.MenuChoice;
import com.zutubi.pulse.core.scm.api.MenuOption;
import com.zutubi.pulse.core.scm.api.YesNoResponse;
import com.zutubi.pulse.core.test.api.PulseTestCase;

import java.io.IOException;
import static java.util.Arrays.asList;
import java.util.LinkedList;
import java.util.List;

public class ConsoleUITest extends PulseTestCase
{
    private static final String TEST_QUESTION = "Test question";

    private static final String FULL_YES_NO_PROMPT = "Yes/No/Always/nEver [default: Yes]> ";
    private static final String FULL_YES_NO_OUTPUT = TEST_QUESTION + "\n" + FULL_YES_NO_PROMPT;

    private static final String MENU_PROMPT = "Choose a number (append '!' to save this selection) [default: 2]> ";
    private static final String FULL_MENU_PROMPT = TEST_QUESTION + ":\n" +
            "  1) option a\n" +
            "  2) option b\n" +
            MENU_PROMPT;
    private static final String MENU_VALUE_1 = "a";
    private static final String MENU_VALUE_2 = "b";

    private ConsoleUI ui;
    private TestConsole console;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        ui = new ConsoleUI();
        console = new TestConsole();
        ui.setConsole(console);
    }

    public void testYesNoPrompt()
    {
        console.addInputs(MENU_VALUE_1);
        YesNoResponse response = ui.yesNoPrompt(TEST_QUESTION, true, true, YesNoResponse.YES);
        assertEquals(FULL_YES_NO_OUTPUT, console.consumeOutput());
        assertEquals(YesNoResponse.ALWAYS, response);
    }

    public void testYesNoPromptDefault()
    {
        console.addInputs("");
        YesNoResponse response = ui.yesNoPrompt(TEST_QUESTION, true, true, YesNoResponse.YES);
        assertEquals(FULL_YES_NO_OUTPUT, console.consumeOutput());
        assertEquals(YesNoResponse.YES, response);
    }

    public void testYesNoPromptInvalidResponse()
    {
        console.addInputs("hello", "never");
        YesNoResponse response = ui.yesNoPrompt(TEST_QUESTION, true, true, YesNoResponse.YES);
        // We see the prompt again after the invalid input
        assertEquals(FULL_YES_NO_OUTPUT + FULL_YES_NO_PROMPT, console.consumeOutput());
        assertEquals(YesNoResponse.NEVER, response);
    }

    public void testYesNoPromptUnavailableResponse()
    {
        console.addInputs("always", "never");
        YesNoResponse response = ui.yesNoPrompt(TEST_QUESTION, false, true, YesNoResponse.NO);
        assertEquals(TEST_QUESTION + "\nYes/No/nEver [default: No]> Yes/No/nEver [default: No]> ", console.consumeOutput());
        assertEquals(YesNoResponse.NEVER, response);
    }

    public void testMenuPrompt()
    {
        simpleMenuPromptTest("1", new MenuChoice<String>(MENU_VALUE_1, false));
    }

    public void testMenuPromptDefault()
    {
        simpleMenuPromptTest("", new MenuChoice<String>(MENU_VALUE_2, false));
    }

    public void testMenuPromptPersist()
    {
        simpleMenuPromptTest("1!", new MenuChoice<String>(MENU_VALUE_1, true));
    }

    public void testMenuPromptPersistDefault()
    {
        simpleMenuPromptTest("!", new MenuChoice<String>(MENU_VALUE_2, true));
    }

    private void simpleMenuPromptTest(String input, MenuChoice<String> expectedChoice)
    {
        console.addInputs(input);
        MenuChoice<String> choice = ui.menuPrompt(TEST_QUESTION, asList(new MenuOption<String>(MENU_VALUE_1, "option a", false), new MenuOption<String>(MENU_VALUE_2, "option b", true)));
        assertEquals(FULL_MENU_PROMPT, console.consumeOutput());
        assertEquals(expectedChoice, choice);
    }

    public void testMenuPromptNotANumber()
    {
        invalidMenuPromptTest("i'm no number");
    }

    public void testMenuPromptZero()
    {
        invalidMenuPromptTest("0");
    }

    public void testMenuPromptNegative()
    {
        invalidMenuPromptTest("-8");
    }

    public void testMenuPromptOnePast()
    {
        invalidMenuPromptTest("3");
    }

    public void testMenuPromptOneHuge()
    {
        invalidMenuPromptTest("999999");
    }

    private void invalidMenuPromptTest(String input)
    {
        // Give a second input that just chooses the default.
        console.addInputs(input, "");
        MenuChoice<String> choice = ui.menuPrompt(TEST_QUESTION, asList(new MenuOption<String>(MENU_VALUE_1, "option a", false), new MenuOption<String>(MENU_VALUE_2, "option b", true)));
        // We see the prompt again after the invalid input
        assertEquals(FULL_MENU_PROMPT + MENU_PROMPT, console.consumeOutput());
        assertEquals(new MenuChoice<String>(MENU_VALUE_2, false), choice);
    }

    private static class TestConsole implements Console
    {
        private List<String> inputs = new LinkedList<String>();
        private List<String> passwords = new LinkedList<String>();
        private String output = "";
        private String error = "";

        public void addInputs(String... s)
        {
            inputs.addAll(asList(s));
        }

        public void addPasswords(String... s)
        {
            passwords.addAll(asList(s));
        }

        public String consumeOutput()
        {
            String o = output;
            output = "";
            return o;
        }

        public String consumeError()
        {
            String e = error;
            error = "";
            return e;
        }

        public String readInputLine() throws IOException
        {
            if (inputs.size() == 0)
            {
                throw new RuntimeException("No more input to read");
            }
            return inputs.remove(0);
        }

        public String readPassword(String prompt, boolean echo)
        {
            if (passwords.size() == 0)
            {
                throw new RuntimeException("No more passwords to read");
            }
            return passwords.remove(0);
        }

        public void printOutput(String output)
        {
            this.output += output;
        }

        public void printOutputLine(String output)
        {
            printOutput(output + "\n");
        }

        public void printError(String error)
        {
            this.error += error;
        }

        public void printErrorLine(String error)
        {
            printError(error + "\n");
        }
    }
}

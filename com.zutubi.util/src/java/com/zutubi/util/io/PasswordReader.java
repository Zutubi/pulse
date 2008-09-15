package com.zutubi.util.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 */
public class PasswordReader
{
    private static final String ECHO_PROPERTY = "pulse.echo.passwords";

    private BufferedReader reader;
    private Object console;
    private Method readPasswordMethod;

    public PasswordReader(BufferedReader reader)
    {
        this.reader = reader;

        try
        {
            Method getConsoleMethod = System.class.getMethod("console");
            console = getConsoleMethod.invoke(null);
            Class consoleClass = Class.forName("java.io.Console");
            readPasswordMethod = consoleClass.getMethod("readPassword", String.class, Object[].class);
        }
        catch (Exception e)
        {
            // OK, use dodgy masking
        }
    }

    public String readPassword(String prompt)
    {
        if (echo())
        {
            System.out.print(prompt);
            try
            {
                return reader.readLine();
            }
            catch (IOException e)
            {
                return null;
            }
        }
        {
            if (readPasswordMethod == null)
            {
                return maskedPassword(prompt);
            }
            else
            {
                try
                {
                    char[] password = (char[]) readPasswordMethod.invoke(console, prompt, new Object[0]);
                    return new String(password);
                }
                catch (IllegalAccessException e)
                {
                    return null;
                }
                catch (InvocationTargetException e)
                {
                    return null;
                }
            }
        }
    }

    private boolean echo()
    {
        String property = System.getProperty(ECHO_PROPERTY);
        return property != null && Boolean.valueOf(property);
    }

    private String maskedPassword(String prompt)
    {
        EraserThread eraserThread = new EraserThread(prompt);

        try
        {
            Thread t = new Thread(eraserThread);
            t.start();
            return reader.readLine();
        }
        catch (IOException e)
        {
            return null;
        }
        finally
        {
            eraserThread.stopMasking();
        }
    }

    private class EraserThread implements Runnable
    {
        private boolean run;

        /**
         * @param prompt the prompt displayed to the user
         */
        public EraserThread(String prompt)
        {
            System.out.print(prompt);
        }

        public void run()
        {
            run = true;
            while (run)
            {
                System.out.print("\b ");
                try
                {
                    Thread.sleep(1);
                }
                catch (InterruptedException ie)
                {
                    // Ignore
                }
            }
        }

        public void stopMasking()
        {
            this.run = false;
        }
    }
}

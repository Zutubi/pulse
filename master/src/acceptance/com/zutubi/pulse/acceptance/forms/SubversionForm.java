/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.acceptance.forms;

import net.sourceforge.jwebunit.WebTester;

/**
 * The Subversion scm forms. Both create and edit have common field names,
 * and therefore have been layed out in this manor.
 */
public abstract class SubversionForm extends BaseForm
{
    public SubversionForm(WebTester tester)
    {
        super(tester);
    }

    public String[] getFieldNames()
    {
        return new String[]{"svn.username", "svn.password", "svn.url", "svn.keyfile", "svn.passphrase"};
    }

    /**
     * The create cvs form.
     */
    public static class Create extends SubversionForm
    {
        public Create(WebTester tester)
        {
            super(tester);
        }

        public String getFormName()
        {
            return "svn.setup";
        }
    }

    /**
     * The edit cvs form.
     */
    public static class Edit extends SubversionForm
    {
        public Edit(WebTester tester)
        {
            super(tester);
        }

        public String getFormName()
        {
            return "svn.edit";
        }
    }
}
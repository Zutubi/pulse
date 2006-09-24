package com.zutubi.pulse.form.ui.renderers;

import freemarker.template.TemplateTransformModel;

import java.io.Writer;
import java.io.IOException;
import java.util.Map;

/**
 * <class-comment/>
 */
public class I18NTransform implements TemplateTransformModel
{


    public Writer getWriter(Writer out, Map args)
    {
        return new I18NWriter(out);
    }

    private class I18NWriter extends Writer
    {
        private Writer out;

        I18NWriter(Writer out)
        {
            this.out = out;
        }

        public void write(char[] cbuf, int off, int len) throws IOException
        {
            // get the key.
            String i18nKey = new String(cbuf, off, len);

            // look it up.
            String message = i18nKey;
            
            out.write(message);
        }

        public void flush() throws IOException
        {
            out.flush();
        }

        public void close()
        {
        }
    }
}
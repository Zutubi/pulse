package freemarker.core;

import freemarker.template.TemplateModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateMethodModel;
import freemarker.template.SimpleScalar;

import java.util.Arrays;

/**
 *
 *
 */
public class DelegateBuiltin extends StringBuiltins.StringBuiltIn
{
    private final String delegateKey;

    public DelegateBuiltin(String delegateKey)
    {
        this.delegateKey = delegateKey;
    }

    TemplateModel calculateResult(String s, Environment env) throws TemplateException
    {
        // delegate if possible.
        TemplateMethodModel method = (TemplateMethodModel)env.__getitem__(delegateKey);
        if (method != null)
        {
            return (TemplateModel) method.exec(Arrays.asList(s));
        }

        return new SimpleScalar(s);
    }

    public static void conditionalRegistration(String builtinKey, String methodKey)
    {
        if (!BuiltIn.builtins.containsKey(builtinKey))
        {
            BuiltIn.builtins.put(builtinKey, new DelegateBuiltin(methodKey));
        }
    }
}

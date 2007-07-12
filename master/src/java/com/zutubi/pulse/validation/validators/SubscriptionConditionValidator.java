package com.zutubi.pulse.validation.validators;

import antlr.MismatchedTokenException;
import antlr.NoViableAltException;
import antlr.collections.AST;
import com.zutubi.pulse.condition.NotifyCondition;
import com.zutubi.pulse.condition.NotifyConditionFactory;
import com.zutubi.pulse.condition.antlr.NotifyConditionLexer;
import com.zutubi.pulse.condition.antlr.NotifyConditionParser;
import com.zutubi.pulse.condition.antlr.NotifyConditionTreeParser;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.FieldValidatorSupport;

import java.io.StringReader;

/**
 * Validates custom project subscription condition expressions by parsing
 * them.
 */
public class SubscriptionConditionValidator extends FieldValidatorSupport
{
    private NotifyConditionFactory notifyConditionFactory;

    public void validate(Object object) throws ValidationException
    {
        Object obj = getFieldValue(getFieldName(), object);
        if(validateCondition((String) obj) == null)
        {
            addFieldError(getFieldName());
        }
    }

    public NotifyCondition validateCondition(String condition)
    {
        try
        {
            NotifyConditionLexer lexer = new NotifyConditionLexer(new StringReader(condition));
            NotifyConditionParser parser = new NotifyConditionParser(lexer);
            parser.condition();
            AST t = parser.getAST();
            if(t != null)
            {
                NotifyConditionTreeParser tree = new NotifyConditionTreeParser();
                tree.setNotifyConditionFactory(notifyConditionFactory);
                return tree.cond(t);
            }
        }
        catch(MismatchedTokenException mte)
        {
            if(mte.token.getText() == null)
            {
                setDefaultMessage("line " + mte.getLine() + ":" + mte.getColumn() + ": end of input when expecting " + NotifyConditionParser._tokenNames[mte.expecting]);
            }
            else
            {
                setDefaultMessage(mte.toString());
            }
        }
        catch(NoViableAltException nvae)
        {
            if(nvae.token.getText() == null)
            {
                setDefaultMessage("line " + nvae.getLine() + ":" + nvae.getColumn() + ": unexpected end of input");
            }
            else
            {
                setDefaultMessage(nvae.toString());
            }
        }
        catch (Exception e)
        {
            setDefaultMessage(e.toString());
        }

        return null;
    }

    public void setNotifyConditionFactory(NotifyConditionFactory notifyConditionFactory)
    {
        this.notifyConditionFactory = notifyConditionFactory;
    }
}

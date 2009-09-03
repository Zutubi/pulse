package com.zutubi.pulse.master.validation.validators;

import antlr.MismatchedTokenException;
import antlr.NoViableAltException;
import antlr.collections.AST;
import com.zutubi.pulse.master.notifications.condition.NotifyCondition;
import com.zutubi.pulse.master.notifications.condition.NotifyConditionFactory;
import com.zutubi.pulse.master.notifications.condition.antlr.NotifyConditionLexer;
import com.zutubi.pulse.master.notifications.condition.antlr.NotifyConditionParser;
import com.zutubi.pulse.master.notifications.condition.antlr.NotifyConditionTreeParser;
import com.zutubi.validation.ValidationException;
import com.zutubi.validation.validators.StringFieldValidatorSupport;

import java.io.StringReader;

/**
 * Validates custom project subscription condition expressions by parsing
 * them.
 */
public class SubscriptionConditionValidator extends StringFieldValidatorSupport
{
    private NotifyConditionFactory notifyConditionFactory;

    public void validateStringField(String condition) throws ValidationException
    {
        validateCondition(condition);
    }

    public NotifyCondition validateCondition(String condition)
    {
        NotifyCondition result = null;

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
                result = tree.cond(t);
            }

            if(result == null)
            {
                addError();
            }
        }
        catch(MismatchedTokenException mte)
        {
            if(mte.token.getText() == null)
            {
                addErrorMessage("line " + mte.getLine() + ":" + mte.getColumn() + ": end of input when expecting " + NotifyConditionParser._tokenNames[mte.expecting]);
            }
            else
            {
                addErrorMessage(mte.toString());
            }
        }
        catch(NoViableAltException nvae)
        {
            if(nvae.token.getText() == null)
            {
                addErrorMessage("line " + nvae.getLine() + ":" + nvae.getColumn() + ": unexpected end of input");
            }
            else
            {
                addErrorMessage(nvae.toString());
            }
        }
        catch (Exception e)
        {
            addErrorMessage(e.toString());
        }

        return result;
    }

    public void setNotifyConditionFactory(NotifyConditionFactory notifyConditionFactory)
    {
        this.notifyConditionFactory = notifyConditionFactory;
    }
}

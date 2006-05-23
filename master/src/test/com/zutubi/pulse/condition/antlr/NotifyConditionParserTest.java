package com.zutubi.pulse.condition.antlr;

import com.zutubi.pulse.test.PulseTestCase;
import com.zutubi.pulse.core.ObjectFactory;
import com.zutubi.pulse.condition.*;
import com.zutubi.pulse.model.Subscription;
import antlr.collections.AST;
import antlr.TokenStreamException;
import antlr.RecognitionException;

import java.io.StringReader;

/**
 */
public class NotifyConditionParserTest extends PulseTestCase
{
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    public void testParseTrue()
    {
        NotifyCondition condition = parseExpression("true");
        assertTrue(condition instanceof TrueNotifyCondition);
    }

    public void testParseFalse()
    {
        NotifyCondition condition = parseExpression("false");
        assertTrue(condition instanceof FalseNotifyCondition);
    }

    public void testParseSuccess()
    {
        NotifyCondition condition = parseExpression("success");
        assertTrue(condition instanceof SuccessNotifyCondition);
    }

    public void testParseFailure()
    {
        NotifyCondition condition = parseExpression("failure");
        assertTrue(condition instanceof FailureNotifyCondition);
    }

    public void testParseError()
    {
        NotifyCondition condition = parseExpression("error");
        assertTrue(condition instanceof ErrorNotifyCondition);
    }

    public void testParseChanged()
    {
        NotifyCondition condition = parseExpression("changed");
        assertTrue(condition instanceof ChangedNotifyCondition);
    }

    public void testParseChangedByMe()
    {
        NotifyCondition condition = parseExpression("changed.by.me");
        assertTrue(condition instanceof ChangedByMeNotifyCondition);
    }

    public void testParseStateChange()
    {
        NotifyCondition condition = parseExpression("state.change");
        assertTrue(condition instanceof StateChangeNotifyCondition);
    }

    public void testAnd()
    {
        NotifyCondition condition = parseExpression("true and true");
        CompoundNotifyCondition compound = assertAnd(condition);
        assertTrue(compound.getChildren().get(0) instanceof TrueNotifyCondition);
        assertTrue(compound.getChildren().get(1) instanceof TrueNotifyCondition);
    }

    public void testOr()
    {
        NotifyCondition condition = parseExpression("true or true");
        CompoundNotifyCondition compound = assertOr(condition);
        assertTrue(compound.getChildren().get(0) instanceof TrueNotifyCondition);
        assertTrue(compound.getChildren().get(1) instanceof TrueNotifyCondition);
    }

    public void testNot()
    {
        NotifyCondition condition = parseExpression("not true");
        NotNotifyCondition not = assertNot(condition);
        assertTrue(not.getCondition() instanceof TrueNotifyCondition);
    }

    public void testPrecedence()
    {
        NotifyCondition condition = parseExpression("true and false or not changed");
        CompoundNotifyCondition compound = assertOr(condition);
        CompoundNotifyCondition first = assertAnd(compound.getChildren().get(0));
        assertTrue(first.getChildren().get(0) instanceof TrueNotifyCondition);
        assertTrue(first.getChildren().get(1) instanceof FalseNotifyCondition);
        NotNotifyCondition not = assertNot(compound.getChildren().get(1));
        assertTrue(not.getCondition() instanceof ChangedNotifyCondition);
    }

    public void testGrouping()
    {
        NotifyCondition condition = parseExpression("true and (false or not changed)");
        CompoundNotifyCondition compound = assertAnd(condition);
        assertTrue(compound.getChildren().get(0) instanceof TrueNotifyCondition);
        CompoundNotifyCondition second = assertOr(compound.getChildren().get(1));
        assertTrue(second.getChildren().get(0) instanceof FalseNotifyCondition);
        NotNotifyCondition not = assertNot(second.getChildren().get(1));
        assertTrue(not.getCondition() instanceof ChangedNotifyCondition);
    }

    public void testParseEmpty()
    {
        NotifyCondition condition = parseExpression("");
        assertTrue(condition instanceof FalseNotifyCondition);
    }

    public void testParseWhitespace()
    {
        NotifyCondition condition = parseExpression("   ");
        assertTrue(condition instanceof FalseNotifyCondition);
    }

    private CompoundNotifyCondition assertAnd(NotifyCondition condition)
    {
        assertTrue(condition instanceof CompoundNotifyCondition);
        CompoundNotifyCondition compound = (CompoundNotifyCondition) condition;
        assertFalse(compound.isDisjunctive());
        return compound;
    }

    private CompoundNotifyCondition assertOr(NotifyCondition condition)
    {
        assertTrue(condition instanceof CompoundNotifyCondition);
        CompoundNotifyCondition compound = (CompoundNotifyCondition) condition;
        assertTrue(compound.isDisjunctive());
        return compound;
    }

    private NotNotifyCondition assertNot(NotifyCondition condition)
    {
        assertTrue(condition instanceof NotNotifyCondition);
        return (NotNotifyCondition) condition;
    }

    private NotifyCondition parseExpression(String expression)
    {
        Subscription s = new Subscription();
        s.setCondition(expression);
        NotifyConditionFactory factory = new NotifyConditionFactory();
        factory.setObjectFactory(new ObjectFactory());
        s.setNotifyConditionFactory(factory);
        return s.getNotifyCondition();
    }
}

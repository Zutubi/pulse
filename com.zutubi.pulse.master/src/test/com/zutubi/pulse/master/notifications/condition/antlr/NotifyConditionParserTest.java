package com.zutubi.pulse.master.notifications.condition.antlr;

import antlr.MismatchedTokenException;
import antlr.collections.AST;
import com.zutubi.pulse.core.api.PulseRuntimeException;
import com.zutubi.pulse.core.test.api.PulseTestCase;
import com.zutubi.pulse.master.notifications.condition.*;
import com.zutubi.pulse.master.validation.validators.SubscriptionConditionValidator;
import com.zutubi.util.bean.DefaultObjectFactory;

import java.io.StringReader;

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

    public void testParseSkipped()
    {
        NotifyCondition condition = parseExpression("skipped");
        assertTrue(condition instanceof SkippedNotifyCondition);
    }

    public void testParseSuccess()
    {
        NotifyCondition condition = parseExpression("success");
        assertTrue(condition instanceof SuccessNotifyCondition);
    }

    public void testParseWarnings()
    {
        NotifyCondition condition = parseExpression("warnings");
        assertTrue(condition instanceof WarningsNotifyCondition);
    }

    public void testParseHealthy()
    {
        NotifyCondition condition = parseExpression("healthy");
        assertTrue(condition instanceof HealthyNotifyCondition);
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

    public void testParseTerminated()
    {
        NotifyCondition condition = parseExpression("terminated");
        assertTrue(condition instanceof TerminatedNotifyCondition);
    }

    public void testParseBroken()
    {
        NotifyCondition condition = parseExpression("broken");
        assertTrue(condition instanceof BrokenNotifyCondition);
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

    public void testParseChangedByMeSinceHealthy()
    {
        NotifyCondition condition = parseExpression("changed.by.me.since.healthy");
        assertTrue(condition instanceof ChangedByMeSinceHealthyNotifyCondition);
    }

    public void testParseChangedByMeSinceSuccess()
    {
        NotifyCondition condition = parseExpression("changed.by.me.since.success");
        assertTrue(condition instanceof ChangedByMeSinceSuccessNotifyCondition);
    }

    public void testParseResponsibilityTaken()
    {
        NotifyCondition condition = parseExpression("responsibility.taken");
        assertTrue(condition instanceof ResponsibilityTakenNotifyCondition);
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
        failureHelper("", "line 1:1: unexpected end of input");
    }

    public void testParseWhitespace()
    {
        failureHelper("   ", "line 1:4: unexpected end of input");
    }

    public void testEqualExpressionEqual()
    {
        NotifyCondition condition = parseExpression("10 == 10");
        assertTrue(condition.satisfied(null, null));
    }

    public void testEqualExpressionGreater()
    {
        NotifyCondition condition = parseExpression("10 == 2");
        assertFalse(condition.satisfied(null, null));
    }

    public void testEqualExpressionLess()
    {
        NotifyCondition condition = parseExpression("0 == 2");
        assertFalse(condition.satisfied(null, null));
    }

    public void testNotEqualExpressionEqual()
    {
        NotifyCondition condition = parseExpression("10 != 10");
        assertFalse(condition.satisfied(null, null));
    }

    public void testNotEqualExpressionGreater()
    {
        NotifyCondition condition = parseExpression("100 != 2");
        assertTrue(condition.satisfied(null, null));
    }

    public void testNotEqualExpressionLess()
    {
        NotifyCondition condition = parseExpression("1 != 2");
        assertTrue(condition.satisfied(null, null));
    }

    public void testGreaterExpressionEqual()
    {
        NotifyCondition condition = parseExpression("4 > 4");
        assertFalse(condition.satisfied(null, null));
    }

    public void testGreaterExpressionGreater()
    {
        NotifyCondition condition = parseExpression("5 > 2");
        assertTrue(condition.satisfied(null, null));
    }

    public void testGreaterExpressionLess()
    {
        NotifyCondition condition = parseExpression("1 > 2");
        assertFalse(condition.satisfied(null, null));
    }

    public void testGreaterEqualExpressionEqual()
    {
        NotifyCondition condition = parseExpression("4 >= 4");
        assertTrue(condition.satisfied(null, null));
    }

    public void testGreaterEqualExpressionGreater()
    {
        NotifyCondition condition = parseExpression("5 >= 2");
        assertTrue(condition.satisfied(null, null));
    }

    public void testGreaterEqualExpressionLess()
    {
        NotifyCondition condition = parseExpression("1 >= 2");
        assertFalse(condition.satisfied(null, null));
    }

    public void testLessExpressionEqual()
    {
        NotifyCondition condition = parseExpression("4 < 4");
        assertFalse(condition.satisfied(null, null));
    }

    public void testLessExpressionGreater()
    {
        NotifyCondition condition = parseExpression("30 < 20");
        assertFalse(condition.satisfied(null, null));
    }

    public void testLessExpressionLess()
    {
        NotifyCondition condition = parseExpression("1 < 2");
        assertTrue(condition.satisfied(null, null));
    }

    public void testLessEqualExpressionEqual()
    {
        NotifyCondition condition = parseExpression("4 <= 4");
        assertTrue(condition.satisfied(null, null));
    }

    public void testLessEqualExpressionGreater()
    {
        NotifyCondition condition = parseExpression("50 <= 49");
        assertFalse(condition.satisfied(null, null));
    }

    public void testLessEqualExpressionLess()
    {
        NotifyCondition condition = parseExpression("1 <= 200000");
        assertTrue(condition.satisfied(null, null));
    }

    public void testJustInteger()
    {
        failureHelper("10", "line 1:3: unexpected end of input");
    }

    public void testDanglingOperator()
    {
        failureHelper("10 >", "line 1:5: unexpected end of input");
    }

    public void testCompareBooleanLeft()
    {
        failureHelper("true == 5", "line 1:6: unexpected token: ==");
    }

    public void testCompareBooleanRight()
    {
        failureHelper("10 > true", "line 1:6: unexpected token: true");
    }

    public void testComparisonInBooleanExpression()
    {
        NotifyCondition condition = parseExpression("true and 3 > 2 or false");
        assertTrue(condition.satisfied(null, null));
    }

    public void testBrokenBuilds()
    {
        NotifyCondition condition = parseExpression("broken.count.builds == 5");
        assertTrue(condition instanceof ComparisonNotifyCondition);
        ComparisonNotifyCondition comp = (ComparisonNotifyCondition) condition;
        assertTrue(comp.getLeft() instanceof BrokenCountBuildsValue);
        assertTrue(comp.getRight() instanceof LiteralNotifyIntegerValue);
        assertEquals(5, comp.getRight().getValue(null, null));
    }

    public void testBrokenDays()
    {
        NotifyCondition condition = parseExpression("5 <= broken.count.days");
        assertTrue(condition instanceof ComparisonNotifyCondition);
        ComparisonNotifyCondition comp = (ComparisonNotifyCondition) condition;
        assertTrue(comp.getLeft() instanceof LiteralNotifyIntegerValue);
        assertEquals(5, comp.getLeft().getValue(null, null));
        assertTrue(comp.getRight() instanceof BrokenCountDaysValue);
    }

    public void testParamsCondition()
    {
        NotifyCondition condition = parseExpression("success(previous)");
        assertTrue(condition instanceof PreviousNotifyCondition);
        assertTrue(((PreviousNotifyCondition) condition).getDelegate() instanceof SuccessNotifyCondition);
    }

    public void testParamsConditionGrouped()
    {
        CompoundNotifyCondition condition = assertAnd(parseExpression("true and (false or success(previous))"));
        assertTrue(condition.getChildren().get(0) instanceof TrueNotifyCondition);
        CompoundNotifyCondition subCondition = assertOr(condition.getChildren().get(1));
        assertTrue(subCondition.getChildren().get(0) instanceof FalseNotifyCondition);
        NotifyCondition subSubCondition = subCondition.getChildren().get(1);
        assertTrue(subSubCondition instanceof PreviousNotifyCondition);
        assertTrue(((PreviousNotifyCondition) subSubCondition).getDelegate() instanceof SuccessNotifyCondition);
    }

    public void testParamsValue()
    {
        NotifyCondition condition = parseExpression("broken.count.days(previous) < 5");
        assertTrue(condition instanceof ComparisonNotifyCondition);
        ComparisonNotifyCondition comparison = (ComparisonNotifyCondition) condition;
        assertTrue(comparison.getLeft() instanceof PreviousNotifyIntegerValue);
        assertTrue(((PreviousNotifyIntegerValue) comparison.getLeft()).getDelegate() instanceof BrokenCountDaysValue);
        assertTrue(comparison.getRight() instanceof LiteralNotifyIntegerValue);
    }

    public void testFailingForXDays()
    {
        parseExpression("broken.count.days(previous) < 5 and broken.count.days >= 5");
    }

    public void testFailingForXBuilds()
    {
        parseExpression("broken.count.builds == 3");
    }

    public void testUnrecognisedParameter()
    {
        failureHelper("success(word)", "line 1:9: expecting \"previous\", found 'word'");
    }

    public void testUnrecognisedParameterKnownToken()
    {
        failureHelper("success(true)", "line 1:9: expecting \"previous\", found 'true'");
    }

    public void testLiteralParameters()
    {
        failureHelper("5(previous)", "line 1:2: unexpected token: (");
    }

    public void testMisplacedParameters()
    {
        failureHelper("true or (previous)", "line 1:10: unexpected token: previous");
    }

    private void failureHelper(String expression, String error)
    {
        try
        {
            parseExpression(expression);
            fail();
        }
        catch (PulseRuntimeException e)
        {
            assertEquals(error, e.getMessage());
        }
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
        NotifyConditionFactory factory = new NotifyConditionFactory();
        factory.setObjectFactory(new DefaultObjectFactory());
        final String[] errorMessage = new String[1];
        SubscriptionConditionValidator validator = new SubscriptionConditionValidator()
        {
            public void addErrorMessage(String message)
            {
                errorMessage[0] = message;
            }
        };

        validator.setNotifyConditionFactory(factory);
        NotifyCondition condition = validator.validateCondition(expression);
        if(condition == null)
        {
            throw new PulseRuntimeException(errorMessage[0]);
        }

        return condition;
    }

    public static void main(String argv[])
    {
        try
        {
            NotifyConditionLexer lexer = new NotifyConditionLexer(new StringReader("1 == 2"));
            NotifyConditionParser parser = new NotifyConditionParser(lexer);
            parser.condition();
            AST t = parser.getAST();
            System.out.println("t.toString() = " + t.toString());
        }
        catch(MismatchedTokenException mte)
        {
            if(mte.token.getText() == null)
            {
                System.err.println("Caught error: line " + mte.getLine() + ":" + mte.getColumn() + ": end of input when expecting " + NotifyConditionParser._tokenNames[mte.expecting]);
            }
            else
            {
                System.err.println("Caught error: " + mte.toString());
            }
        }
        catch (Exception e)
        {
            System.err.println("Caught error: " + e.toString());
        }

    }
}

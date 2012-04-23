package com.zutubi.pulse.master.notifications.condition;

import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.notifications.NotifyConditionContext;
import com.zutubi.pulse.master.tove.config.user.UserConfiguration;

/**
 *
 *
 */
public class ComparisonNotifyCondition implements NotifyCondition
{

    public enum Op
    {
        EQUAL
        {
            public String toToken()
            {
                return "==";
            }

            public boolean evaluate(Comparable left, Comparable right)
            {
                if (left == null)
                {
                    return right == null;
                }
                if (right == null)
                {
                    return false;
                }
                checkMatchingType(left, right);
                return left.compareTo(right) == 0;
            }
        },
        NOT_EQUAL
        {
            public String toToken()
            {
                return "!=";
            }

            public boolean evaluate(Comparable left, Comparable right)
            {
                return !EQUAL.evaluate(left, right);
            }
        },
        LESS_THAN
        {
            public String toToken()
            {
                return "<";
            }

            public boolean evaluate(Comparable left, Comparable right)
            {
                checkMatchingType(left, right);
                return left.compareTo(right) < 0;
            }
        },
        LESS_THAN_OR_EQUAL
        {
            public String toToken()
            {
                return "<=";
            }

            public boolean evaluate(Comparable left, Comparable right)
            {
                checkMatchingType(left, right);
                return left.compareTo(right) <= 0;
            }
        },
        GREATER_THAN
        {
            public String toToken()
            {
                return ">";
            }

            public boolean evaluate(Comparable left, Comparable right)
            {
                checkMatchingType(left, right);
                return left.compareTo(right) > 0;
            }
        },
        GREATER_THAN_OR_EQUAL
        {
            public String toToken()
            {
                return ">=";
            }

            public boolean evaluate(Comparable left, Comparable right)
            {
                checkMatchingType(left, right);
                return left.compareTo(right) >= 0;
            }
        };

        public abstract String toToken();
        public abstract boolean evaluate(Comparable left, Comparable right);

        public static Op fromToken(String token)
        {
            for(Op o: values())
            {
                if(o.toToken().equals(token))
                {
                    return o;
                }
            }

            throw new IllegalArgumentException("Unrecognised token '" + token + "'");
        }

        private static void checkMatchingType(Comparable left, Comparable right)
        {
            if (left == null || right == null)
            {
                throw new IllegalArgumentException("Unable to compare values '"+left+"' and '"+right+"'.  One of them is null.");
            }
            if (left.getClass() != right.getClass())
            {
                throw new IllegalArgumentException("Unable to compare values '"+left+"' and '"+right+"'.  Types do not match.");
            }
        }
    }

    private NotifyValue left;
    private NotifyValue right;
    private Op op;

    public ComparisonNotifyCondition(NotifyValue left, NotifyValue right, Op op)
    {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public boolean satisfied(NotifyConditionContext context, UserConfiguration user)
    {
        BuildResult buildResult = context.getBuildResult();
        Comparable leftValue = left.getValue(buildResult, user);
        Comparable rightValue = right.getValue(buildResult, user);

        return op.evaluate(leftValue, rightValue);
    }

    public NotifyValue getLeft()
    {
        return left;
    }

    public NotifyValue getRight()
    {
        return right;
    }

    public Op getOp()
    {
        return op;
    }
}


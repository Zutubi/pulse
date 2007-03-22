package com.zutubi.pulse.condition;

import com.zutubi.pulse.model.BuildResult;
import com.zutubi.pulse.model.User;

/**
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

            public boolean evaluate(int left, int right)
            {
                return left == right;
            }
        },
        NOT_EQUAL
        {
            public String toToken()
            {
                return "!=";
            }

            public boolean evaluate(int left, int right)
            {
                return left != right;
            }
        },
        LESS_THAN
        {
            public String toToken()
            {
                return "<";
            }

            public boolean evaluate(int left, int right)
            {
                return left < right;
            }
        },
        LESS_THAN_OR_EQUAL
        {
            public String toToken()
            {
                return "<=";
            }

            public boolean evaluate(int left, int right)
            {
                return left <= right;
            }
        },
        GREATER_THAN
        {
            public String toToken()
            {
                return ">";
            }

            public boolean evaluate(int left, int right)
            {
                return left > right;
            }
        },
        GREATER_THAN_OR_EQUAL
        {
            public String toToken()
            {
                return ">=";
            }

            public boolean evaluate(int left, int right)
            {
                return left >= right;
            }
        };

        public abstract String toToken();
        public abstract boolean evaluate(int left, int right);

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
    }

    private NotifyIntegerValue left;
    private NotifyIntegerValue right;
    private Op op;

    public ComparisonNotifyCondition(NotifyIntegerValue left, NotifyIntegerValue right, Op op)
    {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    public boolean satisfied(BuildResult result, User user)
    {
        return op.evaluate(left.getValue(result, user), right.getValue(result, user));
    }

    public NotifyIntegerValue getLeft()
    {
        return left;
    }

    public NotifyIntegerValue getRight()
    {
        return right;
    }

    public Op getOp()
    {
        return op;
    }
}

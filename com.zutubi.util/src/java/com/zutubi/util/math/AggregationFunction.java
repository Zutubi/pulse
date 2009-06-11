package com.zutubi.util.math;

import com.zutubi.util.BinaryFunction;
import com.zutubi.util.CollectionUtils;

import java.util.Collection;

/**
 * Functions that cna be used to combine a collection of numbers into a single
 * number.
 */
public enum AggregationFunction
{
    /**
     * Aggregates numbers by adding them together.
     */
    SUM
    {
        public Number internalAggregate(Collection<Number> numbers)
        {
            Number first = numbers.iterator().next();
            Number base;
            if (first instanceof Integer)
            {
                base = 0;
            }
            else if (first instanceof Long)
            {
                base = 0L;
            }
            else
            {
                base = 0d;
            }

            return CollectionUtils.reduce(numbers, base, new NumberAddition());
        }
    },
    /**
     * Aggregates numbers by taking their average.
     */
    MEAN
    {
        public Number internalAggregate(Collection<Number> numbers)
        {
            Number sum = SUM.internalAggregate(numbers);
            return sum.doubleValue() / numbers.size();
        }
    },
    /**
     * Aggregates numbers by selecting the minimum number.
     */
    MIN
    {
        public Number internalAggregate(Collection<Number> numbers)
        {
            return CollectionUtils.reduce(numbers, numbers.iterator().next(), new NumberMin());
        }
    },
    /**
     * Aggregates numbers by selecting the maximum number.
     */
    MAX
    {
        public Number internalAggregate(Collection<Number> numbers)
        {
            return CollectionUtils.reduce(numbers, numbers.iterator().next(), new NumberMax());
        }
    };

    /**
     * Aggregates the given numbers according to this function.
     *
     * @param numbers the numbers to aggregate, must not be empty.
     * @return the aggregation of the given numbers
     */
    public Number aggregate(Collection<Number> numbers)
    {
        if (numbers.size() == 0)
        {
            throw new IllegalArgumentException("Cannot aggregate an empty collection");
        }

        return internalAggregate(numbers);
    }

    protected abstract Number internalAggregate(Collection<Number> numbers);

    /**
     * Abstract base for binary functions that operate on two numbers.  Hides
     * the ugliness of the different number types.  Note that only doubles and
     * integers are currently supported.
    */
    public static abstract class NumberFunction implements BinaryFunction<Number, Number, Number>
    {
        public Number process(Number input1, Number input2)
        {
            if (input1 instanceof Integer && input2 instanceof Integer)
            {
                return processIntegers(input1.intValue(), input2.intValue());
            }
            else if (input1 instanceof Long && input2 instanceof Long)
            {
                return processLongs(input1.longValue(), input2.longValue());
            }
            else if (input1 instanceof Double && input2 instanceof Double)
            {
                return processDoubles(input1.doubleValue(), input2.doubleValue());
            }
            else
            {
                throw new IllegalArgumentException("Unsupported argument types or combination: " + input1.getClass().getName() + " and " + input2.getClass().getName());
            }
        }

        protected abstract int processIntegers(int i1, int i2);
        protected abstract long processLongs(long l1, long l2);
        protected abstract double processDoubles(double d1, double d2);
    }

    /**
     * A function that adds two numbers.
    */
    public static class NumberAddition extends NumberFunction
    {
        protected int processIntegers(int i1, int i2)
        {
            return i1 + i2;
        }

        protected long processLongs(long l1, long l2)
        {
            return l1 + l2;
        }

        protected double processDoubles(double d1, double d2)
        {
            return d1 + d2;
        }
    }

    /**
     * A function that returns the maximum of two numbers.
    */
    public static class NumberMax extends NumberFunction
    {
        protected int processIntegers(int i1, int i2)
        {
            return Math.max(i1, i2);
        }

        protected long processLongs(long l1, long l2)
        {
            return Math.max(l1, l2);
        }

        protected double processDoubles(double d1, double d2)
        {
            return Math.max(d1, d2);
        }
    }

    /**
     * A function that returns the minimum of two numbers.
    */
    public static class NumberMin extends NumberFunction
    {
        protected int processIntegers(int i1, int i2)
        {
            return Math.min(i1, i2);
        }

        protected long processLongs(long l1, long l2)
        {
            return Math.min(l1, l2);
        }

        protected double processDoubles(double d1, double d2)
        {
            return Math.min(d1, d1);
        }
    }
}

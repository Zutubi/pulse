/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zutubi.validation.validators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <class-comment/>
 */
public class DateRangeValidator extends AbstractRangeValidator<Date>
{
    private String dateFormat = "dd/MM/yyyy";

    private String min;
    private String max;

    public void setMax(String max)
    {
        this.max = max;
    }

    public String getMax()
    {
        return max;
    }

    public void setMin(String min)
    {
        this.min = min;
    }

    public String getMin()
    {
        return min;
    }

    protected Date getMaxComparatorValue()
    {
        if (max != null)
        {
            try
            {
                return toDate(max);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    protected Date getMinComparatorValue()
    {
        if (min != null)
        {
            try
            {
                return toDate(min);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    private Date toDate(String str) throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat(dateFormat);
        return format.parse(str);
    }
}

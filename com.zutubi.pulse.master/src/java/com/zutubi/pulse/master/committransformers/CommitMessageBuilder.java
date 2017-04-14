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

package com.zutubi.pulse.master.committransformers;

import com.opensymphony.util.TextUtils;

import java.util.LinkedList;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Used to apply multiple transformations to a commit message.  The process
 * is quite tricky as we have multiple replacements, encoding issues trimming
 * issues and so on.
 */
public class CommitMessageBuilder
{
    private String message;
    private LinkedList<Boolean> mods;
    private LinkedList<Boolean> exclusiveMods;
    private LinkedList<Boolean> visible;

    public CommitMessageBuilder(String message)
    {
        this.message = message;
        this.mods = new LinkedList<Boolean>();
        this.exclusiveMods = new LinkedList<Boolean>();
        for (int i = 0; i < message.length(); i++)
        {
            mods.add(false);
            exclusiveMods.add(false);
        }
    }

    public String toString()
    {
        return message;
    }

    public String replace(Substitution substitution)
    {
        return replace(substitution.getExpression(), substitution.getReplacement(), false);
    }

    public String replace(String regex, String replacement, boolean exclusive)
    {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        StringBuilder modifiedMessage = new StringBuilder();
        LinkedList<Boolean> modifiedMods = new LinkedList<Boolean>();
        LinkedList<Boolean> modifiedExclusiveMods = new LinkedList<Boolean>();

        int end = 0;

        while (matcher.find())
        {
            // Ignore matches that overlap an exclusive-modified area.
            if (!exclusiveMods.subList(matcher.start(), matcher.end()).contains(true))
            {
                // Ignore this match if it overlaps a modified region.
                int start = matcher.start();

                // a) the unmatched piece up to the start of the match.
                modifiedMessage.append(message.substring(end, start));
                modifiedMods.addAll(mods.subList(end, start));
                modifiedExclusiveMods.addAll(exclusiveMods.subList(end, start));

                // b) the replacement for the matched region
                String replacementStr = SubstitutionUtils.processSubstitution(replacement, matcher);
                modifiedMessage.append(replacementStr);
                for (int i = 0; i < replacementStr.length(); i++)
                {
                    modifiedMods.add(true);
                    modifiedExclusiveMods.add(exclusive);
                }

                end = matcher.end();
            }
        }

        if (end < message.length())
        {
            String tail = message.substring(end);
            modifiedMessage.append(tail);
            modifiedMods.addAll(mods.subList(end, mods.size()));
            modifiedExclusiveMods.addAll(exclusiveMods.subList(end, exclusiveMods.size()));
        }

        // update the stored message.
        message = modifiedMessage.toString();
        mods = modifiedMods;
        exclusiveMods = modifiedExclusiveMods;

        return toString();
    }

    public String encode()
    {
        StringBuilder modifiedMessage = new StringBuilder();
        LinkedList<Boolean> modifiedMods = new LinkedList<Boolean>();

        for (int i = 0; i < message.length(); i++)
        {
            if (mods.get(i))
            {
                modifiedMessage.append(message.charAt(i));
                modifiedMods.add(mods.get(i));
            }
            else
            {
                String replacementStr = TextUtils.htmlEncode("" + message.charAt(i));
                modifiedMessage.append(replacementStr);
                for (int j = 0; j < replacementStr.length(); j++)
                {
                    modifiedMods.add(false);
                }
            }
        }

        message = modifiedMessage.toString();
        mods = modifiedMods;

        return toString();
    }

    public String trim(int length)
    {
        recalculateVisibleIndicies();

        String s = message;

        if (length == 0)
        {
            return "";
        }

        if (visibleLength() <= length)
        {
            return s;
        }

        // move forwards through the string until we reach the trim length.
        StringBuilder result = new StringBuilder();
        int i;
        for (i = 0; i < s.length(); i++)
        {
            if (visible.get(i))
            {
                if (result.length() < length)
                {
                    result.append(s.charAt(i));
                }
                if (result.length() == length)
                {
                    break;
                }
            }
            else
            {
                result.append(s.charAt(i));
                length++;
            }
        }

        // is i in a modified zone? if so, ensure that we repare any broken tags.
        if (mods.get(i))
        {
            int j;
            for (j = i; 0 <= j; j--)
            {
                if (!mods.get(j))
                {
                    break;
                }
            }
            // shuffle forward to the first modified.
            j++;

            String balanced = balanceTags(message.substring(j, i + 1));
            result.replace(j, i + 1, balanced);
        }

        // move backwards though the result, replacing the last 3 characters with an dot.
        int count = 0;
        for (; 0 <= i && count < 3; i--)
        {
            if (visible.get(i))
            {
                count++;
                result.setCharAt(i, '.');
            }
        }

        message = result.toString();
        mods = new LinkedList<Boolean>(mods.subList(0, message.length()));

        return toString();
    }

    public String wrap(int lineLength)
    {
        // work through the message, recording each possible split location as it is encountered.
        // when a split is required, apply a split the recent location.

        recalculateVisibleIndicies();

        int splitOption = -1;
        int currentLineLength = 0;
        int lineStart = 0;
        int visiblePastSplit = 0;

        StringBuilder modifiedMessage = new StringBuilder();

        for (int i = 0; i < message.length(); i++)
        {
            if (!visible.get(i))
            {
                continue;
            }

            char c = message.charAt(i);
            if (Character.isWhitespace(c))
            {
                if (c == '\n')
                {
                    modifiedMessage.append(message.substring(lineStart, i + 1));

                    currentLineLength = 0;
                    visiblePastSplit = 0;
                    lineStart = i + 1;
                    splitOption = -1;
                    continue;
                }

                splitOption = i;
                visiblePastSplit = 0;
            }
            else
            {
                visiblePastSplit++;
            }

            currentLineLength++;

            if (lineLength < currentLineLength && splitOption != -1)
            {
                // split.
                modifiedMessage.append(message.substring(lineStart, splitOption));
                modifiedMessage.append("\n");

                currentLineLength = visiblePastSplit;
                visiblePastSplit = 0;
                lineStart = splitOption + 1;
                splitOption = -1;
            }
        }

        if (lineStart < message.length())
        {
            modifiedMessage.append(message.substring(lineStart));
        }

        message = modifiedMessage.toString();

        return message;
    }

    public int getLength()
    {
        recalculateVisibleIndicies();
        return visibleLength();
    }

    private int visibleLength()
    {
        int i = 0;
        for (boolean b : visible)
        {
            if (b)
            {
                i++;
            }
        }
        return i;
    }

    private void recalculateVisibleIndicies()
    {
        visible = new LinkedList<Boolean>();

        boolean isTag = false;
        for (int i = 0; i < message.length(); i++)
        {
            char c = message.charAt(i);
            if (mods.get(i))
            {
                if (c == '<' && !isTag)
                {
                    isTag = true;
                }

                visible.add(!isTag);

                if (c == '>' && isTag)
                {
                    isTag = false;
                }
            }
            else
            {
                isTag = false;
                visible.add(true);
            }
        }
    }

    private String balanceTags(String html)
    {
        // trimmed result.
        StringBuffer buffer = new StringBuffer(html.length());

        boolean isTag = false;

        StringBuffer tag = new StringBuffer();

        Stack<String> stack = new Stack<String>();

        for (int i = 0; i < html.length(); i++)
        {
            char c = html.charAt(i);
            if (c == '<' && !isTag)
            {
                isTag = true;
            }

            if (isTag)
            {
                tag.append(c);
            }

            buffer.append(c);

            if (c == '>' && isTag)
            {
                isTag = false;
                if (tag.charAt(1) == '/')
                {
                    stack.pop();
                }
                else
                {
                    stack.push(tag.toString());
                }
                tag = new StringBuffer();
            }
        }

        while(stack.size() > 0)
        {
            buffer.append(toClosingTag(stack.pop()));
        }

        return buffer.toString();
    }

    private String toClosingTag(String tag)
    {
        StringBuffer name = new StringBuffer();
        // extract the tag name.
        for (char c : tag.toCharArray())
        {
            if (c == '<')
            {
                continue;
            }
            if (Character.isWhitespace(c) && name.length() > 0)
            {
                break;
            }
            if (c == '>')
            {
                break;
            }
            name.append(c);
        }

        name.append(">");
        name.insert(0, "</");
        return name.toString();
    }

}

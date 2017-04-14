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

// $ANTLR 2.7.6 (20051207): "NotifyCondition.g" -> "NotifyConditionLexer.java"$

    package com.zutubi.pulse.master.notifications.condition.antlr;

public interface NotifyConditionParserTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int LITERAL_or = 4;
	int LITERAL_and = 5;
	int LITERAL_not = 6;
	int LEFT_PAREN = 7;
	int RIGHT_PAREN = 8;
	int LITERAL_true = 9;
	int LITERAL_false = 10;
	int LITERAL_success = 11;
	int LITERAL_failure = 12;
	int LITERAL_error = 13;
	int LITERAL_changed = 14;
	// "changed.by.me" = 15
	// "success.after.failure" = 16
	// "state.change" = 17
	int WORD = 18;
	int WHITESPACE = 19;
}

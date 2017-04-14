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

// $ANTLR 2.7.6 (2005-12-22): "NotifyCondition.g" -> "NotifyConditionLexer.java"$

    package com.zutubi.pulse.master.notifications.condition.antlr;

public interface NotifyConditionTreeParserTokenTypes {
	int EOF = 1;
	int NULL_TREE_LOOKAHEAD = 3;
	int LITERAL_and = 4;
	int LITERAL_or = 5;
	int LITERAL_not = 6;
	int EQUAL = 7;
	int NOT_EQUAL = 8;
	int LESS_THAN = 9;
	int LESS_THAN_OR_EQUAL = 10;
	int GREATER_THAN = 11;
	int GREATER_THAN_OR_EQUAL = 12;
	int LITERAL_previous = 13;
	int LITERAL_changed = 14;
	// "by.me" = 15
	// "include.upstream" = 16
	// "since.healthy" = 17
	// "since.success" = 18
	int INTEGER = 19;
	int LITERAL_true = 20;
	int LITERAL_false = 21;
	int LITERAL_skipped = 22;
	int LITERAL_success = 23;
	int LITERAL_warnings = 24;
	int LITERAL_failure = 25;
	int LITERAL_error = 26;
	int LITERAL_terminated = 27;
	int LITERAL_healthy = 28;
	int LITERAL_broken = 29;
	// "responsibility.taken" = 30
	// "state.change" = 31
	// "broken.count.builds" = 32
	// "broken.count.days" = 33
	int LEFT_PAREN = 34;
	int RIGHT_PAREN = 35;
	int COMMA = 36;
	int WORD = 37;
	int WHITESPACE = 38;
}

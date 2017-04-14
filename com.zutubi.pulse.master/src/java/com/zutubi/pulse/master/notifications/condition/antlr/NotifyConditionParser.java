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

// $ANTLR 2.7.6 (2005-12-22): "NotifyCondition.g" -> "NotifyConditionParser.java"$

    package com.zutubi.pulse.master.notifications.condition.antlr;

import antlr.*;
import antlr.collections.AST;

public class NotifyConditionParser extends antlr.LLkParser       implements NotifyConditionTreeParserTokenTypes
 {

protected NotifyConditionParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public NotifyConditionParser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected NotifyConditionParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

public NotifyConditionParser(TokenStream lexer) {
  this(lexer,1);
}

public NotifyConditionParser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
  buildTokenTypeASTClassMap();
  astFactory = new ASTFactory(getTokenTypeToASTClassMap());
}

	public final void condition() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST condition_AST = null;
		
		orexpression();
		astFactory.addASTChild(currentAST, returnAST);
		AST tmp1_AST = null;
		tmp1_AST = astFactory.create(LT(1));
		astFactory.addASTChild(currentAST, tmp1_AST);
		match(Token.EOF_TYPE);
		condition_AST = (AST)currentAST.root;
		returnAST = condition_AST;
	}
	
	public final void orexpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST orexpression_AST = null;
		
		andexpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop27:
		do {
			if ((LA(1)==LITERAL_or)) {
				AST tmp2_AST = null;
				tmp2_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp2_AST);
				match(LITERAL_or);
				andexpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop27;
			}
			
		} while (true);
		}
		orexpression_AST = (AST)currentAST.root;
		returnAST = orexpression_AST;
	}
	
	public final void andexpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST andexpression_AST = null;
		
		notexpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop30:
		do {
			if ((LA(1)==LITERAL_and)) {
				AST tmp3_AST = null;
				tmp3_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp3_AST);
				match(LITERAL_and);
				notexpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop30;
			}
			
		} while (true);
		}
		andexpression_AST = (AST)currentAST.root;
		returnAST = andexpression_AST;
	}
	
	public final void notexpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST notexpression_AST = null;
		
		{
		switch ( LA(1)) {
		case LITERAL_not:
		{
			AST tmp4_AST = null;
			tmp4_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp4_AST);
			match(LITERAL_not);
			break;
		}
		case LITERAL_changed:
		case INTEGER:
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_skipped:
		case LITERAL_success:
		case LITERAL_warnings:
		case LITERAL_failure:
		case LITERAL_error:
		case LITERAL_terminated:
		case LITERAL_healthy:
		case LITERAL_broken:
		case 30:
		case 31:
		case 32:
		case 33:
		case LEFT_PAREN:
		{
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		boolexpression();
		astFactory.addASTChild(currentAST, returnAST);
		notexpression_AST = (AST)currentAST.root;
		returnAST = notexpression_AST;
	}
	
	public final void boolexpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST boolexpression_AST = null;
		
		switch ( LA(1)) {
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_skipped:
		case LITERAL_success:
		case LITERAL_warnings:
		case LITERAL_failure:
		case LITERAL_error:
		case LITERAL_terminated:
		case LITERAL_healthy:
		case LITERAL_broken:
		case 30:
		case 31:
		{
			boolsymbol();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case LEFT_PAREN:
			{
				match(LEFT_PAREN);
				AST tmp6_AST = null;
				tmp6_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp6_AST);
				match(LITERAL_previous);
				match(RIGHT_PAREN);
				break;
			}
			case EOF:
			case LITERAL_and:
			case LITERAL_or:
			case RIGHT_PAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			boolexpression_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_changed:
		{
			AST tmp8_AST = null;
			tmp8_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp8_AST);
			match(LITERAL_changed);
			{
			switch ( LA(1)) {
			case LEFT_PAREN:
			{
				match(LEFT_PAREN);
				changedmodifier();
				astFactory.addASTChild(currentAST, returnAST);
				{
				_loop37:
				do {
					if ((LA(1)==COMMA)) {
						match(COMMA);
						changedmodifier();
						astFactory.addASTChild(currentAST, returnAST);
					}
					else {
						break _loop37;
					}
					
				} while (true);
				}
				match(RIGHT_PAREN);
				break;
			}
			case EOF:
			case LITERAL_and:
			case LITERAL_or:
			case RIGHT_PAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			boolexpression_AST = (AST)currentAST.root;
			break;
		}
		case LEFT_PAREN:
		{
			match(LEFT_PAREN);
			orexpression();
			astFactory.addASTChild(currentAST, returnAST);
			match(RIGHT_PAREN);
			boolexpression_AST = (AST)currentAST.root;
			break;
		}
		case INTEGER:
		case 32:
		case 33:
		{
			compareexpression();
			astFactory.addASTChild(currentAST, returnAST);
			boolexpression_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = boolexpression_AST;
	}
	
	public final void boolsymbol() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST boolsymbol_AST = null;
		
		switch ( LA(1)) {
		case LITERAL_true:
		{
			AST tmp14_AST = null;
			tmp14_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp14_AST);
			match(LITERAL_true);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_false:
		{
			AST tmp15_AST = null;
			tmp15_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp15_AST);
			match(LITERAL_false);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_skipped:
		{
			AST tmp16_AST = null;
			tmp16_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp16_AST);
			match(LITERAL_skipped);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_success:
		{
			AST tmp17_AST = null;
			tmp17_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp17_AST);
			match(LITERAL_success);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_warnings:
		{
			AST tmp18_AST = null;
			tmp18_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp18_AST);
			match(LITERAL_warnings);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_failure:
		{
			AST tmp19_AST = null;
			tmp19_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp19_AST);
			match(LITERAL_failure);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_error:
		{
			AST tmp20_AST = null;
			tmp20_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp20_AST);
			match(LITERAL_error);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_terminated:
		{
			AST tmp21_AST = null;
			tmp21_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp21_AST);
			match(LITERAL_terminated);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_healthy:
		{
			AST tmp22_AST = null;
			tmp22_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp22_AST);
			match(LITERAL_healthy);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_broken:
		{
			AST tmp23_AST = null;
			tmp23_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp23_AST);
			match(LITERAL_broken);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		case 30:
		{
			AST tmp24_AST = null;
			tmp24_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp24_AST);
			match(30);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		case 31:
		{
			AST tmp25_AST = null;
			tmp25_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp25_AST);
			match(31);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = boolsymbol_AST;
	}
	
	public final void changedmodifier() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST changedmodifier_AST = null;
		
		switch ( LA(1)) {
		case 15:
		{
			AST tmp26_AST = null;
			tmp26_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp26_AST);
			match(15);
			changedmodifier_AST = (AST)currentAST.root;
			break;
		}
		case 16:
		{
			AST tmp27_AST = null;
			tmp27_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp27_AST);
			match(16);
			changedmodifier_AST = (AST)currentAST.root;
			break;
		}
		case 17:
		{
			AST tmp28_AST = null;
			tmp28_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp28_AST);
			match(17);
			changedmodifier_AST = (AST)currentAST.root;
			break;
		}
		case 18:
		{
			AST tmp29_AST = null;
			tmp29_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp29_AST);
			match(18);
			changedmodifier_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = changedmodifier_AST;
	}
	
	public final void compareexpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST compareexpression_AST = null;
		
		integer();
		astFactory.addASTChild(currentAST, returnAST);
		{
		switch ( LA(1)) {
		case EQUAL:
		{
			AST tmp30_AST = null;
			tmp30_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp30_AST);
			match(EQUAL);
			break;
		}
		case NOT_EQUAL:
		{
			AST tmp31_AST = null;
			tmp31_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp31_AST);
			match(NOT_EQUAL);
			break;
		}
		case LESS_THAN:
		{
			AST tmp32_AST = null;
			tmp32_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp32_AST);
			match(LESS_THAN);
			break;
		}
		case LESS_THAN_OR_EQUAL:
		{
			AST tmp33_AST = null;
			tmp33_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp33_AST);
			match(LESS_THAN_OR_EQUAL);
			break;
		}
		case GREATER_THAN:
		{
			AST tmp34_AST = null;
			tmp34_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp34_AST);
			match(GREATER_THAN);
			break;
		}
		case GREATER_THAN_OR_EQUAL:
		{
			AST tmp35_AST = null;
			tmp35_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp35_AST);
			match(GREATER_THAN_OR_EQUAL);
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		}
		integer();
		astFactory.addASTChild(currentAST, returnAST);
		compareexpression_AST = (AST)currentAST.root;
		returnAST = compareexpression_AST;
	}
	
	public final void integer() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST integer_AST = null;
		
		switch ( LA(1)) {
		case INTEGER:
		{
			AST tmp36_AST = null;
			tmp36_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp36_AST);
			match(INTEGER);
			integer_AST = (AST)currentAST.root;
			break;
		}
		case 32:
		case 33:
		{
			intsymbol();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case LEFT_PAREN:
			{
				match(LEFT_PAREN);
				AST tmp38_AST = null;
				tmp38_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp38_AST);
				match(LITERAL_previous);
				match(RIGHT_PAREN);
				break;
			}
			case EOF:
			case LITERAL_and:
			case LITERAL_or:
			case EQUAL:
			case NOT_EQUAL:
			case LESS_THAN:
			case LESS_THAN_OR_EQUAL:
			case GREATER_THAN:
			case GREATER_THAN_OR_EQUAL:
			case RIGHT_PAREN:
			{
				break;
			}
			default:
			{
				throw new NoViableAltException(LT(1), getFilename());
			}
			}
			}
			integer_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = integer_AST;
	}
	
	public final void intsymbol() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST intsymbol_AST = null;
		
		switch ( LA(1)) {
		case 32:
		{
			AST tmp40_AST = null;
			tmp40_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp40_AST);
			match(32);
			intsymbol_AST = (AST)currentAST.root;
			break;
		}
		case 33:
		{
			AST tmp41_AST = null;
			tmp41_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp41_AST);
			match(33);
			intsymbol_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = intsymbol_AST;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"and\"",
		"\"or\"",
		"\"not\"",
		"EQUAL",
		"NOT_EQUAL",
		"LESS_THAN",
		"LESS_THAN_OR_EQUAL",
		"GREATER_THAN",
		"GREATER_THAN_OR_EQUAL",
		"\"previous\"",
		"\"changed\"",
		"\"by.me\"",
		"\"include.upstream\"",
		"\"since.healthy\"",
		"\"since.success\"",
		"an integer",
		"\"true\"",
		"\"false\"",
		"\"skipped\"",
		"\"success\"",
		"\"warnings\"",
		"\"failure\"",
		"\"error\"",
		"\"terminated\"",
		"\"healthy\"",
		"\"broken\"",
		"\"responsibility.taken\"",
		"\"state.change\"",
		"\"broken.count.builds\"",
		"\"broken.count.days\"",
		"an opening parenthesis '('",
		"a closing parenthesis ')'",
		"a comma",
		"a word",
		"WHITESPACE"
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	
	}

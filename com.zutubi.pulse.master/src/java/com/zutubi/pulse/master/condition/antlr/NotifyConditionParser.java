// $ANTLR 2.7.6 (20051207): "NotifyCondition.g" -> "NotifyConditionParser.java"$

    package com.zutubi.pulse.master.condition.antlr;

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
		_loop22:
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
				break _loop22;
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
		_loop25:
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
				break _loop25;
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
		case INTEGER:
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_success:
		case LITERAL_failure:
		case LITERAL_error:
		case LITERAL_changed:
		case 21:
		case 22:
		case 23:
		case 24:
		case 25:
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
		case LITERAL_success:
		case LITERAL_failure:
		case LITERAL_error:
		case LITERAL_changed:
		case 21:
		case 22:
		case 23:
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
		case 24:
		case 25:
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
			AST tmp10_AST = null;
			tmp10_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp10_AST);
			match(LITERAL_true);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_false:
		{
			AST tmp11_AST = null;
			tmp11_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp11_AST);
			match(LITERAL_false);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_success:
		{
			AST tmp12_AST = null;
			tmp12_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp12_AST);
			match(LITERAL_success);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_failure:
		{
			AST tmp13_AST = null;
			tmp13_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp13_AST);
			match(LITERAL_failure);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_error:
		{
			AST tmp14_AST = null;
			tmp14_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp14_AST);
			match(LITERAL_error);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_changed:
		{
			AST tmp15_AST = null;
			tmp15_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp15_AST);
			match(LITERAL_changed);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		case 21:
		{
			AST tmp16_AST = null;
			tmp16_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp16_AST);
			match(21);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		case 22:
		{
			AST tmp17_AST = null;
			tmp17_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp17_AST);
			match(22);
			boolsymbol_AST = (AST)currentAST.root;
			break;
		}
		case 23:
		{
			AST tmp18_AST = null;
			tmp18_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp18_AST);
			match(23);
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
			AST tmp19_AST = null;
			tmp19_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp19_AST);
			match(EQUAL);
			break;
		}
		case NOT_EQUAL:
		{
			AST tmp20_AST = null;
			tmp20_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp20_AST);
			match(NOT_EQUAL);
			break;
		}
		case LESS_THAN:
		{
			AST tmp21_AST = null;
			tmp21_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp21_AST);
			match(LESS_THAN);
			break;
		}
		case LESS_THAN_OR_EQUAL:
		{
			AST tmp22_AST = null;
			tmp22_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp22_AST);
			match(LESS_THAN_OR_EQUAL);
			break;
		}
		case GREATER_THAN:
		{
			AST tmp23_AST = null;
			tmp23_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp23_AST);
			match(GREATER_THAN);
			break;
		}
		case GREATER_THAN_OR_EQUAL:
		{
			AST tmp24_AST = null;
			tmp24_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp24_AST);
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
			AST tmp25_AST = null;
			tmp25_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp25_AST);
			match(INTEGER);
			integer_AST = (AST)currentAST.root;
			break;
		}
		case 24:
		case 25:
		{
			intsymbol();
			astFactory.addASTChild(currentAST, returnAST);
			{
			switch ( LA(1)) {
			case LEFT_PAREN:
			{
				match(LEFT_PAREN);
				AST tmp27_AST = null;
				tmp27_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp27_AST);
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
		case 24:
		{
			AST tmp29_AST = null;
			tmp29_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp29_AST);
			match(24);
			intsymbol_AST = (AST)currentAST.root;
			break;
		}
		case 25:
		{
			AST tmp30_AST = null;
			tmp30_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp30_AST);
			match(25);
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
		"an integer",
		"\"true\"",
		"\"false\"",
		"\"success\"",
		"\"failure\"",
		"\"error\"",
		"\"changed\"",
		"\"changed.by.me\"",
		"\"changed.by.me.since.success\"",
		"\"state.change\"",
		"\"unsuccessful.count.builds\"",
		"\"unsuccessful.count.days\"",
		"an opening parenthesis '('",
		"a closing parenthesis ')'",
		"a word",
		"WHITESPACE"
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	
	}

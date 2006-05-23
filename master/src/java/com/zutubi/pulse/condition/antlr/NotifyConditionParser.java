// $ANTLR 2.7.6 (20051207): "NotifyCondition.g" -> "NotifyConditionParser.java"$

    package com.zutubi.pulse.condition.antlr;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.collections.AST;
import java.util.Hashtable;
import antlr.ASTFactory;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

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

	public final void orexpression() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST orexpression_AST = null;
		
		andexpression();
		astFactory.addASTChild(currentAST, returnAST);
		{
		_loop8:
		do {
			if ((LA(1)==LITERAL_or)) {
				AST tmp12_AST = null;
				tmp12_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp12_AST);
				match(LITERAL_or);
				andexpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop8;
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
		_loop11:
		do {
			if ((LA(1)==LITERAL_and)) {
				AST tmp13_AST = null;
				tmp13_AST = astFactory.create(LT(1));
				astFactory.makeASTRoot(currentAST, tmp13_AST);
				match(LITERAL_and);
				notexpression();
				astFactory.addASTChild(currentAST, returnAST);
			}
			else {
				break _loop11;
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
			AST tmp14_AST = null;
			tmp14_AST = astFactory.create(LT(1));
			astFactory.makeASTRoot(currentAST, tmp14_AST);
			match(LITERAL_not);
			break;
		}
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_success:
		case LITERAL_failure:
		case LITERAL_error:
		case LITERAL_changed:
		case 13:
		case 14:
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
		atom();
		astFactory.addASTChild(currentAST, returnAST);
		notexpression_AST = (AST)currentAST.root;
		returnAST = notexpression_AST;
	}
	
	public final void atom() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST atom_AST = null;
		
		switch ( LA(1)) {
		case LITERAL_true:
		case LITERAL_false:
		case LITERAL_success:
		case LITERAL_failure:
		case LITERAL_error:
		case LITERAL_changed:
		case 13:
		case 14:
		{
			condition();
			astFactory.addASTChild(currentAST, returnAST);
			atom_AST = (AST)currentAST.root;
			break;
		}
		case LEFT_PAREN:
		{
			match(LEFT_PAREN);
			orexpression();
			astFactory.addASTChild(currentAST, returnAST);
			match(RIGHT_PAREN);
			atom_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = atom_AST;
	}
	
	public final void condition() throws RecognitionException, TokenStreamException {
		
		returnAST = null;
		ASTPair currentAST = new ASTPair();
		AST condition_AST = null;
		
		switch ( LA(1)) {
		case LITERAL_true:
		{
			AST tmp17_AST = null;
			tmp17_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp17_AST);
			match(LITERAL_true);
			condition_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_false:
		{
			AST tmp18_AST = null;
			tmp18_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp18_AST);
			match(LITERAL_false);
			condition_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_success:
		{
			AST tmp19_AST = null;
			tmp19_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp19_AST);
			match(LITERAL_success);
			condition_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_failure:
		{
			AST tmp20_AST = null;
			tmp20_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp20_AST);
			match(LITERAL_failure);
			condition_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_error:
		{
			AST tmp21_AST = null;
			tmp21_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp21_AST);
			match(LITERAL_error);
			condition_AST = (AST)currentAST.root;
			break;
		}
		case LITERAL_changed:
		{
			AST tmp22_AST = null;
			tmp22_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp22_AST);
			match(LITERAL_changed);
			condition_AST = (AST)currentAST.root;
			break;
		}
		case 13:
		{
			AST tmp23_AST = null;
			tmp23_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp23_AST);
			match(13);
			condition_AST = (AST)currentAST.root;
			break;
		}
		case 14:
		{
			AST tmp24_AST = null;
			tmp24_AST = astFactory.create(LT(1));
			astFactory.addASTChild(currentAST, tmp24_AST);
			match(14);
			condition_AST = (AST)currentAST.root;
			break;
		}
		default:
		{
			throw new NoViableAltException(LT(1), getFilename());
		}
		}
		returnAST = condition_AST;
	}
	
	
	public static final String[] _tokenNames = {
		"<0>",
		"EOF",
		"<2>",
		"NULL_TREE_LOOKAHEAD",
		"\"and\"",
		"\"or\"",
		"\"not\"",
		"\"true\"",
		"\"false\"",
		"\"success\"",
		"\"failure\"",
		"\"error\"",
		"\"changed\"",
		"\"changed.by.me\"",
		"\"state.change\"",
		"LEFT_PAREN",
		"RIGHT_PAREN",
		"WORD",
		"WHITESPACE"
	};
	
	protected void buildTokenTypeASTClassMap() {
		tokenTypeToASTClassMap=null;
	};
	
	
	}

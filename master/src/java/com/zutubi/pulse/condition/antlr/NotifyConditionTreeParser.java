// $ANTLR 2.7.6 (20051207): "NotifyCondition.g" -> "NotifyConditionTreeParser.java"$

    package com.zutubi.pulse.condition.antlr;

import antlr.TreeParser;
import antlr.Token;
import antlr.collections.AST;
import antlr.RecognitionException;
import antlr.ANTLRException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.collections.impl.BitSet;
import antlr.ASTPair;
import antlr.collections.impl.ASTArray;

    import com.zutubi.pulse.bootstrap.ComponentContext;
    import com.zutubi.pulse.condition.NotifyCondition;
    import com.zutubi.pulse.condition.NotifyConditionFactory;
    import com.zutubi.pulse.condition.CompoundNotifyCondition;
    import com.zutubi.pulse.condition.NotNotifyCondition;


public class NotifyConditionTreeParser extends antlr.TreeParser       implements NotifyConditionTreeParserTokenTypes
 {

    private NotifyConditionFactory factory;

    public void setNotifyConditionFactory(NotifyConditionFactory factory)
    {
        this.factory = factory;
    }
public NotifyConditionTreeParser() {
	tokenNames = _tokenNames;
}

	public final NotifyCondition  cond(AST _t) throws RecognitionException {
		NotifyCondition r;
		
		AST cond_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST c = null;
		
		NotifyCondition a, b;
		r = null;
		
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LITERAL_and:
			{
				AST __t2 = _t;
				AST tmp1_AST_in = (AST)_t;
				match(_t,LITERAL_and);
				_t = _t.getFirstChild();
				a=cond(_t);
				_t = _retTree;
				b=cond(_t);
				_t = _retTree;
				_t = __t2;
				_t = _t.getNextSibling();
				r = new CompoundNotifyCondition(a, b, false);
				break;
			}
			case LITERAL_or:
			{
				AST __t3 = _t;
				AST tmp2_AST_in = (AST)_t;
				match(_t,LITERAL_or);
				_t = _t.getFirstChild();
				a=cond(_t);
				_t = _retTree;
				b=cond(_t);
				_t = _retTree;
				_t = __t3;
				_t = _t.getNextSibling();
				r = new CompoundNotifyCondition(a, b, true);
				break;
			}
			case LITERAL_not:
			{
				AST __t4 = _t;
				AST tmp3_AST_in = (AST)_t;
				match(_t,LITERAL_not);
				_t = _t.getFirstChild();
				a=cond(_t);
				_t = _retTree;
				_t = __t4;
				_t = _t.getNextSibling();
				r = new NotNotifyCondition(a);
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
			{
				c = _t==ASTNULL ? null : (AST)_t;
				condition(_t);
				_t = _retTree;
				r = factory.createCondition(c.getText());
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return r;
	}
	
	public final void condition(AST _t) throws RecognitionException {
		
		AST condition_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LITERAL_true:
			{
				AST tmp4_AST_in = (AST)_t;
				match(_t,LITERAL_true);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_false:
			{
				AST tmp5_AST_in = (AST)_t;
				match(_t,LITERAL_false);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_success:
			{
				AST tmp6_AST_in = (AST)_t;
				match(_t,LITERAL_success);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_failure:
			{
				AST tmp7_AST_in = (AST)_t;
				match(_t,LITERAL_failure);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_error:
			{
				AST tmp8_AST_in = (AST)_t;
				match(_t,LITERAL_error);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_changed:
			{
				AST tmp9_AST_in = (AST)_t;
				match(_t,LITERAL_changed);
				_t = _t.getNextSibling();
				break;
			}
			case 13:
			{
				AST tmp10_AST_in = (AST)_t;
				match(_t,13);
				_t = _t.getNextSibling();
				break;
			}
			case 14:
			{
				AST tmp11_AST_in = (AST)_t;
				match(_t,14);
				_t = _t.getNextSibling();
				break;
			}
			default:
			{
				throw new NoViableAltException(_t);
			}
			}
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
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
	
	}
	

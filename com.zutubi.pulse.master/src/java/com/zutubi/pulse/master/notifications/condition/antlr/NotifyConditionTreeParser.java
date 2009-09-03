// $ANTLR 2.7.6 (20051207): "NotifyCondition.g" -> "NotifyConditionTreeParser.java"$

    package com.zutubi.pulse.master.notifications.condition.antlr;

import antlr.NoViableAltException;
import antlr.RecognitionException;
import antlr.collections.AST;
import com.zutubi.pulse.master.notifications.condition.*;


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
				AST tmp31_AST_in = (AST)_t;
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
				AST tmp32_AST_in = (AST)_t;
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
				AST tmp33_AST_in = (AST)_t;
				match(_t,LITERAL_not);
				_t = _t.getFirstChild();
				a=cond(_t);
				_t = _retTree;
				_t = __t4;
				_t = _t.getNextSibling();
				r = new NotNotifyCondition(a);
				break;
			}
			case EQUAL:
			case NOT_EQUAL:
			case LESS_THAN:
			case LESS_THAN_OR_EQUAL:
			case GREATER_THAN:
			case GREATER_THAN_OR_EQUAL:
			{
				r=comp(_t);
				_t = _retTree;
				break;
			}
			case LITERAL_previous:
			{
				r=prev(_t);
				_t = _retTree;
				break;
			}
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
				c = _t==ASTNULL ? null : (AST)_t;
				boolsymbol(_t);
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
	
	public final NotifyCondition  comp(AST _t) throws RecognitionException {
		NotifyCondition r;
		
		AST comp_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		NotifyValue x, y;
		r = null;
		
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case EQUAL:
			{
				AST __t6 = _t;
				AST tmp34_AST_in = (AST)_t;
				match(_t,EQUAL);
				_t = _t.getFirstChild();
				x=integer(_t);
				_t = _retTree;
				y=integer(_t);
				_t = _retTree;
				_t = __t6;
				_t = _t.getNextSibling();
				r = new ComparisonNotifyCondition(x, y, ComparisonNotifyCondition.Op.EQUAL);
				break;
			}
			case NOT_EQUAL:
			{
				AST __t7 = _t;
				AST tmp35_AST_in = (AST)_t;
				match(_t,NOT_EQUAL);
				_t = _t.getFirstChild();
				x=integer(_t);
				_t = _retTree;
				y=integer(_t);
				_t = _retTree;
				_t = __t7;
				_t = _t.getNextSibling();
				r = new ComparisonNotifyCondition(x, y, ComparisonNotifyCondition.Op.NOT_EQUAL);
				break;
			}
			case LESS_THAN:
			{
				AST __t8 = _t;
				AST tmp36_AST_in = (AST)_t;
				match(_t,LESS_THAN);
				_t = _t.getFirstChild();
				x=integer(_t);
				_t = _retTree;
				y=integer(_t);
				_t = _retTree;
				_t = __t8;
				_t = _t.getNextSibling();
				r = new ComparisonNotifyCondition(x, y, ComparisonNotifyCondition.Op.LESS_THAN);
				break;
			}
			case LESS_THAN_OR_EQUAL:
			{
				AST __t9 = _t;
				AST tmp37_AST_in = (AST)_t;
				match(_t,LESS_THAN_OR_EQUAL);
				_t = _t.getFirstChild();
				x=integer(_t);
				_t = _retTree;
				y=integer(_t);
				_t = _retTree;
				_t = __t9;
				_t = _t.getNextSibling();
				r = new ComparisonNotifyCondition(x, y, ComparisonNotifyCondition.Op.LESS_THAN_OR_EQUAL);
				break;
			}
			case GREATER_THAN:
			{
				AST __t10 = _t;
				AST tmp38_AST_in = (AST)_t;
				match(_t,GREATER_THAN);
				_t = _t.getFirstChild();
				x=integer(_t);
				_t = _retTree;
				y=integer(_t);
				_t = _retTree;
				_t = __t10;
				_t = _t.getNextSibling();
				r = new ComparisonNotifyCondition(x, y, ComparisonNotifyCondition.Op.GREATER_THAN);
				break;
			}
			case GREATER_THAN_OR_EQUAL:
			{
				AST __t11 = _t;
				AST tmp39_AST_in = (AST)_t;
				match(_t,GREATER_THAN_OR_EQUAL);
				_t = _t.getFirstChild();
				x=integer(_t);
				_t = _retTree;
				y=integer(_t);
				_t = _retTree;
				_t = __t11;
				_t = _t.getNextSibling();
				r = new ComparisonNotifyCondition(x, y, ComparisonNotifyCondition.Op.GREATER_THAN_OR_EQUAL);
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
	
	public final NotifyCondition  prev(AST _t) throws RecognitionException {
		NotifyCondition r;
		
		AST prev_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		r = null;
		
		
		try {      // for error handling
			AST __t13 = _t;
			AST tmp40_AST_in = (AST)_t;
			match(_t,LITERAL_previous);
			_t = _t.getFirstChild();
			r=cond(_t);
			_t = _retTree;
			_t = __t13;
			_t = _t.getNextSibling();
			r = factory.build(PreviousNotifyCondition.class, new Class[]{ NotifyCondition.class }, new Object[]{ r });
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return r;
	}
	
	public final void boolsymbol(AST _t) throws RecognitionException {
		
		AST boolsymbol_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case LITERAL_true:
			{
				AST tmp41_AST_in = (AST)_t;
				match(_t,LITERAL_true);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_false:
			{
				AST tmp42_AST_in = (AST)_t;
				match(_t,LITERAL_false);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_success:
			{
				AST tmp43_AST_in = (AST)_t;
				match(_t,LITERAL_success);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_failure:
			{
				AST tmp44_AST_in = (AST)_t;
				match(_t,LITERAL_failure);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_error:
			{
				AST tmp45_AST_in = (AST)_t;
				match(_t,LITERAL_error);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_changed:
			{
				AST tmp46_AST_in = (AST)_t;
				match(_t,LITERAL_changed);
				_t = _t.getNextSibling();
				break;
			}
			case 21:
			{
				AST tmp47_AST_in = (AST)_t;
				match(_t,21);
				_t = _t.getNextSibling();
				break;
			}
			case 22:
			{
				AST tmp48_AST_in = (AST)_t;
				match(_t,22);
				_t = _t.getNextSibling();
				break;
			}
			case 23:
			{
				AST tmp49_AST_in = (AST)_t;
				match(_t,23);
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
	
	public final NotifyValue  integer(AST _t) throws RecognitionException {
		NotifyValue r;
		
		AST integer_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		AST i = null;
		AST j = null;
		
		r = null;
		
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case INTEGER:
			{
				i = (AST)_t;
				match(_t,INTEGER);
				_t = _t.getNextSibling();
				r = new LiteralNotifyIntegerValue(Integer.parseInt(i.getText()));
				break;
			}
			case LITERAL_previous:
			{
				r=previnteger(_t);
				_t = _retTree;
				break;
			}
			case 24:
			case 25:
			{
				j = _t==ASTNULL ? null : (AST)_t;
				intsymbol(_t);
				_t = _retTree;
				r = factory.createIntegerValue(j.getText());
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
	
	public final NotifyValue  previnteger(AST _t) throws RecognitionException {
		NotifyValue r;
		
		AST previnteger_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		r = null;
		
		
		try {      // for error handling
			AST __t16 = _t;
			AST tmp50_AST_in = (AST)_t;
			match(_t,LITERAL_previous);
			_t = _t.getFirstChild();
			r=integer(_t);
			_t = _retTree;
			_t = __t16;
			_t = _t.getNextSibling();
			r = factory.build(PreviousNotifyIntegerValue.class, new Class[]{ NotifyIntegerValue.class }, new Object[]{ r });
		}
		catch (RecognitionException ex) {
			reportError(ex);
			if (_t!=null) {_t = _t.getNextSibling();}
		}
		_retTree = _t;
		return r;
	}
	
	public final void intsymbol(AST _t) throws RecognitionException {
		
		AST intsymbol_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case 24:
			{
				AST tmp51_AST_in = (AST)_t;
				match(_t,24);
				_t = _t.getNextSibling();
				break;
			}
			case 25:
			{
				AST tmp52_AST_in = (AST)_t;
				match(_t,25);
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
	
	}
	

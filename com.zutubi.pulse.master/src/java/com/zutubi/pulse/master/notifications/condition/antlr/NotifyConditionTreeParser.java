// $ANTLR 2.7.6 (2005-12-22): "NotifyCondition.g" -> "NotifyConditionTreeParser.java"$

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
				AST tmp42_AST_in = (AST)_t;
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
				AST tmp43_AST_in = (AST)_t;
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
				AST tmp44_AST_in = (AST)_t;
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
			case LITERAL_changed:
			{
				r=change(_t);
				_t = _retTree;
				break;
			}
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
				AST tmp45_AST_in = (AST)_t;
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
				AST tmp46_AST_in = (AST)_t;
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
				AST tmp47_AST_in = (AST)_t;
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
				AST tmp48_AST_in = (AST)_t;
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
				AST tmp49_AST_in = (AST)_t;
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
				AST tmp50_AST_in = (AST)_t;
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
			AST tmp51_AST_in = (AST)_t;
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
	
	public final ChangedNotifyCondition  change(AST _t) throws RecognitionException {
		ChangedNotifyCondition r;
		
		AST change_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		r = factory.build(ChangedNotifyCondition.class);
		
		
		try {      // for error handling
			AST __t15 = _t;
			AST tmp52_AST_in = (AST)_t;
			match(_t,LITERAL_changed);
			_t = _t.getFirstChild();
			{
			_loop17:
			do {
				if (_t==null) _t=ASTNULL;
				if (((_t.getType() >= 15 && _t.getType() <= 18))) {
					changemod(_t,r);
					_t = _retTree;
				}
				else {
					break _loop17;
				}
				
			} while (true);
			}
			_t = __t15;
			_t = _t.getNextSibling();
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
				AST tmp53_AST_in = (AST)_t;
				match(_t,LITERAL_true);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_false:
			{
				AST tmp54_AST_in = (AST)_t;
				match(_t,LITERAL_false);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_skipped:
			{
				AST tmp55_AST_in = (AST)_t;
				match(_t,LITERAL_skipped);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_success:
			{
				AST tmp56_AST_in = (AST)_t;
				match(_t,LITERAL_success);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_warnings:
			{
				AST tmp57_AST_in = (AST)_t;
				match(_t,LITERAL_warnings);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_failure:
			{
				AST tmp58_AST_in = (AST)_t;
				match(_t,LITERAL_failure);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_error:
			{
				AST tmp59_AST_in = (AST)_t;
				match(_t,LITERAL_error);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_terminated:
			{
				AST tmp60_AST_in = (AST)_t;
				match(_t,LITERAL_terminated);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_healthy:
			{
				AST tmp61_AST_in = (AST)_t;
				match(_t,LITERAL_healthy);
				_t = _t.getNextSibling();
				break;
			}
			case LITERAL_broken:
			{
				AST tmp62_AST_in = (AST)_t;
				match(_t,LITERAL_broken);
				_t = _t.getNextSibling();
				break;
			}
			case 30:
			{
				AST tmp63_AST_in = (AST)_t;
				match(_t,30);
				_t = _t.getNextSibling();
				break;
			}
			case 31:
			{
				AST tmp64_AST_in = (AST)_t;
				match(_t,31);
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
			case 32:
			case 33:
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
	
	public final void changemod(AST _t,
		ChangedNotifyCondition c
	) throws RecognitionException {
		
		AST changemod_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		try {      // for error handling
			if (_t==null) _t=ASTNULL;
			switch ( _t.getType()) {
			case 15:
			{
				AST tmp65_AST_in = (AST)_t;
				match(_t,15);
				_t = _t.getNextSibling();
				c.addModifier(ChangedNotifyCondition.Modifier.BY_ME);
				break;
			}
			case 16:
			{
				AST tmp66_AST_in = (AST)_t;
				match(_t,16);
				_t = _t.getNextSibling();
				c.addModifier(ChangedNotifyCondition.Modifier.INCLUDE_UPSTREAM);
				break;
			}
			case 17:
			{
				AST tmp67_AST_in = (AST)_t;
				match(_t,17);
				_t = _t.getNextSibling();
				c.addModifier(ChangedNotifyCondition.Modifier.SINCE_HEALTHY);
				break;
			}
			case 18:
			{
				AST tmp68_AST_in = (AST)_t;
				match(_t,18);
				_t = _t.getNextSibling();
				c.addModifier(ChangedNotifyCondition.Modifier.SINCE_SUCCESS);
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
	
	public final NotifyValue  previnteger(AST _t) throws RecognitionException {
		NotifyValue r;
		
		AST previnteger_AST_in = (_t == ASTNULL) ? null : (AST)_t;
		
		r = null;
		
		
		try {      // for error handling
			AST __t21 = _t;
			AST tmp69_AST_in = (AST)_t;
			match(_t,LITERAL_previous);
			_t = _t.getFirstChild();
			r=integer(_t);
			_t = _retTree;
			_t = __t21;
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
			case 32:
			{
				AST tmp70_AST_in = (AST)_t;
				match(_t,32);
				_t = _t.getNextSibling();
				break;
			}
			case 33:
			{
				AST tmp71_AST_in = (AST)_t;
				match(_t,33);
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
	
	}
	

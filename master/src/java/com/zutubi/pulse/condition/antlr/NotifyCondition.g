header {
    package com.zutubi.pulse.condition.antlr;
}

{
    import com.zutubi.pulse.bootstrap.ComponentContext;
    import com.zutubi.pulse.condition.NotifyCondition;
    import com.zutubi.pulse.condition.NotifyConditionFactory;
    import com.zutubi.pulse.condition.CompoundNotifyCondition;
    import com.zutubi.pulse.condition.NotNotifyCondition;
}

class NotifyConditionTreeParser extends TreeParser;

{
    private NotifyConditionFactory factory;

    public void setNotifyConditionFactory(NotifyConditionFactory factory)
    {
        this.factory = factory;
    }
}

cond returns [NotifyCondition r]
{
    NotifyCondition a, b;
    r = null;
}
    : #("and" a=cond b=cond) { r = new CompoundNotifyCondition(a, b, false); }
    | #("or" a=cond b=cond) { r = new CompoundNotifyCondition(a, b, true); }
    | #("not" a=cond) { r = new NotNotifyCondition(a); }
    | c:condition { r = factory.createCondition(c.getText()); }
    ;

condition
    : "true"
    | "false"
    | "success"
    | "failure"
    | "error"
    | "changed"
    | "changed.by.me"
    | "state.change"
    ;

class NotifyConditionParser extends Parser;

options {
        buildAST=true;
        defaultErrorHandler=false;
}

orexpression
    :   andexpression ("or"^ andexpression)*
    ;

andexpression
    : notexpression ("and"^ notexpression)*
    ;

notexpression
    : ("not"^)? atom
    ;

atom
    : condition
    | LEFT_PAREN! orexpression RIGHT_PAREN!
    ;

condition
    : "true"
    | "false"
    | "success"
    | "failure"
    | "error"
    | "changed"
    | "changed.by.me"
    | "state.change"
    ;

class NotifyConditionLexer extends Lexer;

options {
   k = 1;
}

// Words, which include our operators
WORD: ('a'..'z' | 'A'..'Z' | '.')+ ;

// Grouping
LEFT_PAREN: '(';
RIGHT_PAREN: ')';

WHITESPACE
    :   (' ' | '\t' | '\r' | '\n') { $setType(Token.SKIP); }
    ;

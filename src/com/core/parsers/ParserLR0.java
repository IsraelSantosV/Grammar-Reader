package com.core.parsers;

import com.core.Grammar;

public class ParserLR0 extends ParserLR {

    public ParserLR0(Grammar grammar) {
        super(grammar);
    }

    @Override
    protected void createGoToTable() {

    }

    public boolean parserLR0(){
        return true;
    }

    public boolean parserSLR1(){
        return true;
    }
}

package com.core.parsers;

import com.core.Grammar;

public class ParserLR1 extends ParserLR {

    public ParserLR1(Grammar grammar) {
        super(grammar);
    }

    @Override
    protected void createGoToTable() {

    }

    public boolean parserCLR1(){
        return true;
    }
}

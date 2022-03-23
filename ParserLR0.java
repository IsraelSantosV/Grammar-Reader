package com.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParserLR0 extends Parser {

    private final char SHIFT = 'S';
    private final char REDUCE = 'R';

    private List<Integer> m_States;
    private String[][] m_Actions;
    private Integer[][] m_GoTo;

    public ParserLR0(Syntax syntax) { super(syntax); }

    @Override
    protected void initializeTable() {
        //First step: Extend current grammar
        m_Syntax.extendGrammar();
        initializeStates();

        Set<Syntax.Symbol> terminals = m_Syntax.getAllSymbolsOf(true);
        Set<Syntax.Symbol> notTerminals = m_Syntax.getAllSymbolsOf(false);

        //Add special syntax token and remove void
        terminals.removeIf(currentSymbol -> currentSymbol.Token == m_Syntax.getVoidSymbol());
        terminals.add(new Syntax.Symbol(m_Syntax.getSpecialRuleCharacter(), true));

        //Lines = amount of states | Columns = target characters
        m_Actions = new String[m_States.size()][terminals.size()];
        m_GoTo = new Integer[m_States.size()][notTerminals.size()];

        //Second step: calculate LR Items
        calculateLRItems();
    }

    private void initializeStates(){
        m_States = new ArrayList<>();
        int countOfStates = 0;

        Set<Syntax.Symbol> notTerminals = m_Syntax.getAllSymbolsOf(false);

        for (Syntax.Symbol notTerminal : notTerminals) {
            List<Syntax.OutputRule> rule = m_Syntax.getOutputRule(notTerminal.Token);
            countOfStates += rule.size();
        }

        for(int i = 0; i < countOfStates; i++){
            m_States.add(i);
        }
    }

    @Override
    public void createTable() {

    }

    private void calculateLRItems(){

    }

    private void calculateItem(char[] production, int index){

    }

    @Override
    public void saveTable() {

    }

    @Override
    protected String getSaveFileName() {
        return "ParserLR0";
    }
}

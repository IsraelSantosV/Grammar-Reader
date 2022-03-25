package com.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class ParserLR0 extends Parser {

    private final char SHIFT = 'S';
    private final char REDUCE = 'R';

    //private List<Integer> m_States;
    private String[][] m_Actions;
    private Integer[][] m_GoTo;
    private List<LRState> m_States;

    public class LRState {
        private int m_Index;

        public class LRStateData {
            public String ProductionText;
            public char MyRootTerminal;
            public char[] Productions;

            public int CurrentDotIndex;
        }

        private List<LRStateData> m_Data;

        public LRState(int index){
            m_Index = index;
            m_Data = new ArrayList<>();
        }

        public int getIndex() { return m_Index; }

        public void registerProduction(char rootTerminal, char[] productions, String productionText){
            LRStateData newData = new LRStateData();
            newData.MyRootTerminal = rootTerminal;
            newData.Productions = Arrays.copyOf(productions, productions.length);
            newData.ProductionText = productionText;
            newData.CurrentDotIndex = -1;

            m_Data.add(newData);
        }

        public char getNextTokenOfDot(int currentProduction){
            if(currentProduction < 0 || currentProduction >= m_Data.size()) return ' ';

            int currentDotIndex = m_Data.get(currentProduction).CurrentDotIndex;

            char[] currentProdChar = m_Data.get(currentProduction).Productions;
            if(currentDotIndex + 1 >= currentProdChar.length) return ' ';
            return currentProdChar[currentDotIndex + 1];
        }

        public void advanceDotIndex(int currentProduction){
            if(currentProduction < 0 || currentProduction >= m_Data.size()) return;
            m_Data.get(currentProduction).CurrentDotIndex++;
        }
    }

    public ParserLR0(Syntax syntax) { super(syntax); }

    @Override
    protected void initializeTable() {
        //First step: Extend current grammar
        m_Syntax.extendGrammar();
        setAmountOfStates();

        Set<Syntax.Symbol> terminals = m_Syntax.getAllTerminalSymbols();
        List<Syntax.Symbol> notTerminals = m_Syntax.getAllProductionSymbols();

        //Add special syntax token and remove void
        terminals.removeIf(currentSymbol -> currentSymbol.Token == m_Syntax.getVoidSymbol());
        terminals.add(new Syntax.Symbol(m_Syntax.getSpecialRuleCharacter(), true));

        //Lines = amount of states | Columns = target characters
        //m_Actions = new String[m_States.size()][terminals.size()];
        //m_GoTo = new Integer[m_States.size()][notTerminals.size()];

        //Second step: calculate LR Items
        calculateLRItems();
    }

    private void setAmountOfStates(){
        //m_States = new ArrayList<>();
        int countOfStates = 0;

        List<Syntax.Symbol> notTerminals = m_Syntax.getAllProductionSymbols();

        for (Syntax.Symbol notTerminal : notTerminals) {
            List<Syntax.OutputRule> rule = m_Syntax.getOutputRule(notTerminal.Token);
            countOfStates += rule.size();
        }

        for(int i = 0; i < countOfStates; i++){
            //m_States.add(i);
        }
    }

    @Override
    public void createTable() {

    }

    private void calculateLRItems(){
        m_States = new ArrayList<>();
        List<Syntax.Symbol> grammarSymbols = m_Syntax.getAllProductionSymbols();

        //For each grammar symbol (not terminal) read LR items
        int currentStateIndex = 0;
        for (Syntax.Symbol productionSymbol : grammarSymbols) {
            //Create new state to store values
            LRState currentState = new LRState(currentStateIndex);
            m_States.add(currentState);

            //Get all productions of current grammar symbol
            List<Syntax.OutputRule> productions = m_Syntax.getOutputRule(productionSymbol.Token);
            for (int i = 0; i < productions.size(); i++) {
                Syntax.OutputRule production = productions.get(i);
                //Register current production
                String productionText = productionSymbol.Token + " -> " + Arrays.toString(production.convertOutputInTokens());
                currentState.registerProduction(productionSymbol.Token, production.convertOutputInTokens(), productionText);

                char nextToken = currentState.getNextTokenOfDot(i);
                //Compare if is not terminal
                if(Character.isUpperCase(nextToken) || nextToken == m_Syntax.getInitialToken()){
                    //Get all productions of next token
                    List<Syntax.OutputRule> productionsOfNextToken = m_Syntax.getOutputRule(nextToken);

                    //Add new LRData for each production of next token
                    productionsOfNextToken.forEach(nextProduction -> currentState.registerProduction(
                            nextToken, nextProduction.convertOutputInTokens(),
                            nextToken + " -> " + Arrays.toString(nextProduction.convertOutputInTokens())
                    ));
                }
            }
        }
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

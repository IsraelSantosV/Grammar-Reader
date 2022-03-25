package com.core;

import com.tools.FileResourceUtils;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ParserLL1 extends Parser {

    private String[][] m_Table;
    private Set<Syntax.Symbol> m_Terminals;
    private List<Syntax.Symbol> m_NotTerminals;

    public ParserLL1(Syntax syntax) {
        super(syntax);
    }

    @Override
    protected void initializeTable(){
        m_NotTerminals = m_Syntax.getAllProductionSymbols();
        m_Terminals = m_Syntax.getAllTerminalSymbols();
        m_Terminals.removeIf(currentSymbol -> currentSymbol.Token == m_Syntax.getVoidSymbol());

        //Add special syntax token
        m_Terminals.add(new Syntax.Symbol(m_Syntax.getSpecialRuleCharacter(), true));
        m_Table = new String[m_NotTerminals.size()][m_Terminals.size()];
    }

    protected Syntax.Symbol getTableLabelInIndex(boolean isTerminal, int index){
        if(index < 0 || (isTerminal && index >= m_Terminals.size()) || (!isTerminal && index >= m_NotTerminals.size())){
            return null;
        }

        return (Syntax.Symbol) (isTerminal ? m_Terminals.toArray()[index] : m_NotTerminals.toArray()[index]);
    }

    protected Pair<Integer, Integer> getIndex(Character line, Character column){
        for(int i = 0; i < m_NotTerminals.size(); i++){
            for(int j = 0; j < m_Terminals.size(); j++){
                Syntax.Symbol currentNotTerminal = getTableLabelInIndex(false, i);
                Syntax.Symbol currentTerminal = getTableLabelInIndex(true, j);

                if(currentNotTerminal.Token == line && currentTerminal.Token == column){
                    return new Pair<>(i, j);
                }
            }
        }

        return new Pair<>(INVALID_INDEX, INVALID_INDEX);
    }

    protected void setTableValueWithSet(Character line, Set<Character> columns, String value){
        for (Character columnValue : columns) {
            Pair<Integer, Integer> pairValue = getIndex(line, columnValue);
            if(!Objects.equals(pairValue, new Pair<>(INVALID_INDEX, INVALID_INDEX))){
                String currentValue = m_Table[pairValue.getKey()][pairValue.getValue()];
                if(currentValue == null || currentValue.equals("")){
                    m_Table[pairValue.getKey()][pairValue.getValue()] = value;
                }
                else{
                    m_Table[pairValue.getKey()][pairValue.getValue()] += "/" + value;
                }
            }
        }
    }

    @Override
    public void createTable() {
        for(int i = 0; i < m_NotTerminals.size(); i++){
            Syntax.Symbol currentNotTerminal = getTableLabelInIndex(false, i);
            List<Syntax.OutputRule> productionsOfNotTerminal = m_Syntax.getOutputRule(currentNotTerminal.Token);

            for (Syntax.OutputRule outputRule : productionsOfNotTerminal) {
                //For each production rule A → α, add A → α in each position M[A,a] in which "a" is in First (α).
                String production = currentNotTerminal.Token + " -> " + String.valueOf(outputRule.convertOutputInTokens());

                Syntax.Symbol firstProductionSymbol = outputRule.Output.get(0);
                Set<Character> firstSetOfSymbol = getFirstSetOf(firstProductionSymbol.Token);

                if(firstSetOfSymbol.contains(m_Syntax.getVoidSymbol())){
                    Set<Character> followSetOfRoot = getFollowSetOf(currentNotTerminal.Token);
                    Set<Character> terminalsInRootFollowSet = m_Syntax.getAllTerminalSymbols(followSetOfRoot);
                    setTableValueWithSet(currentNotTerminal.Token, terminalsInRootFollowSet, production);
                }
                else {
                    setTableValueWithSet(currentNotTerminal.Token, firstSetOfSymbol, production);
                }
            }
        }

        System.out.println("Table is generated for " + getSaveFileName());
        saveTable();
    }

    @Override
    protected String getSaveFileName() {
        return "ParserLL1";
    }

    @Override
    public void saveTable() {
        List<String> terminals = new ArrayList<>();
        terminals.add(" "); //Skip first element
        for (Syntax.Symbol terminal : m_Terminals) {
            terminals.add(String.valueOf(terminal.Token));
        }

        List<String[]> lineValues = new ArrayList<>();
        for(int i = 0; i < m_NotTerminals.size(); i++){ //Line
            String[] currentLine = new String[1 + m_Terminals.size()];
            currentLine[0] = String.valueOf(getTableLabelInIndex(false, i).Token);
            lineValues.add(currentLine);
            for(int j = 0; j < m_Terminals.size(); j++){ //Column
                currentLine[j+1] = m_Table[i][j];
            }
        }

        String[] headers = new String[terminals.size()];
        terminals.toArray(headers);

        try {
            FileResourceUtils.writeMatrixCSV(headers, lineValues, getSaveFileName());
            System.out.println("Table saved in: /" + getSaveFileName() + ".csv");
        } catch (IOException e) {
            System.out.println("Error or generate CSV: " + e.getMessage());
        }
    }
}

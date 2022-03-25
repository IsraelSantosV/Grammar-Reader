package com.core.parsers;

import com.core.Grammar;
import com.core.Production;
import com.core.parsers.util.Action;
import com.core.parsers.util.ActionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public abstract class ParserLR {

    protected HashMap<String, Integer>[] m_GoToTable;
    protected HashMap<String, Action>[] m_ActionTable;
    protected Grammar m_Grammar;

    public ParserLR(Grammar grammar){
        m_Grammar = grammar;
    }

    protected abstract void createGoToTable();

    public Grammar getGrammar() { return m_Grammar; }

    public boolean accept(ArrayList<String> inputs){
        inputs.add("$");
        Stack<String> stack = new Stack<>();
        stack.add("0");

        int index = 0;
        while(index < inputs.size()){
            int state = Integer.parseInt(stack.peek());
            String nextInput = inputs.get(index);
            Action action = m_ActionTable[state].get(nextInput);

            if(action == null) return false;
            switch (action.getType()){
                case ACCEPT:
                    return true;
                case SHIFT:
                    stack.push(nextInput);
                    stack.push(action.getOperand() + "");
                    index++;
                    break;
                case REDUCE:
                    int productionIndex = action.getOperand();
                    Production production = m_Grammar.getProductions().get(productionIndex);
                    String variable = production.getVariable();

                    int productionLength = production.getProductions().length;
                    for(int i = 0; i < 2 * productionLength; i++){
                        stack.pop();
                    }

                    int nextState = Integer.parseInt(stack.peek());
                    stack.push(variable);

                    int variableState = m_GoToTable[nextState].get(variable);
                    stack.push(variableState + "");
                    break;
            }
        }

        return false;
    }

    public String getGoToTableString() {
        StringBuilder str = new StringBuilder("GoTo Table : \n");
        str.append("          ");
        for (String variable : m_Grammar.getVariables()) {
            str.append(String.format("%-6s", variable));
        }

        str.append("\n");

        for (int i = 0; i < m_GoToTable.length; i++) {
            for (int j = 0; j < (m_Grammar.getVariables().size()+1)*6+2; j++) {
                str.append("-");
            }

            str.append("\n");
            str.append(String.format("|%-6s|", i));
            for (String variable : m_Grammar.getVariables()) {
                str.append(String.format("%6s", (m_GoToTable[i].get(variable) == null ? "|" : m_GoToTable[i].get(variable) + "|")));
            }
            str.append("\n");
        }

        for (int j = 0; j < (m_Grammar.getVariables().size()+1)*6+2; j++) {
            str.append("-");
        }

        return str.toString();
    }

    public String getActionTableString() {
        StringBuilder str = new StringBuilder("Action Table : \n");
        HashSet<String> terminals = new HashSet<>(m_Grammar.getTerminals());
        terminals.add("$");
        str.append("                ");

        for (String terminal : terminals) {
            str.append(String.format("%-10s", terminal));
        }

        str.append("\n");

        for (int i = 0; i < m_ActionTable.length; i++) {
            for (int j = 0; j < (terminals.size()+1)*10+2; j++) {
                str.append("-");
            }

            str.append("\n");
            str.append(String.format("|%-10s|", i));
            for (String terminal : terminals) {
                str.append(String.format("%10s", (m_ActionTable[i].get(terminal) == null ? "|" : m_ActionTable[i].get(terminal) + "|")));
            }
            str.append("\n");
        }

        for (int j = 0; j < (terminals.size()+1)*10+2; j++) {
            str.append("-");
        }

        return str.toString();
    }

}

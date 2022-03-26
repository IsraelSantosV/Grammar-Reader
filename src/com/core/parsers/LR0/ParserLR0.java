package com.core.parsers.LR0;

import com.core.Grammar;
import com.core.Production;
import com.core.parsers.ParserLR;
import com.core.parsers.util.Action;
import com.core.parsers.util.ActionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ParserLR0 extends ParserLR {

    private ArrayList<LR0State> m_Collection;

    public ParserLR0(Grammar m_Grammar) {
        super(m_Grammar);
    }

    public boolean parserLR0(){
        createStates();
        createGoToTable();
        return createActionTableForLR0();
    }

    public boolean parserSLR1(){
        createStates();
        createGoToTable();
        return createActionTableForSLR1();
    }

    protected void createStates(){
        m_Collection = new ArrayList<>();
        HashSet<LR0Item> startItem = new HashSet<>();
        startItem.add(new LR0Item(m_Grammar.getProductions().get(0)));

        LR0State startState = new LR0State(m_Grammar, startItem);
        m_Collection.add(startState);

        for (int i = 0; i < m_Collection.size(); i++) {
            HashSet<String> stringWithDot = new HashSet<>();
            for (LR0Item item : m_Collection.get(i).getItems()) {
                if (item.getCurrentTerminal() != null) {
                    stringWithDot.add(item.getCurrentTerminal());
                }
            }

            for (String str : stringWithDot) {
                HashSet<LR0Item> nextStateItems = new HashSet<>();
                for (LR0Item item : m_Collection.get(i).getItems()) {
                    if (item.getCurrentTerminal() != null && item.getCurrentTerminal().equals(str)) {
                        LR0Item temporary = new LR0Item(item);
                        temporary.goTo();
                        nextStateItems.add(temporary);
                    }
                }

                LR0State nextState = new LR0State(m_Grammar, nextStateItems);
                boolean isExist = false;

                for (LR0State lr0State : m_Collection) {
                    if (lr0State.getItems().containsAll(nextState.getItems())
                            && nextState.getItems().containsAll(lr0State.getItems())) {

                        isExist = true;
                        m_Collection.get(i).addTransition(str, lr0State);
                    }
                }

                if (!isExist) {
                    m_Collection.add(nextState);
                    m_Collection.get(i).addTransition(str, nextState);
                }
            }
        }
    }

    @Override
    protected void createGoToTable() {
        m_GoToTable = new HashMap[m_Collection.size()];
        for (int i = 0; i < m_GoToTable.length; i++) {
            m_GoToTable[i] = new HashMap<>();
        }

        for (int i = 0; i < m_Collection.size(); i++) {
            for (String s : m_Collection.get(i).getTransitions().keySet()) {
                if (m_Grammar.getVariables().contains(s)) {
                    m_GoToTable[i].put(s, findStateIndex(m_Collection.get(i).getTransitions().get(s)));
                }
            }
        }
    }

    private boolean createActionTableForLR0(){
        m_ActionTable = new HashMap[m_Collection.size()];
        for (int i = 0; i < m_GoToTable.length; i++) {
            m_ActionTable[i] = new HashMap<>();
        }

        for (int i = 0; i < m_Collection.size(); i++) {
            for (String s : m_Collection.get(i).getTransitions().keySet()) {
                if (m_Grammar.getTerminals().contains(s)) {
                    m_ActionTable[i].put(s, new Action(ActionType.SHIFT,
                            findStateIndex(m_Collection.get(i).getTransitions().get(s))));
                }
            }
        }

        for (int i = 0; i < m_Collection.size(); i++) {
            for (LR0Item item : m_Collection.get(i).getItems()) {
                if (item.getDotPointer() == item.getProductions().length) {
                    if (item.getVariable().equals(m_Grammar.getDefinition("EXTEND_GRAMMAR_SYMBOL"))) {
                        m_ActionTable[i].put(m_Grammar.getDefinition("ROOT_GRAMMAR_SYMBOL"), new Action(ActionType.ACCEPT, 0));
                    }
                    else {
                        HashSet<String> terminals = m_Grammar.getTerminals();
                        terminals.add(m_Grammar.getDefinition("ROOT_GRAMMAR_SYMBOL"));
                        Production production = new Production(item.getVariable(), item.getProductions().clone());

                        int index = m_Grammar.findProductionIndex(production);
                        Action action = new Action(ActionType.REDUCE, index);

                        for (String str : terminals) {
                            if (m_ActionTable[i].get(str) != null) {
                                System.out.println("it has a REDUCE-" + m_ActionTable[i].get(str).getType()
                                        + " conflict in state " + i);
                                return false;
                            }
                            else {
                                m_ActionTable[i].put(str, action);
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private boolean createActionTableForSLR1(){
        m_ActionTable = new HashMap[m_Collection.size()];
        for (int i = 0; i < m_GoToTable.length; i++) {
            m_ActionTable[i] = new HashMap<>();
        }

        for (int i = 0; i < m_Collection.size(); i++) {
            for (String s : m_Collection.get(i).getTransitions().keySet()) {
                if (m_Grammar.getTerminals().contains(s)) {
                    m_ActionTable[i].put(s, new Action(ActionType.SHIFT,
                            findStateIndex(m_Collection.get(i).getTransitions().get(s))));
                }
            }
        }

        for (int i = 0; i < m_Collection.size(); i++) {
            for (LR0Item item : m_Collection.get(i).getItems()) {
                if (item.getDotPointer() == item.getProductions().length) {
                    if (item.getVariable().equals(m_Grammar.getDefinition("EXTEND_GRAMMAR_SYMBOL"))) {
                        m_ActionTable[i].put(m_Grammar.getDefinition("ROOT_GRAMMAR_SYMBOL"), new Action(ActionType.ACCEPT, 0));
                    }
                    else {
                        HashSet<String> follow = m_Grammar.getFollowSets().get(item.getVariable());
                        Production production = new Production(item.getVariable(), item.getProductions().clone());

                        int index = m_Grammar.findProductionIndex(production);
                        Action action = new Action(ActionType.REDUCE, index);

                        for (String str : follow) {
                            if (m_ActionTable[i].get(str) != null) {
                                System.out.println("It has a REDUCE-" + m_ActionTable[i].get(str).getType()
                                        + " conflict in state " + i);
                                return false;
                            }
                            else {
                                m_ActionTable[i].put(str, action);
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private int findStateIndex(LR0State state){
        for(int i = 0; i < m_Collection.size(); i++){
            if(m_Collection.get(i).equals(state)){
                return i;
            }
        }

        return -1;
    }

    public String collectionToString(){
        StringBuilder str = new StringBuilder("Collection: \n");
        for(int i = 0; i < m_Collection.size(); i++){
            str.append("State ").append(i).append(" : \n");
            str.append(m_Collection.get(i)).append("\n");
        }

        return str.toString();
    }
}

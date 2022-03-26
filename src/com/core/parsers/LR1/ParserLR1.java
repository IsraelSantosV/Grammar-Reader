package com.core.parsers.LR1;

import com.core.Grammar;
import com.core.Production;
import com.core.parsers.ParserLR;
import com.core.parsers.util.Action;
import com.core.parsers.util.ActionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class ParserLR1 extends ParserLR {

    private ArrayList<LR1State> m_Collection;

    public ParserLR1(Grammar m_Grammar){
        super(m_Grammar);
    }

    protected void createStatesForCLR1() {
        m_Collection = new ArrayList<>();
        HashSet<LR1Item> startItem = new HashSet<>();
        Production startRule = m_Grammar.getProductions().get(0);

        HashSet<String> startLockAHead = new HashSet<>();
        startLockAHead.add(m_Grammar.getDefinition("ROOT_GRAMMAR_SYMBOL"));
        startItem.add(new LR1Item(startRule.getVariable(), startRule.getProductions(),0, startLockAHead));

        LR1State startState = new LR1State(m_Grammar, startItem);
        m_Collection.add(startState);

        for (int i = 0; i < m_Collection.size(); i++) {
            HashSet<String> stringWithDot = new HashSet<>();
            for (LR1Item item : m_Collection.get(i).getItems()) {
                if (item.getCurrent() != null) {
                    stringWithDot.add(item.getCurrent());
                }
            }

            for (String str : stringWithDot) {
                HashSet<LR1Item> nextStateItems = new HashSet<>();
                for (LR1Item item : m_Collection.get(i).getItems()) {
                    if (item.getCurrent() != null && item.getCurrent().equals(str)) {
                        LR1Item temp = new LR1Item(item.getVariable(), item.getProductions(),
                                item.getDotPointer()+1, item.getLookAHead());
                        nextStateItems.add(temp);
                    }
                }

                LR1State nextState = new LR1State(m_Grammar, nextStateItems);
                boolean isExist = false;
                for (LR1State lr1State : m_Collection) {
                    if (lr1State.getItems().containsAll(nextState.getItems())
                            && nextState.getItems().containsAll(lr1State.getItems())) {

                        isExist = true;
                        m_Collection.get(i).getTransitions().put(str, lr1State);
                    }
                }

                if (!isExist) {
                    m_Collection.add(nextState);
                    m_Collection.get(i).getTransitions().put(str, nextState);
                }
            }
        }

    }

    public boolean parserCLR1(){
        createStatesForCLR1();
        createGoToTable();
        return createActionTable();
    }

    protected void createGoToTable() {
        m_GoToTable = new HashMap[m_Collection.size()];
        for (int i = 0; i < m_GoToTable.length; i++) {
            m_GoToTable[i] = new HashMap<>();
        }

        for (int i = 0; i < m_Collection.size(); i++) {
            for (String s : m_Collection.get(i).getTransitions().keySet()) {
                if (m_Grammar.isVariable(s)) {
                    m_GoToTable[i].put(s, findStateIndex(m_Collection.get(i).getTransitions().get(s)));
                }
            }
        }
    }

    private int findStateIndex(LR1State state) {
        for (int i = 0; i < m_Collection.size(); i++) {
            if (m_Collection.get(i).equals(state)) {
                return i;
            }
        }

        return -1;
    }

    private boolean createActionTable() {
        m_ActionTable = new HashMap[m_Collection.size()];
        for (int i = 0; i < m_GoToTable.length; i++) {
            m_ActionTable[i] = new HashMap<>();
        }

        for (int i = 0; i < m_Collection.size(); i++) {
            for (String s : m_Collection.get(i).getTransitions().keySet()) {
                if (m_Grammar.getTerminals().contains(s)) {
                    m_ActionTable[i].put(s, new Action(ActionType.SHIFT, findStateIndex(m_Collection.get(i).getTransitions().get(s))));
                }
            }
        }

        for (int i = 0; i < m_Collection.size(); i++) {
            for (LR1Item item : m_Collection.get(i).getItems()) {
                if (item.getDotPointer() == item.getProductions().length) {
                    if (item.getVariable().equals(m_Grammar.getDefinition("EXTEND_GRAMMAR_SYMBOL"))) {
                        m_ActionTable[i].put(m_Grammar.getDefinition("ROOT_GRAMMAR_SYMBOL"), new Action(ActionType.ACCEPT, 0));
                    }
                    else {
                        Production production = new Production(item.getVariable(), item.getProductions().clone());
                        int index = m_Grammar.findProductionIndex(production);
                        Action action = new Action(ActionType.REDUCE, index);

                        for (String str : item.getLookAHead()) {
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

    public String collectionToString() {
        StringBuilder str = new StringBuilder("Collection : \n");
        for (int i = 0; i < m_Collection.size(); i++) {
            for (String s : Arrays.asList("State " + i + " : \n", m_Collection.get(i) + "\n")) {
                str.append(s);
            }
        }

        return str.toString();
    }
}

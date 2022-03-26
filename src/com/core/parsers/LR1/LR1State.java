package com.core.parsers.LR1;

import com.core.Grammar;
import com.core.Production;

import java.util.*;

public class LR1State {

    private final LinkedHashSet<LR1Item> m_Items;
    private final HashMap<String,LR1State> m_Transitions;

    public LR1State(Grammar grammar, HashSet<LR1Item> coreItems){
        m_Items = new LinkedHashSet<>(coreItems);
        m_Transitions = new HashMap<>();
        closure(grammar);
    }

    private void closure(Grammar grammar) {
        boolean changeFlag = false;
        do {
            changeFlag = false;
            for(LR1Item item : m_Items){
                if(item.getDotPointer() != item.getProductions().length && grammar.isVariable(item.getCurrent())){
                    HashSet<String> lookahead = new HashSet<>();

                    if(item.getDotPointer() == item.getProductions().length - 1){
                        lookahead.addAll(item.getLookAHead());
                    }
                    else{
                        HashSet<String> firstSet = grammar.computeFirst(item.getProductions(),item.getDotPointer()+1);
                        if(firstSet.contains(Grammar.Epsilon)){
                            firstSet.remove(Grammar.Epsilon);
                            firstSet.addAll(item.getLookAHead());
                        }

                        lookahead.addAll(firstSet);
                    }

                    HashSet<Production> productions = grammar.getProductionsOf(item.getCurrent());
                    for(Production production : productions){
                        String[] rhs = production.getProductions();

                        int finished = 0;
                        if (rhs.length == 1 && rhs[0].equals(Grammar.Epsilon)) {
                            finished = 1;
                        }

                        HashSet<String> newLA = new HashSet<String>(lookahead);
                        LR1Item newItem = new LR1Item(production.getVariable(), rhs, finished, newLA);

                        //Merge lookaheads with existing item
                        boolean found = false;
                        for (LR1Item existingItem : m_Items) {
                            if (newItem.equalLR0(existingItem)) {
                                HashSet<String> existLA = existingItem.getLookAHead();
                                if (!existLA.containsAll(newLA)) {
                                    //Changing the lookahead will change the hash code
                                    //of the item, which means it must be re-added.
                                    m_Items.remove(existingItem);
                                    existLA.addAll(newLA);
                                    m_Items.add(existingItem);
                                    changeFlag = true;
                                }

                                found = true;
                                break;
                            }
                        }

                        if (!found) {
                            m_Items.add(newItem);
                            changeFlag = true;
                        }
                    }

                    if (changeFlag) break;
                }
            }
        } while (changeFlag);
    }

    public HashMap<String, LR1State> getTransitions() {
        return m_Transitions;
    }

    public LinkedHashSet<LR1Item> getItems() {
        return m_Items;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for(LR1Item item: m_Items){
            s.append(item).append("\n");
        }
        return s.toString();
    }
}

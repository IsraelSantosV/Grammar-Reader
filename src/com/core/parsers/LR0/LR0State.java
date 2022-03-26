package com.core.parsers.LR0;

import com.core.Grammar;
import com.core.Production;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;

public class LR0State {

    LinkedHashSet<LR0Item> m_Items;
    HashMap<String, LR0State> m_Transitions;

    public LR0State(Grammar grammar, HashSet<LR0Item> coreItems){
        m_Items = new LinkedHashSet<>(coreItems);
        m_Transitions = new HashMap<>();
        closure(grammar);
    }

    private void closure(Grammar grammar){
        boolean changeFlag = false;

        do {
            changeFlag = false;
            HashSet<LR0Item> temporary = new HashSet<>();
            for(LR0Item item : m_Items){
                if(item.getCurrentTerminal() != null && grammar.isVariable(item.getCurrentTerminal())){
                    HashSet<Production> productions = grammar.getProductionsOf(item.getCurrentTerminal());
                    temporary.addAll(createLR0Item(productions));
                }
            }

            if(!m_Items.containsAll(temporary)){
                m_Items.addAll(temporary);
                changeFlag = true;
            }
        } while(changeFlag);
    }

    private HashSet<LR0Item> createLR0Item(HashSet<Production> productions){
        HashSet<LR0Item> results = new HashSet<>();
        for(Production production : productions){
            results.add(new LR0Item(production));
        }

        return results;
    }

    public void addTransition(String id, LR0State state){
        m_Transitions.put(id, state);
    }

    public HashSet<LR0Item> getItems() { return m_Items; }

    public HashMap<String, LR0State> getTransitions() { return m_Transitions; }

    @Override
    public int hashCode(){
        int hash = 7;
        hash = 83 * hash + Objects.hashCode(m_Items);
        hash = 83 * hash + Objects.hashCode(m_Transitions);
        return hash;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if(obj == null) return false;
        if(getClass() != obj.getClass()) return false;

        final LR0State otherState = (LR0State) obj;
        if(!(m_Items.containsAll(otherState.m_Items) && otherState.m_Items.containsAll(m_Items))) return false;
        return Objects.equals(m_Transitions, otherState.m_Transitions);
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        for(LR0Item item : m_Items){
            str.append(item).append("\n");
        }

        return str.toString();
    }

}

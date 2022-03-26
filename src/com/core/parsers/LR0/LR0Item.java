package com.core.parsers.LR0;

import com.core.Grammar;
import com.core.Production;

import java.util.Arrays;
import java.util.Objects;

public class LR0Item extends Production {

    protected int m_DotPointer;

    public LR0Item(Production production){
        super(production.getVariable(), production.getProductions());
        int finished = 0;
        if(production.getProductions().length == 1 && production.getProductions()[0].equals(Grammar.Epsilon)){
            finished = 1;
        }

        m_DotPointer = finished;
    }

    public LR0Item(String variable, String[] productions, int dotPointer) {
        super(variable, productions);
        m_DotPointer = dotPointer;
    }

    public LR0Item(LR0Item item){
        super(item);
        m_DotPointer = item.getDotPointer();
    }

    public int getDotPointer() { return m_DotPointer; }

    public boolean goTo(){
        if(m_DotPointer >= getProductions().length){
            return false;
        }

        m_DotPointer++;
        return true;
    }

    public String getCurrentTerminal(){
        if(m_DotPointer == getProductions().length){
            return null;
        }

        return getProductions()[m_DotPointer];
    }

    @Override
    public int hashCode(){
        int hash = 7;
        hash = 89 * hash + m_DotPointer;
        hash = 89 * hash + Objects.hashCode(getVariable());
        hash = 89 * hash + Arrays.deepHashCode(getProductions());
        return hash;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if(obj == null) return false;
        if(getClass() != obj.getClass()) return false;

        final LR0Item otherItem = (LR0Item) obj;
        if(m_DotPointer != otherItem.m_DotPointer) return false;
        if(!getVariable().equals(otherItem.getVariable())) return false;
        return Arrays.equals(getProductions(), otherItem.getProductions());
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder(getVariable() + " -> ");
        for(int i = 0; i < getProductions().length; i++){
            if(i == m_DotPointer){
                str.append(".");
            }

            str.append(getProductions()[i]);
            if(i != getProductions().length - 1){
                str.append(" ");
            }
        }

        if(getProductions().length == m_DotPointer){
            str.append(".");
        }

        return str.toString();
    }
}

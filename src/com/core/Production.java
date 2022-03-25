package com.core;

import java.util.Arrays;
import java.util.Objects;

public class Production {

    private String m_Variable;
    private String[] m_Productions;

    public Production(String variable, String[] productions){
        setVariable(variable);
        setProductions(productions);
    }

    public Production(Production production){
        setVariable(production.getVariable());
        setProductions(production.getProductions());
    }

    public String getVariable() { return m_Variable; }
    public String[] getProductions() { return m_Productions; }

    public void setVariable(String variable){
        m_Variable = variable;
    }

    public void setProductions(String[] productions){
        m_Productions = productions;
    }

    @Override
    public int hashCode(){
        int hash = 3;
        hash = 29 * hash + Objects.hashCode(m_Variable);
        hash = 29 * hash + Arrays.deepHashCode(m_Productions);
        return hash;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if(obj == null) return false;
        if(getClass() != obj.getClass()) return false;

        final Production otherProduction = (Production) obj;
        if(!Objects.equals(m_Variable, otherProduction.m_Variable)) return false;
        return Arrays.deepEquals(m_Productions, otherProduction.m_Productions);
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder(m_Variable + " -> ");
        for (String m_production : m_Productions) {
            str.append(m_production).append(" ");
        }

        return str.toString();
    }
}

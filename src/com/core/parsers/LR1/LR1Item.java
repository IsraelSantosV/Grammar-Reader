package com.core.parsers.LR1;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class LR1Item {

    private HashSet<String> m_LookAHead;
    private final String m_Variable;
    private String[] m_Productions;
    private int m_DotPointer;

    public LR1Item(String leftSide, String[] rightSide, int dotPointer, HashSet<String> lookahead){
        m_Variable = leftSide;
        m_Productions = rightSide;
        m_DotPointer = dotPointer;
        m_LookAHead = lookahead;
    }

    public String getCurrent(){
        if(m_DotPointer == m_Productions.length){
            return null;
        }

        return m_Productions[m_DotPointer];
    }

    boolean goTo() {
        if (m_DotPointer >= m_Productions.length) {
            return false;
        }

        m_DotPointer++;
        return true;
    }

    public int getDotPointer() {
        return m_DotPointer;
    }

    public String[] getProductions() {
        return m_Productions;
    }

    public HashSet<String> getLookAHead() {
        return m_LookAHead;
    }

    public String getVariable() {
        return m_Variable;
    }

    public void setLookAHead(HashSet<String> m_LookAHead) {
        this.m_LookAHead = m_LookAHead;
    }

    public void setProductions(String[] m_Productions) {
        this.m_Productions = m_Productions;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        LR1Item lr1Item = (LR1Item) other;
        return m_DotPointer == lr1Item.m_DotPointer &&
                Objects.equals(m_LookAHead, lr1Item.m_LookAHead) &&
                Objects.equals(m_Variable, lr1Item.m_Variable) &&
                Arrays.equals(m_Productions, lr1Item.m_Productions);
    }

    public boolean equalLR0(LR1Item item){
        return m_Variable.equals(item.getVariable()) &&
                Arrays.equals(m_Productions,item.getProductions()) &&
                m_DotPointer == item.getDotPointer();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + this.m_DotPointer;
        hash = 31 * hash + Objects.hashCode(this.m_Variable);
        hash = 31 * hash + Arrays.deepHashCode(this.m_Productions);
        hash = 31 * hash + Objects.hashCode(this.m_LookAHead);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder(m_Variable + " -> ");
        for (int i = 0; i < m_Productions.length; i++) {
            if (i == m_DotPointer) {
                str.append(".");
            }
            str.append(m_Productions[i]);
            if(i != m_Productions.length - 1){
                str.append(" ");
            }
        }

        if (m_Productions.length == m_DotPointer) {
            str.append(".");
        }

        str.append(" , ").append(m_LookAHead);
        return str.toString();
    }
}

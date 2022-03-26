package com.core.parsers.util;

public class Action {

    private final ActionType m_Type;
    private final int m_Operand;

    public Action(ActionType type, int operand){
        m_Type = type;
        m_Operand = operand;
    }

    @Override
    public String toString(){
        return m_Type + " " + (m_Type == ActionType.ACCEPT ? "" : m_Operand);
    }

    public ActionType getType() { return m_Type; }

    public int getOperand() { return m_Operand; }

}

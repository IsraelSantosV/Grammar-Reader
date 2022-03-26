package com.core.parsers;

import com.core.Grammar;
import com.core.Production;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ParserLL1 {

    protected HashMap<String, String>[] m_Table;
    protected Grammar m_Grammar;

    public ParserLL1(Grammar grammar){
        m_Grammar = grammar;
    }

    public Grammar getGrammar() { return m_Grammar; }

    public boolean parserLL1(){
        return createTableForLL1();
    }

    private boolean createTableForLL1(){
        m_Table = new HashMap[m_Grammar.getVariables().size()];
        for(int i = 0; i < m_Grammar.getVariables().size(); i++){
            m_Table[i] = new HashMap<>();
        }

        int currentLine = 0;
        for(String variable : m_Grammar.getVariables()){
            HashSet<Production> productions = m_Grammar.getProductionsOf(variable);
            for(Production production : productions){
                String firstProduction = production.getProductions()[0];
                HashSet<String> firstSet = new HashSet<>();

                if(m_Grammar.isVariable(firstProduction)){
                    firstSet = m_Grammar.getFirstSets().get(firstProduction);
                }
                else{
                    firstSet.add(firstProduction);
                }

                if(firstSet.contains(m_Grammar.getDefinition("EPSILON"))){
                    HashSet<String> followSetOfVariable = m_Grammar.getFollowSets().get(production.getVariable());
                    HashSet<String> temporary = new HashSet<>();

                    for(String str : followSetOfVariable){
                        if(!m_Grammar.isVariable(str)){
                            temporary.add(str);
                        }
                    }

                    for(String terminal : temporary){
                        if(m_Table[currentLine].containsKey(terminal)){
                            return false;
                        }

                        m_Table[currentLine].put(terminal, production.toString());
                    }
                }
                else{
                    //Set with firstSet
                    for(String terminal : firstSet){
                        if(m_Table[currentLine].containsKey(terminal)){
                            return false;
                        }

                        m_Table[currentLine].put(terminal, production.toString());
                    }
                }
            }

            currentLine++;
        }

        return true;
    }

    public String getTableString(){
        StringBuilder str = new StringBuilder("Collection: ");
        HashSet<String> terminals = new HashSet<>(m_Grammar.getTerminals());
        terminals.add(m_Grammar.getDefinition("ROOT_GRAMMAR_SYMBOL"));
        str.append(terminals).append("\n\n");

        int currentLine = 0;
        for(String variable : m_Grammar.getVariables()){
            str.append(variable).append(": ");

            String currentKey = "";
            for(Map.Entry<String, String> entry : m_Table[currentLine].entrySet()){
                if(currentKey.equals("")) currentKey = entry.getKey();

                str.append(entry.getKey()).append(": ").append(entry.getValue());
                str.append("   ");

                currentKey = entry.getKey();
            }

            str.append("\n");
            currentLine++;
        }

        return str.toString();
    }

}

package com.core;

import com.tools.FileResourceUtils;
import org.json.JSONObject;

import java.util.*;

public class Grammar {

    private final List<Production> m_Productions;
    private final HashSet<String> m_Terminals;
    private final HashSet<String> m_Variables;
    private String m_StartVariable;
    private HashMap<String, HashSet<String>> m_FirstSets;
    private HashMap<String, HashSet<String>> m_FollowSets;

    private final boolean m_IsExtendedGrammar;
    private String m_ExtendedGrammarSymbol;
    private final JSONObject m_Definitions;

    public Grammar(String grammarText, boolean extendGrammar){
        m_Productions = new ArrayList<>();
        m_Terminals = new HashSet<>();
        m_Variables = new HashSet<>();
        m_IsExtendedGrammar = extendGrammar;

        m_Definitions = FileResourceUtils.readJson(Main.SYNTAX_DEFINITIONS_FILE);
        if(extendGrammar){
            m_ExtendedGrammarSymbol = m_Definitions.getString("EXTEND_GRAMMAR_SYMBOL");
        }

        int currentLine = 0;
        for(String value : grammarText.split(",")){
            String[] sides = value.split("->");
            String rootSymbol = sides[0].trim();
            String[] productions = sides[1].trim().split("\\|");

            m_Variables.add(rootSymbol);

            for(String production : productions){
                String[] rightSide = production.trim().split("\\s+");
                for(String terminal : rightSide){
                    if(!terminal.equals(m_Definitions.getString("EPSILON"))){
                        m_Terminals.add(terminal);
                    }
                }

                if(currentLine == 0){
                    m_StartVariable = rootSymbol;

                    if(extendGrammar) {
                        m_Productions.add(new Production(m_ExtendedGrammarSymbol, new String[]{m_StartVariable}));
                    }
                }

                m_Productions.add(new Production(rootSymbol, rightSide));
                currentLine++;
            }
        }

        for(String variable : m_Variables){
            m_Terminals.remove(variable);
        }

        //Print current productions
        System.out.println("Productions: ");
        for(int i = 0; i < m_Productions.size(); i++){
            System.out.println(i + " : " + m_Productions.get(i));
        }

        calculateFirstSets();
        calculateFollowSets();
    }

    private void calculateFirstSets(){
        m_FirstSets = new HashMap<>();

        for(String variable : m_Variables){
            HashSet<String> temporary = new HashSet<>();
            m_FirstSets.put(variable, temporary);
        }

        while(true){
            boolean isChanged = false;

            for(String variable : m_Variables){
                HashSet<String> firstSet = new HashSet<>();

                for(Production production : m_Productions){
                    if(production.getVariable().equals(variable)){
                        HashSet<String> addAll = computeFirst(production.getProductions(), 0);
                        firstSet.addAll(addAll);
                    }
                }

                if(!m_FirstSets.get(variable).containsAll(firstSet)){
                    isChanged = true;
                    m_FirstSets.get(variable).addAll(firstSet);
                }
            }

            if(!isChanged) break;
        }

        if(m_IsExtendedGrammar){
            m_FirstSets.put(m_ExtendedGrammarSymbol, m_FirstSets.get(m_StartVariable));
        }
    }

    private void calculateFollowSets(){
        m_FollowSets = new HashMap<>();

        for(String variable : m_Variables){
            HashSet<String> temporary = new HashSet<>();
            m_FollowSets.put(variable, temporary);
        }

        HashSet<String> start = new HashSet<>();
        start.add(m_Definitions.getString("ROOT_GRAMMAR_SYMBOL"));

        if(m_IsExtendedGrammar){
            m_FollowSets.put(m_ExtendedGrammarSymbol, start);
        }
        else{
            m_FollowSets.put(m_StartVariable, start);
        }

        while(true){
            boolean isChange = false;

            for(String variable : m_Variables){
                for(Production production : m_Productions){
                    for(int i = 0; i < production.getProductions().length; i++){
                        if(production.getProductions()[i].equals(variable)){
                            HashSet<String> first;

                            if(i == production.getProductions().length - 1){
                                first = m_FollowSets.get(production.getVariable());
                            }
                            else{
                                first = computeFirst(production.getProductions(), i + 1);
                                if(first.contains(m_Definitions.getString("EPSILON"))){
                                    first.remove(m_Definitions.getString("EPSILON"));
                                    first.addAll(m_FollowSets.get(production.getVariable()));
                                }
                            }

                            if(!m_FollowSets.get(variable).containsAll(first)){
                                isChange = true;
                                m_FollowSets.get(variable).addAll(first);
                            }
                        }
                    }
                }
            }

            if(!isChange) break;
        }
    }

    public HashSet<String> computeFirst(String[] productions, int index){
        HashSet<String> first = new HashSet<>();
        if(index == productions.length) return first;

        if(m_Terminals.contains(productions[index])
                || productions[index].equals(m_Definitions.getString("EPSILON"))){
            first.add(productions[index]);
            return first;
        }

        if(m_Variables.contains(productions[index])){
            first.addAll(m_FirstSets.get(productions[index]));
        }

        if(first.contains(m_Definitions.getString("EPSILON"))){
            if(index != productions.length - 1){
                first.remove(m_Definitions.getString("EPSILON"));
                first.addAll(computeFirst(productions, index + 1));
            }
        }

        return first;
    }

    public HashSet<Production> getProductionsOf(String variable){
        HashSet<Production> variableProductions = new HashSet<>();
        for(Production production : m_Productions){
            if(production.getVariable().equals(variable)){
                variableProductions.add(production);
            }
        }

        return variableProductions;
    }

    @Override
    public int hashCode(){
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(m_Productions);
        hash = 37 * hash + Objects.hashCode(m_Terminals);
        hash = 37 * hash + Objects.hashCode(m_Variables);
        return hash;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj) return true;
        if(obj == null) return false;
        if(getClass() != obj.getClass()) return false;

        final Grammar otherGrammar = (Grammar) obj;
        if(!Objects.equals(m_Productions, otherGrammar.m_Productions)) return false;
        if(!Objects.equals(m_Terminals, otherGrammar.m_Terminals)) return false;
        return Objects.equals(m_Variables, otherGrammar.m_Variables);
    }

    @Override
    public String toString(){
        StringBuilder str = new StringBuilder();
        for(Production production : m_Productions){
            str.append(production).append("\n");
        }

        return str.toString();
    }

    public boolean isVariable(String variable) { return m_Variables.contains(variable); }

    public HashMap<String, HashSet<String>> getFirstSets() { return m_FirstSets; }

    public HashMap<String, HashSet<String>> getFollowSets() { return m_FollowSets; }

    public HashSet<String> getTerminals() { return m_Terminals; }

    public HashSet<String> getVariables() { return m_Variables; }

    public List<Production> getProductions() { return m_Productions; }

}

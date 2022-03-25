package com.core;

import java.util.*;

public abstract class Parser {

    protected final int INVALID_INDEX = -1;
    protected Syntax m_Syntax;

    private Map<Character, Set<Character>> m_FirstSets;
    private Map<Character, Set<Character>> m_FollowSets;

    public Parser(Syntax syntax){
        m_Syntax = syntax;

        //Calculate all firstSets and followSets
        m_FirstSets = new HashMap<>();
        m_FollowSets = new HashMap<>();
        Set<Syntax.Symbol> terminals = m_Syntax.getAllTerminalSymbols();
        List<Syntax.Symbol> notTerminals = m_Syntax.getAllProductionSymbols();

        terminals.forEach(this::internalSetFirstSetOf);
        notTerminals.forEach(this::internalSetFirstSetOf);
        notTerminals.forEach(this::internalSetFollowSetOf);

        initializeTable();
    }

    protected abstract  void initializeTable();
    public abstract void createTable();
    public abstract void saveTable();
    protected abstract String getSaveFileName();

    public Set<Character> getFirstSetOf(char token){
        if(m_FirstSets.get(token) == null){
            return null;
        }

        return new HashSet<>(m_FirstSets.get(token));
    }

    public Set<Character> getFollowSetOf(char token){
        if(m_FollowSets.get(token) == null){
            return null;
        }

        return new HashSet<>(m_FollowSets.get(token));
    }

    protected void internalSetFirstSetOf(Syntax.Symbol symbol){
        if(getFirstSetOf(symbol.Token) != null) return;
        Set<Character> firstSet = new HashSet<>();

        //If is a terminal, then first(a)={a}
        if(symbol.IsTerminal) {
            firstSet.add(symbol.Token);
        }
        else {
            //If A->Y1 Y2 Y3....Yn is a production:
            Syntax.Symbol[] firstSymbols = m_Syntax.getFirstSymbolsFromRule(symbol.Token);
            for (int i = 0; i < firstSymbols.length; i++) {
                Syntax.Symbol firstSymbol = firstSymbols[i];

                if (firstSymbol.IsTerminal) {
                    firstSet.add(firstSymbol.Token);
                }
                else if (firstSymbol.Token != symbol.Token) {
                    if(getFirstSetOf(firstSymbol.Token) == null){
                        internalSetFirstSetOf(firstSymbol);
                    }

                    Set<Character> firstOfCurrentSymbol = getFirstSetOf(firstSymbol.Token);

                    // First(A) = first(Y1)
                    firstSet.addAll(firstOfCurrentSymbol);

                    // If first(Y1) contains Є then
                    //first(A)={first(Y1)–Є} U {first(Y2)}
                    if (firstOfCurrentSymbol.contains(m_Syntax.getVoidSymbol())) {
                        firstSet.remove(m_Syntax.getVoidSymbol());

                        //First(Y2)
                        Syntax.Symbol nextSymbol = m_Syntax.getNextSymbolOfToken(symbol.Token, i, firstSymbol.Token);
                        if (nextSymbol != null) {
                            if(getFirstSetOf(nextSymbol.Token) == null){
                                internalSetFirstSetOf(nextSymbol);
                            }

                            Set<Character> firstsOfNextSymbol = getFirstSetOf(nextSymbol.Token);
                            firstSet.addAll(firstsOfNextSymbol);
                        }
                    }
                }
            }
        }

        m_FirstSets.put(symbol.Token, firstSet);
    }

    protected void internalSetFollowSetOf(Syntax.Symbol symbol){
        if(getFollowSetOf(symbol.Token) != null) return;

        Set<Character> followSet = new HashSet<>();
        if(symbol.Token == m_Syntax.getInitialToken()){
            followSet.add(m_Syntax.getSpecialRuleCharacter());
            m_FollowSets.put(symbol.Token, followSet);
        }

        Map<Character, Syntax.OutputRule> allOccurrencesOfSymbol = m_Syntax.getAllRulesOccurrencesOf(symbol.Token);
        for (Map.Entry<Character, Syntax.OutputRule> entrySet : allOccurrencesOfSymbol.entrySet()) {
            Character currentKey = entrySet.getKey();
            Syntax.OutputRule outputRule = entrySet.getValue();

            for(int i = 0; i < outputRule.Output.size(); i++){
                Syntax.Symbol currentSymbol = outputRule.Output.get(i);

                if(currentSymbol.Token == symbol.Token){
                    //If a -> αBβ is a production, where α, B and β they are grammatical symbols any, then
                    //everything in the first (β) except є is in followers (B).
                    if(i < outputRule.Output.size() - 1){
                        //Exist next symbol
                        Syntax.Symbol nextSymbol = outputRule.Output.get(i+1);
                        if(getFirstSetOf(nextSymbol.Token) == null){
                            internalSetFirstSetOf(nextSymbol);
                        }

                        Set<Character> firstOfNextSymbol = getFirstSetOf(nextSymbol.Token);

                        followSet.addAll(firstOfNextSymbol);
                        followSet.remove(m_Syntax.getVoidSymbol());
                        m_FollowSets.put(symbol.Token, followSet);

                        //Followers(Root)
                        if(firstOfNextSymbol.contains(m_Syntax.getVoidSymbol())){
                            setFollowOfRootSymbol(symbol, followSet, currentKey);
                        }
                    }
                    //If a-> αB is a production, then everything in
                    //followers (a) is in followers (B).
                    else {
                        setFollowOfRootSymbol(symbol, followSet, currentKey);
                    }
                }
            }
        }
    }

    protected void setFollowOfRootSymbol(Syntax.Symbol symbol, Set<Character> followSet, Character currentKey) {
        Syntax.Symbol rootSymbol = new Syntax.Symbol(currentKey, false);
        if(getFollowSetOf(rootSymbol.Token) == null){
            internalSetFollowSetOf(rootSymbol);
        }

        Set<Character> followersOfRoot = getFollowSetOf(rootSymbol.Token);
        followSet.addAll(followersOfRoot);
        m_FollowSets.put(symbol.Token, followSet);
    }

}

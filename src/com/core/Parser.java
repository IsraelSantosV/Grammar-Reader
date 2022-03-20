package com.core;

import java.util.*;

public abstract class Parser {

    protected final int INVALID_INDEX = -1;
    protected Syntax m_Syntax;

    private Map<Syntax.Symbol, Set<Character>> m_FirstSets;
    private Map<Syntax.Symbol, Set<Character>> m_FollowSets;

    public Parser(Syntax syntax){
        m_Syntax = syntax;

        //Calculate all firstSets and followSets
        m_FirstSets = new HashMap<>();
        m_FollowSets = new HashMap<>();
        Set<Syntax.Symbol> terminals = m_Syntax.getAllSymbolsOf(true);
        Set<Syntax.Symbol> notTerminals = m_Syntax.getAllSymbolsOf(false);

        terminals.forEach(this::internalSetFirstSetOf);
        notTerminals.forEach(this::internalSetFirstSetOf);
        notTerminals.forEach(this::internalSetFollowSetOf);

        initializeTable();
    }

    protected abstract  void initializeTable();
    public abstract void createTable();
    public abstract void saveTable();
    protected abstract String getSaveFileName();

    public Set<Character> getFirstSetOf(Syntax.Symbol symbol){
        if(m_FirstSets.get(symbol) == null){
            return new HashSet<>();
        }

        return new HashSet<>(m_FirstSets.get(symbol));
    }

    public Set<Character> getFollowSetOf(Syntax.Symbol symbol){
        if(m_FollowSets.get(symbol) == null){
            return new HashSet<>();
        }

        return new HashSet<>(m_FollowSets.get(symbol));
    }

    protected void internalSetFirstSetOf(Syntax.Symbol symbol){
        if(getFirstSetOf(symbol).size() > 0) return;
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
                    if(getFirstSetOf(firstSymbol).size() <= 0){
                        internalSetFirstSetOf(firstSymbol);
                    }

                    Set<Character> firstOfCurrentSymbol = getFirstSetOf(firstSymbol);

                    // First(A) = first(Y1)
                    firstSet.addAll(firstOfCurrentSymbol);

                    // If first(Y1) contains Є then
                    //first(A)={first(Y1)–Є} U {first(Y2)}
                    if (firstOfCurrentSymbol.contains(m_Syntax.getVoidSymbol())) {
                        firstSet.remove(m_Syntax.getVoidSymbol());

                        //First(Y2)
                        Syntax.Symbol nextSymbol = m_Syntax.getNextSymbolOfToken(symbol.Token, i, firstSymbol.Token);
                        if (nextSymbol != null) {
                            if(getFirstSetOf(nextSymbol).size() <= 0){
                                internalSetFirstSetOf(nextSymbol);
                            }

                            Set<Character> firstsOfNextSymbol = getFirstSetOf(nextSymbol);
                            firstSet.addAll(firstsOfNextSymbol);
                        }
                    }
                }
            }
        }

        m_FirstSets.put(symbol, firstSet);
    }

    protected void internalSetFollowSetOf(Syntax.Symbol symbol){
        if(getFollowSetOf(symbol).size() > 0) return;

        Set<Character> followSet = new HashSet<>();
        if(symbol.Token == m_Syntax.getInitialToken()){
            followSet.add(m_Syntax.getSpecialRuleCharacter());
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
                        Set<Character> firstOfNextSymbol = getFirstSetOf(nextSymbol);

                        followSet.addAll(firstOfNextSymbol);
                        followSet.remove(m_Syntax.getVoidSymbol());

                        //Followers(Root)
                        if(firstOfNextSymbol.contains(m_Syntax.getVoidSymbol())){
                            Syntax.Symbol rootSymbol = new Syntax.Symbol(currentKey, false);
                            if(getFollowSetOf(rootSymbol).size() <= 0){
                                internalSetFollowSetOf(rootSymbol);
                            }

                            Set<Character> followersOfRoot = getFollowSetOf(rootSymbol);
                            followSet.addAll(followersOfRoot);
                        }
                    }
                    //If a-> αB is a production, then everything in
                    //followers (a) is in followers (B).
                    else {
                        Syntax.Symbol rootSymbol = new Syntax.Symbol(currentKey, false);
                        //if(getFollowSetOf(rootSymbol).size() <= 0){
                            //internalSetFollowSetOf(rootSymbol);
                        //}

                        Set<Character> followersOfRootSymbol = getFollowSetOf(rootSymbol);
                        followSet.addAll(followersOfRootSymbol);
                    }
                }
            }
        }

        m_FollowSets.put(symbol, followSet);
    }

}

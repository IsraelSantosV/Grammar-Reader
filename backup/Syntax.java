package com.core;

import com.tools.FileResourceUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class Syntax {

    private char END_LINE_SYMBOL;
    private char START_RULES_SYMBOL;
    private char VOID_SYMBOL;
    private char SEPARATE_RULES_SYMBOL;
    private char SPECIAL_RULE_CHARACTER;
    private char EXTEND_GRAMMAR_SYMBOL;

    private char m_InitialGrammarToken;
    private boolean m_GrammarIsExtended;

    public char getEndLineSymbol() { return END_LINE_SYMBOL; }
    public char getStartRuleSymbol() { return START_RULES_SYMBOL; }
    public char getVoidSymbol() { return VOID_SYMBOL; }
    public char getSeparateRulesSymbol() { return SEPARATE_RULES_SYMBOL; }
    public char getSpecialRuleCharacter() { return SPECIAL_RULE_CHARACTER; }
    public char getExtendGrammarSymbol() { return EXTEND_GRAMMAR_SYMBOL; }

    public static class Symbol implements Comparable<Symbol> {
        public char Token;
        public boolean IsTerminal;

        public int NotTerminalIndex;

        public Symbol(char token, boolean isTerminal){
            Token = token;
            IsTerminal = isTerminal;
        }

        @Override
        public String toString() {
            return String.valueOf(Token);
        }

        @Override
        public int compareTo(Symbol other) {
            if(IsTerminal && other.IsTerminal) return 0;
            return NotTerminalIndex > other.NotTerminalIndex ? 1 : -1;
        }
    }

    public static class OutputRule {
        public List<Symbol> Output;

        public OutputRule(){
            Output = new ArrayList<>();
        }

        public boolean hasProductionInOutput(char[] production, boolean fullValidation){
            char[] output = convertOutputInTokens();

            int validFields = 0;
            for (int i = 0; i < output.length && i < production.length; i++){
                if(output[i] == production[validFields]){
                    validFields++;
                }
                else if(fullValidation && validFields > 0){
                    return false;
                }
            }

            return validFields >= production.length;
        }

        public char[] convertOutputInTokens(){
            char[] result = new char[Output.size()];
            for (int i = 0; i < Output.size(); i++){
                result[i] = Output.get(i).Token;
            }

            return result;
        }

        @Override
        public String toString() {
            StringBuilder result = new StringBuilder();
            for (Symbol symbol : Output) {
                result.append(symbol.toString());
            }

            return result.toString();
        }
    }

    private Map<Symbol, List<OutputRule>> m_Rules;

    public Syntax(String inputStream){
        initializeSyntaxDefinitions();
        initializeSymbols(inputStream);
        System.out.println("Syntax is ready!");
    }

    private void initializeSyntaxDefinitions(){
        JSONObject conf = FileResourceUtils.readJson(Main.SYNTAX_DEFINITIONS_FILE);
        try {
            String endLineSymbol = conf.getString("End_Line_Symbol");
            String startLineSymbol = conf.getString("Start_Rule_Symbol");
            String voidSymbol = conf.getString("Void_Symbol");
            String separateRulesSymbol = conf.getString("Separate_Rules_Symbol");
            String specialRuleCharacter = conf.getString("Special_Rule_Character");
            String extendGrammarSymbol = conf.getString("Extend_Grammar_Symbol");

            END_LINE_SYMBOL = endLineSymbol.charAt(0);
            START_RULES_SYMBOL = startLineSymbol.charAt(0);
            VOID_SYMBOL = voidSymbol.charAt(0);
            SEPARATE_RULES_SYMBOL = separateRulesSymbol.charAt(0);
            SPECIAL_RULE_CHARACTER = specialRuleCharacter.charAt(0);
            EXTEND_GRAMMAR_SYMBOL = extendGrammarSymbol.charAt(0);
        } catch (JSONException e) {
            throw new JSONException("Error on read json definition: " + e.getMessage());
        }
    }

    protected void initializeSymbols(String inputStream){
        m_Rules = new LinkedHashMap<>();
        String validInput = inputStream.replaceAll("\\s+","");
        char[] tokens = validInput.toCharArray();

        m_InitialGrammarToken = tokens[0];
        boolean isStartRuleToken = true;
        Symbol currentRuleSymbol = null;
        OutputRule currentOutputRule = null;

        for (int i = 0; i < tokens.length; i++) {
            char token = tokens[i];

            if (token == START_RULES_SYMBOL) {
                isStartRuleToken = false;
            } else if (token == END_LINE_SYMBOL) {
                isStartRuleToken = true;
                currentOutputRule = null;
                currentRuleSymbol = null;
            } else if (token == SEPARATE_RULES_SYMBOL) {
                currentOutputRule = null;
            } else {
                //Receive valid language token
                if (Character.isDigit(token)) {
                    throw new IllegalArgumentException("The syntax does not allow digits!");
                }

                if (token == EXTEND_GRAMMAR_SYMBOL) {
                    m_GrammarIsExtended = true;
                }

                boolean isTerminal = token == VOID_SYMBOL || !Character.isUpperCase(token);
                Symbol newSymbol = new Symbol(token, isTerminal);

                if (!isTerminal) {
                    newSymbol.NotTerminalIndex = i;
                }

                if (isStartRuleToken) {
                    List<OutputRule> lastRule = getOutputRule(token);
                    if (lastRule != null) {
                        currentOutputRule = new OutputRule();
                        lastRule.add(currentOutputRule);
                    } else {
                        m_Rules.put(newSymbol, new ArrayList<>());
                    }

                    isStartRuleToken = false;
                    currentRuleSymbol = newSymbol;
                } else {
                    try {
                        if (currentRuleSymbol == null) throw new AssertionError();
                        List<OutputRule> lineTargetRules = getOutputRule(currentRuleSymbol.Token);
                        if (currentOutputRule == null) {
                            currentOutputRule = new OutputRule();
                            lineTargetRules.add(currentOutputRule);
                        }

                        currentOutputRule.Output.add(newSymbol);
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Error on create syntax: " + e.getMessage());
                    }
                }
            }
        }
    }

    public char getInitialToken(){
        return m_InitialGrammarToken;
    }

    public void extendGrammar(){
        if(m_GrammarIsExtended) return;
        Map<Symbol, List<OutputRule>> rulesCopy = new LinkedHashMap<>(m_Rules);

        OutputRule newOutput = new OutputRule();
        newOutput.Output.add(new Symbol(getInitialToken(), false));
        List<OutputRule> newRule = new ArrayList<>();
        newRule.add(newOutput);

        rulesCopy.put(new Symbol(getExtendGrammarSymbol(), false), newRule);
        m_InitialGrammarToken = getExtendGrammarSymbol();

        m_Rules = rulesCopy;
        m_GrammarIsExtended = true;
    }

    public void printSyntax(){
        m_Rules.forEach((key, value) -> System.out.println(key + " -> " + value));
    }

    public void printValues(boolean onlyTerminals){
        StringBuilder terminals = new StringBuilder();
        for (List<OutputRule> value : m_Rules.values()) {
            for (OutputRule outputRule : value) {
                for (Symbol symbol : outputRule.Output) {
                    if(onlyTerminals && symbol.IsTerminal){
                        terminals.append(symbol.Token).append(", ");
                    }
                    else if(!onlyTerminals && !symbol.IsTerminal){
                        terminals.append(symbol.Token).append(", ");
                    }
                }
            }
        }

        System.out.println(terminals);
    }

    public List<OutputRule> getOutputRule(char token){
        for (Symbol keySet : m_Rules.keySet()){
            if(keySet.Token == token){
                return m_Rules.get(keySet);
            }
        }

        return null;
    }

    public boolean hasProductionInRule(char ruleToken, char[] targetProduction){
        List<OutputRule> rules = getOutputRule(ruleToken);
        if(rules == null || rules.size() <= 0) return false;

        for(OutputRule targetRule : rules){
            if(targetRule.hasProductionInOutput(targetProduction, true)){
                return true;
            }
        }

        return false;
    }

    public Symbol[] getFirstSymbolsFromRule(char ruleToken){
        List<OutputRule> rules = getOutputRule(ruleToken);
        if(rules == null || rules.size() <= 0) return new Symbol[]{ };

        Symbol[] array = new Symbol[rules.size()];
        for(int i = 0; i < rules.size(); i++){
            array[i] = rules.get(i).Output.get(0);
        }

        return array;
    }

    public Symbol getNextSymbolOfToken(char ruleToken, int targetRule, char token){
        List<OutputRule> rules = getOutputRule(ruleToken);
        if(rules == null || rules.size() <= 0 || targetRule < 0 || targetRule >= rules.size()) return null;

        OutputRule rule = rules.get(targetRule);
        for(int i = 0; i < rule.Output.size(); i++){
            Symbol current = rule.Output.get(i);
            if(current.Token == token){
                return i < rule.Output.size() - 1 ? rule.Output.get(i+1) : null;
            }
        }

        return null;
    }

    public Map<Character, OutputRule> getAllRulesOccurrencesOf(char token){
        Map<Character, OutputRule> allOccurrences = new HashMap<>();
        for (Map.Entry<Symbol, List<OutputRule>> entry : m_Rules.entrySet()) {
            Symbol currentKey = entry.getKey();
            for (OutputRule outputRule : entry.getValue()) {
                for (Symbol symbol : outputRule.Output) {
                    if(symbol.Token == token){
                        allOccurrences.put(currentKey.Token, outputRule);
                    }
                }
            }
        }

        return allOccurrences;
    }

    public Set<Character> getAllTerminalSymbols(Set<Character> list){
        Set<Character> resultValues = new HashSet<>();
        for (Character character : list) {
            if(!Character.isUpperCase(character)){
                resultValues.add(character);
            }
        }

        return resultValues;
    }

    public Set<Symbol> getAllTerminalSymbols(){
        Set<Symbol> resultValues = new HashSet<>();

        //For each terminal
        for (Map.Entry<Symbol, List<OutputRule>> entry : m_Rules.entrySet()) {
            for (OutputRule outputRule : entry.getValue()) {
                for (Symbol symbol : outputRule.Output) {
                    if (symbol.IsTerminal && !containsTokenInSet(resultValues, symbol.Token)) {
                        resultValues.add(symbol);
                    }
                }
            }
        }

        return resultValues;
    }

    public List<Symbol> getAllProductionSymbols() {
        Set<Symbol> keys = m_Rules.keySet();

        //Convert set to list
        List<Symbol> notTerminalSymbols = new ArrayList<>(keys);
        Collections.sort(notTerminalSymbols);
        return notTerminalSymbols;
    }

    public boolean containsTokenInSet(Set<Symbol> symbols, char token){
        for (Symbol symbol : symbols) {
            if(symbol.Token == token) return true;
        }

        return false;
    }

}

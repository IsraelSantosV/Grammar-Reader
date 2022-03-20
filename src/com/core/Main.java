package com.core;

import com.tools.FileResourceUtils;

public class Main {

    public static final String INPUT_FILE = "Input.txt";
    public static final String SYNTAX_DEFINITIONS_FILE = "SyntaxDefinitions.json";

    public static void main(String[] args) {
        String input = FileResourceUtils.getInstance().getFileFromResourceAsString(INPUT_FILE);
        Syntax newSyntax = new Syntax(input);

        ParserLL1 parserLL1 = new ParserLL1(newSyntax);
        parserLL1.createTable();
    }
}

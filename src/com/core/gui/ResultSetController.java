package com.core.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class ResultSetController implements Initializable {

    @FXML
    private TextField m_Input;

    @FXML
    private Label m_Result;

    @FXML
    private TextArea m_OutputValue;

    @FXML
    private void onClickGrammar(ActionEvent event){
        if(GrammarInputController.getSelectedParser().equals("LR(0)")
            || GrammarInputController.getSelectedParser().equals("SLR(1)")){
            m_OutputValue.setText(GrammarInputController.getCurrentParserLR0().getGrammar() + "");
        }
        else {
            m_OutputValue.setText(GrammarInputController.getCurrentParserLR1().getGrammar() + "");
        }
    }

    @FXML
    private void onClickFirst(ActionEvent event){
        if(GrammarInputController.getSelectedParser().equals("LR(0)")
            || GrammarInputController.getSelectedParser().equals("SLR(1)")){
            StringBuilder str = new StringBuilder();
            for(String s : GrammarInputController.getCurrentParserLR0().getGrammar().getFirstSets().keySet()){
                str.append(s).append(" : ").append(GrammarInputController.getCurrentParserLR0().getGrammar().getFirstSets().get(s)).append("\n");
            }

            m_OutputValue.setText(str.toString());
        }
    }

    @FXML
    private void onClickFollow(ActionEvent event){

    }

    @FXML
    private void onClickState(ActionEvent event){

    }

    @FXML
    private void onClickGoTo(ActionEvent event){

    }

    @FXML
    private void onClickAction(ActionEvent event){

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}

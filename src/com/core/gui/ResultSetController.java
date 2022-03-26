package com.core.gui;

import com.core.Main;
import com.tools.FileResourceUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

public class ResultSetController implements Initializable {

    @FXML
    private TextField m_Input;

    @FXML
    private Label m_Result;

    @FXML
    private TextArea m_OutputValue;

    @FXML
    private Label m_InputTextLabel;

    @FXML
    public Label Copyright;

    private JSONObject m_Translation;

    @FXML
    private void onClickGrammar(ActionEvent event){
        if(GrammarInputController.getSelectedParser().equals("LL(1)")){
            m_OutputValue.setText(GrammarInputController.getCurrentParserLL1().getGrammar() + "");
        }
        else if(GrammarInputController.getSelectedParser().equals("LR(0)")
            || GrammarInputController.getSelectedParser().equals("SLR(1)")){
            m_OutputValue.setText(GrammarInputController.getCurrentParserLR0().getGrammar() + "");
        }
        else {
            m_OutputValue.setText(GrammarInputController.getCurrentParserLR1().getGrammar() + "");
        }
    }

    @FXML
    private void onClickFirst(ActionEvent event){
        StringBuilder str = new StringBuilder();
        if(GrammarInputController.getSelectedParser().equals("LL(1)")){
            for(String s : GrammarInputController.getCurrentParserLL1().getGrammar().getFirstSets().keySet()){
                str.append(s).append(" : ").append(GrammarInputController.getCurrentParserLL1().getGrammar().getFirstSets().get(s)).append("\n");
            }

            m_OutputValue.setText(str.toString());
        }
        else if(GrammarInputController.getSelectedParser().equals("LR(0)")
            || GrammarInputController.getSelectedParser().equals("SLR(1)")){
            for(String s : GrammarInputController.getCurrentParserLR0().getGrammar().getFirstSets().keySet()){
                str.append(s).append(" : ").append(GrammarInputController.getCurrentParserLR0().getGrammar().getFirstSets().get(s)).append("\n");
            }

            m_OutputValue.setText(str.toString());
        }
    }

    @FXML
    private void onClickFollow(ActionEvent event){
        StringBuilder str = new StringBuilder();

        if(GrammarInputController.getSelectedParser().equals("LL(1)")){
            for(String s : GrammarInputController.getCurrentParserLL1().getGrammar().getFollowSets().keySet()){
                str.append(s).append(" : ").append(GrammarInputController.getCurrentParserLL1().getGrammar().getFollowSets().get(s)).append("\n");
            }

            m_OutputValue.setText(str.toString());
        }
        else if(GrammarInputController.getSelectedParser().equals("LR(0)") ||
                GrammarInputController.getSelectedParser().equals("SLR(1)")){
            for(String s : GrammarInputController.getCurrentParserLR0().getGrammar().getFollowSets().keySet()){
                str.append(s).append(" : ").append(GrammarInputController.getCurrentParserLR0().getGrammar().getFollowSets().get(s)).append("\n");
            }
        }
        else{
            for(String s : GrammarInputController.getCurrentParserLR1().getGrammar().getFollowSets().keySet()){
                str.append(s).append(" : ").append(GrammarInputController.getCurrentParserLR1().getGrammar().getFollowSets().get(s)).append("\n");
            }
        }

        m_OutputValue.setText(str.toString());
    }

    @FXML
    private void onClickState(ActionEvent event){
        if(GrammarInputController.getSelectedParser().equals("LL(1)")){
            m_OutputValue.setText(GrammarInputController.getCurrentParserLL1().getTableString());
        }
        else if(GrammarInputController.getSelectedParser().equals("LR(0)") ||
                GrammarInputController.getSelectedParser().equals("SLR(1)")){
            m_OutputValue.setText(GrammarInputController.getCurrentParserLR0().collectionToString());
        }
        else{
            m_OutputValue.setText(GrammarInputController.getCurrentParserLR1().collectionToString());
        }
    }

    @FXML
    private void onClickGoTo(ActionEvent event){
        if(GrammarInputController.getSelectedParser().equals("LL(1)")){
            m_OutputValue.setText(m_Translation.getString("DONT_HAVE_GOTO"));
        }
        else if(GrammarInputController.getSelectedParser().equals("LR(0)") ||
                GrammarInputController.getSelectedParser().equals("SLR(1)")){
            m_OutputValue.setText(GrammarInputController.getCurrentParserLR0().getGoToTableString());
        }
        else{
            m_OutputValue.setText(GrammarInputController.getCurrentParserLR1().getGoToTableString());
        }
    }

    @FXML
    private void onClickAction(ActionEvent event){
        if(GrammarInputController.getSelectedParser().equals("LL(1)")){
            m_OutputValue.setText(m_Translation.getString("DONT_HAVE_ACTION"));
        }
        else if(GrammarInputController.getSelectedParser().equals("LR(0)") ||
                GrammarInputController.getSelectedParser().equals("SLR(1)")){
            m_OutputValue.setText(GrammarInputController.getCurrentParserLR0().getActionTableString());
        }
        else{
            m_OutputValue.setText(GrammarInputController.getCurrentParserLR1().getActionTableString());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        m_Translation = FileResourceUtils.readJson(Main.TRANSLATION_FILE);

        if(!GrammarInputController.getSelectedParser().equals("LL(1)")){
            m_InputTextLabel.setText(m_Translation.getString("INPUT_ARGUMENTS"));
        }
        else {
            m_InputTextLabel.setText("");
        }

        Copyright.setText("* Developed by: Israel Santos Vieira");

        m_Result.setVisible(false);
        m_Input.setVisible(!GrammarInputController.getSelectedParser().equals("LL(1)"));

        m_Input.textProperty().addListener(((observable, oldValue, newValue) -> {
            String str = m_Input.getText();
            ArrayList<String> words = new ArrayList<>();
            String[] split = str.trim().split("\\s+");
            Collections.addAll(words, split);

            boolean accept;
            if(GrammarInputController.getSelectedParser().equals("LR(0)") ||
                    GrammarInputController.getSelectedParser().equals("SLR(1)")){

                accept = GrammarInputController.getCurrentParserLR0().accept(words);
            }
            else {
                accept = GrammarInputController.getCurrentParserLR1().accept(words);

            }
            if(accept){
                m_Result.setText(m_Translation.getString("ACCEPT"));
                m_Result.setTextFill(Color.GREEN);
            }
            else{
                m_Result.setText(m_Translation.getString("NOT_ACCEPT"));
                m_Result.setTextFill(Color.RED);
            }
            m_Result.setVisible(true);
        }));

        onClickGrammar(null);
    }
}

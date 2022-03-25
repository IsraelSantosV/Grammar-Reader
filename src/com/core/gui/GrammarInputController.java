package com.core.gui;

import com.core.Grammar;
import com.core.Main;
import com.core.parsers.ParserLR0;
import com.core.parsers.ParserLR1;
import com.tools.FileResourceUtils;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import org.json.JSONObject;

import java.util.Objects;
import java.util.ResourceBundle;

public class GrammarInputController implements Initializable {

    @FXML
    private Label m_ErrorLabel;

    @FXML
    private TextArea m_GrammarInput;

    @FXML
    private ComboBox m_ParserDropdown;

    @FXML
    private Label m_TitleText;
    @FXML
    private Label m_InfoBox1;
    @FXML
    private Label m_InfoBox2;
    @FXML
    private Label m_InfoBox3;
    @FXML
    private Label m_InfoBox4;
    @FXML
    private Label m_InfoBox5;
    @FXML
    private Label m_InfoBoxError;

    private JSONObject m_Translation;

    private static String m_SelectedParser;
    private static ParserLR0 m_ParserLR0;
    private static ParserLR1 m_ParserLR1;

    public static String getSelectedParser() { return m_SelectedParser; }
    public static ParserLR0 getCurrentParserLR0() { return m_ParserLR0; }
    public static ParserLR1 getCurrentParserLR1() { return m_ParserLR1; }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        m_ErrorLabel.setVisible(false);
        m_Translation = FileResourceUtils.readJson(Main.TRANSLATION_FILE);

        m_TitleText.setText(m_Translation.getString("INFO_TITLE"));
        m_InfoBox1.setText(m_Translation.getString("INFO_BOX_1"));
        m_InfoBox2.setText(m_Translation.getString("INFO_BOX_2"));
        m_InfoBox3.setText(m_Translation.getString("INFO_BOX_3"));
        m_InfoBox4.setText(m_Translation.getString("INFO_BOX_4"));
        m_InfoBox5.setText(m_Translation.getString("INFO_BOX_5"));
        m_InfoBoxError.setText(m_Translation.getString("INFO_BOX_ERROR"));

        m_GrammarInput.setPromptText(m_Translation.getString("GRAMMAR_INPUT_PROMPT"));

        ObservableList<String> options = FXCollections.observableArrayList(
                "LL(1)", "LR(0)", "SLR(1)", "CLR(1)"
        );

        m_ParserDropdown.setItems(options);
    }

    @FXML
    private void onTriggerStart(ActionEvent event) throws IOException {
        if(m_ParserDropdown.getValue() == null){
            throwError(m_Translation.getString("ERROR_SELECT_PARSER"));
        }
        else {
            m_SelectedParser = (String) m_ParserDropdown.getValue();

            boolean requireExtendGrammar = !m_SelectedParser.equals("LL(1)");

            String grammarText = m_GrammarInput.getText();
            Grammar grammar = new Grammar(grammarText, requireExtendGrammar);

            boolean canBeParse = true;
            if(m_SelectedParser.equals("LR(0)") || m_SelectedParser.equals("SLR(1)")){
                m_ParserLR0 = new ParserLR0(grammar);
                if(m_SelectedParser.equals("LR(0)")){
                    canBeParse = m_ParserLR0.parserLR0();
                }
                else{
                    canBeParse = m_ParserLR0.parserSLR1();
                }
            }
            else {
                m_ParserLR1 = new ParserLR1(grammar);
                if(m_SelectedParser.equals("CLR(1)")){
                    canBeParse = m_ParserLR1.parserCLR1();
                }
            }

            if(!canBeParse){
                throwError(m_Translation.getString("ERROR_ON_PARSE"));
            }
            else {
                Button button = (Button) event.getSource();
                Stage stage = (Stage) button.getScene().getWindow();

                Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("ResultSet.fxml")));
                Scene targetScene = new Scene(root);
                stage.setScene(targetScene);
            }
        }
    }

    public void throwError(String message){
        m_ErrorLabel.setVisible(true);
        m_ErrorLabel.setText(message);
    }
}

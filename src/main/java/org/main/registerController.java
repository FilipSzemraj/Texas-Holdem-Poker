package org.main;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class registerController {
    LoginController loginController;
    public registerController()
    {
    }
    public TextField nameField;
    public TextField surnameField;
    public TextField loginField;
    public TextField passwordField;
    public Label messageToUser;
    public Button registerButton;
    public void setLoginController(LoginController loginController)
    {
        this.loginController=loginController;
    }
    public void registerButtonOnClick(ActionEvent actionEvent) throws Exception {
    if(!nameField.getText().isBlank() && !surnameField.getText().isBlank() && !loginField.getText().isBlank() && !passwordField.getText().isBlank())
    {
        String registerPrompt = "serverAction-registerNewUser-"+nameField.getText()+"-"+surnameField.getText()+"-"+loginField.getText()+"-"+passwordField.getText()+"-";
        loginController.sendRequest(registerPrompt);
        String response = loginController.receiveResponse();
        String[] partedResponse = response.split("-");
        if(partedResponse[0].equals("new"))
        {
            messageToUser.setText("Udalo sie zarejestrowac nowego uzytkownika.");
        }
        else{
            messageToUser.setText("Blad podczas rejestrowania.");
        }
    }
    else{
        messageToUser.setText("Nalezy wypelnic wszystkie pola.");
    }

    }
}

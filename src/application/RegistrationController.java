package application;

import connection.ServerConnectionService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import pojo.RegistrationData;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Created by gandy on 20.04.15.
 *
 */
public class RegistrationController implements Initializable {

    @FXML private TextField     regUserSurnameField;
    @FXML private TextField     regUserNameField;
    @FXML private TextField     regUserTelField;
    @FXML private TextField     regUserEmailField;
    @FXML private TextField     regUserLoginField;
    @FXML private PasswordField regUserPasswordField;
    @FXML private Button        registerBtn;
    @FXML private Button        showLoginBtn;

    private Stage   stage;
    private boolean okClicked = false;

    private Pattern numbPat = Pattern.compile("[0-9]");
    private Pattern namePat = Pattern.compile("^[а-яА-ЯёЁіІїЇa-zA-Z]{0,25}$");
    private Pattern mailPat = Pattern.compile("^[-\\w.]+@([A-z0-9][-A-z0-9]+\\.)+[A-z]{2,4}$");
    private Pattern loginPat = Pattern.compile("^[a-zA-Z][a-zA-Z0-9-_\\.]{0,20}$");
    private Pattern passPat = Pattern.compile("(?=^.{6,}$)((?=.*\\d)|(?=.*\\W+))(?![.\\n])(?=.*[A-Z])(?=.*[a-z]).*$");

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public boolean isOkClicked() {
        return okClicked;
    }

    private void showLogin(){
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource(Main.LOG_FORM));

            AnchorPane pane = loader.load();
            Stage loginStage = new Stage();
            loginStage.setTitle("Авторизация");
            loginStage.setOnCloseRequest(action -> {
                ServerConnectionService.getInstance().stopConnection();
                loginStage.close();
            });
            Scene scene = new Scene(pane);
            scene.getStylesheets().add(Main.STYLE_PATH);
            loginStage.setScene(scene);

            LoginController controller = loader.getController();
            controller.setStage(loginStage);

            loginStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private boolean isFilledRegisterData(){
        return !(   regUserNameField.getText().isEmpty()
                ||  regUserSurnameField.getText().isEmpty()
                ||  regUserEmailField.getText().isEmpty()
                ||  regUserTelField.getText().isEmpty()
                ||  regUserLoginField.getText().isEmpty()
                ||  regUserPasswordField.getText().isEmpty());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.regUserNameField.setOnKeyReleased(event ->{
            TextField field  = null;
            if (event.getSource() instanceof  TextField ){
                field = (TextField) event.getSource();
            } else {
                System.out.println("Not a TextField event (");
                throw new Error("Event not a 'TextField type' FIX IT please!!");
            }
            while (field.getText().length() > 0) {
                if (namePat.matcher(field.getText()).matches())
                    break;
                field.setText(field.getText().substring(0, field.getText().length()-1));
                field.positionCaret(field.getLength());
            }
        });

        this.regUserSurnameField.setOnKeyReleased(this.regUserNameField.getOnKeyReleased());

        this.regUserLoginField.setOnKeyReleased(event ->{
            TextField field  = null;
            if (event.getSource() instanceof  TextField ){
                field = (TextField) event.getSource();
            } else {
                System.out.println("Not a TextField event (");
                throw new Error("Event not a 'TextField type' FIX IT please!!");
            }
            while (field.getText().length() > 0) {
                if (loginPat.matcher(field.getText()).matches())
                    break;
                field.setText(field.getText().substring(0, field.getText().length()-1));
                field.positionCaret(field.getLength());
            }
        });

        this.regUserTelField.setOnKeyReleased(event ->{
            if (this.regUserTelField.getText().length() > 14 ){
                this.regUserTelField.setText(this.regUserTelField.getText().substring(0, 15));
            }
            for (int i=0; i < this.regUserTelField.getLength(); i++){
                if (!numbPat.matcher(this.regUserTelField.getText().substring(i,i+1)).matches()){
                    if (i < 1) {
                        this.regUserTelField.setText(this.regUserTelField.getText().substring(i));
                    }
                    this.regUserTelField.setText(this.regUserTelField.getText().substring(0,i) +
                            this.regUserTelField.getText().substring(i+1));
                    this.regUserTelField.positionCaret(regUserTelField.getLength());
                }
            }
        });

        registerBtn.setOnAction(action -> {
            if (!isFilledRegisterData()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Информационное сообщение");
                alert.initModality(Modality.WINDOW_MODAL);
                alert.initOwner(stage);
                alert.setHeaderText(null);

                alert.setContentText("Необходимо заполнить все поля даными !!!");
                alert.show();
                return;
            }
            RegistrationData user = new RegistrationData();

            user.setSurname(regUserSurnameField.getText());
            user.setName(regUserNameField.getText());
            user.setEMail(regUserEmailField.getText());
            user.setTel(regUserTelField.getText());
            user.setLogin(regUserLoginField.getText());
            user.setPassword(regUserPasswordField.getText());

            Boolean result = false;
            try {
                result = ServerConnectionService.getInstance().registerUser(user);
            } catch (NullPointerException e){
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Внимание");
                alert.setHeaderText(null);
                alert.setContentText("Нет соединения с сервером !!!");
                alert.showAndWait();
                return;
            }

            if (result != null && result) {
                showLogin();
                stage.close();
            }
        });

        showLoginBtn.setOnAction(action -> {
            showLogin();
            stage.close();
        });

    }

}
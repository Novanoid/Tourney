package usspg31.tourney.controller.dialogs;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import usspg31.tourney.controller.PreferencesManager;
import usspg31.tourney.controller.controls.NumberTextField;
import usspg31.tourney.controller.dialogs.modal.DialogButtons;
import usspg31.tourney.controller.dialogs.modal.IModalDialogProvider;
import usspg31.tourney.controller.dialogs.modal.ModalDialog;

public class RegistrationDistributionNumberSelectionDialog extends VBox
        implements IModalDialogProvider<Integer, Integer> {

    private static final Logger log = Logger
            .getLogger(RegistrationDistributionNumberSelectionDialog.class
                    .getName());

    private int maximumRegistratorNumber;

    @FXML private NumberTextField textFieldNumberOfRegistrator;
    @FXML private Label labelNumberSelectionInformation;

    public RegistrationDistributionNumberSelectionDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    this.getClass()
                            .getResource(
                                    "/ui/fxml/dialogs/registration-distribution-number-selection-dialog.fxml"),
                    PreferencesManager.getInstance().getSelectedLanguage()
                            .getLanguageBundle());
            loader.setController(this);
            loader.setRoot(this);
            loader.load();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    @FXML
    private void initialize() {
        this.maximumRegistratorNumber = Integer.MAX_VALUE;
        this.textFieldNumberOfRegistrator.setFloatingPrompt(true);
        this.textFieldNumberOfRegistrator.setShowPrompt(true);
    }

    @Override
    public void setProperties(Integer properties) {
        this.maximumRegistratorNumber = properties;

        /* Display the maximum number of registrators in the text field */
        this.textFieldNumberOfRegistrator
                .setPromptText(PreferencesManager
                        .getInstance()
                        .localizeString(
                                "dialogs.registrationdistributionnumberselection.numberprompt")
                        + " "
                        + PreferencesManager
                                .getInstance()
                                .localizeString(
                                        "dialogs.registrationdistributionnumberselection.numberprompt.info.before")
                        + " "
                        + this.maximumRegistratorNumber
                        + PreferencesManager
                                .getInstance()
                                .localizeString(
                                        "dialogs.registrationdistributionnumberselection.numberprompt.info.after"));

        /* Display the maximum number of registrators in the information label */
        this.labelNumberSelectionInformation
                .setText(PreferencesManager
                        .getInstance()
                        .localizeString(
                                "dialogs.registrationdistributionnumberselection.description.informed.before")
                        + " "
                        + this.maximumRegistratorNumber
                        + " "
                        + PreferencesManager
                                .getInstance()
                                .localizeString(
                                        "dialogs.registrationdistributionnumberselection.description.informed.after"));
    }

    @Override
    public Integer getReturnValue() {
        if (this.textFieldNumberOfRegistrator.getText().length() == 0) {
            return null;
        } else {
            return Integer.valueOf(this.textFieldNumberOfRegistrator.getText());
        }
    }

    @Override
    public String getInputErrorString() {
        if (this.textFieldNumberOfRegistrator.getText().length() == 0) {
            return PreferencesManager
                    .getInstance()
                    .localizeString(
                            "dialogs.registrationdistributionnumberselection.errors.emptydata");
        }
        if (this.getReturnValue() <= 1) {
            return PreferencesManager
                    .getInstance()
                    .localizeString(
                            "dialogs.registrationdistributionnumberselection.errors.numbertoolow");
        }
        if (this.getReturnValue() > this.maximumRegistratorNumber) {
            return PreferencesManager
                    .getInstance()
                    .localizeString(
                            "dialogs.registrationdistributionnumberselection.errors.numbertoohigh.before")
                    + " "
                    + String.valueOf(this.maximumRegistratorNumber)
                    + " "
                    + PreferencesManager
                            .getInstance()
                            .localizeString(
                                    "dialogs.registrationdistributionnumberselection.errors.numbertoohigh.after");
        }

        return null;
    }

    @Override
    public void initModalDialog(ModalDialog<Integer, Integer> modalDialog) {
        modalDialog.title("dialogs.registrationdistributionnumberselection")
                .dialogButtons(DialogButtons.OK);
    }
}

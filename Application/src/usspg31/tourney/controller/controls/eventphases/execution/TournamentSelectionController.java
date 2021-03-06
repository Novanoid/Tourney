package usspg31.tourney.controller.controls.eventphases.execution;

import java.io.File;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import usspg31.tourney.controller.EntryPoint;
import usspg31.tourney.controller.MainWindow;
import usspg31.tourney.controller.PreferencesManager;
import usspg31.tourney.controller.controls.EventUser;
import usspg31.tourney.controller.controls.eventphases.TournamentExecutionPhaseController;
import usspg31.tourney.controller.dialogs.modal.DialogButtons;
import usspg31.tourney.controller.dialogs.modal.SimpleDialog;
import usspg31.tourney.controller.util.SearchUtilities;
import usspg31.tourney.model.Event;
import usspg31.tourney.model.Event.UserFlag;
import usspg31.tourney.model.Tournament;
import usspg31.tourney.model.Tournament.ExecutionState;
import usspg31.tourney.model.filemanagement.FileLoader;
import usspg31.tourney.model.filemanagement.FileSaver;

public class TournamentSelectionController implements EventUser {

    private static final Logger log = Logger
            .getLogger(TournamentSelectionController.class.getName());

    @FXML private TextField textFieldTournamentSearch;
    @FXML private TableView<Tournament> tableTournaments;

    @FXML private Button buttonExecuteTournament;
    @FXML private Button buttonExportTournament;
    @FXML private Button buttonImportTournament;

    private TableColumn<Tournament, String> tableColumnTournamentName;
    private TableColumn<Tournament, ExecutionState> tableColumnTournamentStatus;

    private Event loadedEvent;

    private TournamentExecutionPhaseController phaseController;

    @FXML
    private void initialize() {
        this.initPlayerTable();
    }

    private void initPlayerTable() {
        this.tableColumnTournamentName = new TableColumn<>(PreferencesManager
                .getInstance().localizeString(
                        "tournamentselection.tournamentname"));
        this.tableColumnTournamentName.setCellValueFactory(cellData -> cellData
                .getValue().nameProperty());
        this.tableTournaments.getColumns().add(this.tableColumnTournamentName);

        this.tableColumnTournamentStatus = new TableColumn<>(PreferencesManager
                .getInstance().localizeString(
                        "tournamentselection.tournamentstatus"));
        this.tableColumnTournamentStatus
                .setCellValueFactory(cellData -> cellData.getValue()
                        .executionStateProperty());
        this.tableColumnTournamentStatus.setCellFactory(column -> {
            return new TableCell<Tournament, ExecutionState>() {
                @Override
                protected void updateItem(ExecutionState item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item != null) {
                        switch (item) {
                        case NOT_EXECUTED:
                            this.setText(PreferencesManager
                                    .getInstance()
                                    .localizeString(
                                            "tournamentselection.tournamentstatus.notexecuted"));
                            break;
                        case CURRENTLY_EXECUTED:
                            this.setText(PreferencesManager
                                    .getInstance()
                                    .localizeString(
                                            "tournamentselection.tournamentstatus.executing"));
                            break;
                        case FINISHED:
                            this.setText(PreferencesManager
                                    .getInstance()
                                    .localizeString(
                                            "tournamentselection.tournamentstatus.finished"));
                            break;
                        }
                    }
                }
            };
        });
        this.tableTournaments.getColumns()
                .add(this.tableColumnTournamentStatus);

        this.tableTournaments
                .setPlaceholder(new Text(PreferencesManager.getInstance()
                        .localizeString("tableplaceholder.notournaments")));

        // start the selected tournament when double-clicked
        this.tableTournaments.setRowFactory(tableView -> {
            TableRow<Tournament> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    this.onButtonExecuteTournamentClicked(null);
                }
            });
            return row;
        });
    }

    @Override
    public void loadEvent(Event event) {
        log.info("Loading Event");
        if (this.loadedEvent != null) {
            this.unloadEvent();
        }

        this.loadedEvent = event;

        /* Add all tournaments to the table view and enable searching */
        Comparator<Tournament> comparator = (firstTournament, lastPlayer) -> (int) (SearchUtilities
                .getSearchRelevance(
                        firstTournament.getName(),
                        TournamentSelectionController.this.textFieldTournamentSearch
                                .getText()) - SearchUtilities
                .getSearchRelevance(
                        lastPlayer.getName(),
                        TournamentSelectionController.this.textFieldTournamentSearch
                                .getText()));

        this.textFieldTournamentSearch.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    MainWindow.getInstance().getEventPhaseViewController()
                            .getUndoManager().setSleeping(true);
                    FXCollections.sort(this.tableTournaments.getItems(),
                            comparator);
                    MainWindow.getInstance().getEventPhaseViewController()
                            .getUndoManager().setSleeping(false);
                });

        this.tableTournaments.setItems(this.loadedEvent.getTournaments());

        this.tableColumnTournamentName.prefWidthProperty().set(
                this.tableTournaments.widthProperty().get() * 0.7);
        this.tableColumnTournamentStatus.prefWidthProperty().set(
                this.tableTournaments.widthProperty().get() * 0.3);

        // Bind the button's availability to the list selection
        this.buttonExecuteTournament.disableProperty().bind(
                this.tableTournaments.getSelectionModel()
                        .selectedItemProperty().isNull());
        this.buttonExportTournament.disableProperty().bind(
                this.tableTournaments.getSelectionModel()
                        .selectedItemProperty().isNull());
    }

    @Override
    public void unloadEvent() {
        log.info("Unloading Event");
        if (this.loadedEvent == null) {
            log.warning("Trying to unload an event, even though no event was loaded");
            return;
        }

        /* Unbind the table of tournaments */
        this.tableTournaments.getSelectionModel().clearSelection();

        /* Unbind the button's availablity */
        this.buttonExecuteTournament.disableProperty().unbind();
        this.buttonExportTournament.disableProperty().unbind();

        this.loadedEvent = null;
    }

    public void setExecutionSuperController(
            TournamentExecutionPhaseController controller) {
        this.phaseController = controller;
    }

    @FXML
    private void onButtonExecuteTournamentClicked(ActionEvent event) {
        log.fine("Execute Tournament Button clicked");
        this.checkEventLoaded();

        final Tournament selectedTournament = this.tableTournaments
                .getSelectionModel().getSelectedItem();
        if (selectedTournament == null) {
            new SimpleDialog<>(PreferencesManager.getInstance().localizeString(
                    "tournamentselection.dialogs.noselection")).modalDialog()
                    .dialogButtons(DialogButtons.OK)
                    .title("dialogs.titles.error").show();
        } else {
            this.phaseController
                    .showTournamentExecutionView(selectedTournament);
        }
    }

    @FXML
    private void onButtonExportTournamentClicked(ActionEvent event) {
        log.fine("Export Tournament Button clicked");

        final Tournament selectedTournament = this.tableTournaments
                .getSelectionModel().getSelectedItem();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(PreferencesManager.getInstance().localizeString(
                "tournamentselection.dialogs.distribute.title"));
        fileChooser.getExtensionFilters().add(
                new ExtensionFilter(PreferencesManager.getInstance()
                        .localizeString("dialogs.extensions.eventfile"),
                        "*.tef"));
        File selectedFile = fileChooser.showSaveDialog(EntryPoint
                .getPrimaryStage());
        if (selectedFile == null) {
            return;
        }
        if (!selectedFile.getName().endsWith(".tef")) {
            selectedFile = new File(selectedFile.getAbsolutePath() + ".tef");
        }

        this.loadedEvent.setUserFlag(UserFlag.TOURNAMENT_EXECUTION);
        Tournament lastTournament = this.loadedEvent.getExecutedTournament();
        this.loadedEvent.setExecutedTournament(selectedTournament);

        try {
            FileSaver.saveEventToFile(this.loadedEvent,
                    selectedFile.getAbsolutePath());
        } catch (Exception e) {
            log.log(Level.SEVERE, "Could not save the event.", e);

            new SimpleDialog<>(PreferencesManager.getInstance().localizeString(
                    "dialogs.messages.couldnotsave")).modalDialog()
                    .title("dialogs.titles.error")
                    .dialogButtons(DialogButtons.OK).show();
        }

        this.loadedEvent.setUserFlag(UserFlag.ADMINISTRATION);
        this.loadedEvent.setExecutedTournament(lastTournament);
    }

    @FXML
    private void onButtonImportTournamentClicked(ActionEvent event) {
        log.fine("Import Tournament Button clicked");
        this.checkEventLoaded();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(PreferencesManager.getInstance().localizeString(
                "tournamentselection.dialogs.import.title"));
        fileChooser.getExtensionFilters().add(
                new ExtensionFilter(PreferencesManager.getInstance()
                        .localizeString("dialogs.extensions.eventfile"),
                        "*.tef"));
        File selectedFile = fileChooser.showOpenDialog(EntryPoint
                .getPrimaryStage());
        if (selectedFile != null) {
            Event importedEvent = null;

            try {
                importedEvent = FileLoader.loadEventFromFile(selectedFile
                        .getAbsolutePath());
            } catch (Exception e) {
                log.log(Level.SEVERE, "Could not load the specified event.", e);
                new SimpleDialog<>(PreferencesManager.getInstance()
                        .localizeString("dialogs.messages.couldnotload"))
                        .modalDialog().dialogButtons(DialogButtons.OK)
                        .title("dialogs.titles.error").show();
            }

            if (importedEvent != null) {
                Tournament importedTournament = importedEvent
                        .getExecutedTournament();

                if (importedTournament == null) {
                    new SimpleDialog<>(
                            PreferencesManager
                                    .getInstance()
                                    .localizeString(
                                            "tournamentselection.dialogs.import.unsuccessful.invalidevent"))
                            .modalDialog().dialogButtons(DialogButtons.OK)
                            .title("dialogs.titles.error").show();
                    return;
                }

                Tournament tournamentToRemove = null;
                for (Tournament existentTournament : this.loadedEvent
                        .getTournaments()) {
                    if (existentTournament.getId().equals(
                            importedTournament.getId())) {
                        tournamentToRemove = existentTournament;
                        break;
                    }
                }

                if (tournamentToRemove == null) {
                    new SimpleDialog<>(
                            PreferencesManager
                                    .getInstance()
                                    .localizeString(
                                            "tournamentselection.dialogs.import.unsuccessful.invalidtournament"))
                            .modalDialog().dialogButtons(DialogButtons.OK)
                            .title("dialogs.titles.error").show();
                    return;
                }

                /* Actually replace the tournament by the new one */
                this.loadedEvent.getTournaments().remove(tournamentToRemove);
                this.loadedEvent.getTournaments().add(importedTournament);

                new SimpleDialog<>(
                        PreferencesManager
                                .getInstance()
                                .localizeString(
                                        "tournamentselection.dialogs.import.successful.before")
                                + " \""
                                + importedTournament.getName()
                                + "\" "
                                + PreferencesManager
                                        .getInstance()
                                        .localizeString(
                                                "tournamentselection.dialogs.import.successful.after"))
                        .modalDialog()
                        .dialogButtons(DialogButtons.OK)
                        .title("tournamentselection.dialogs.import.successful.title")
                        .show();
            }
        }
    }

    private void checkEventLoaded() {
        if (this.loadedEvent == null) {
            throw new IllegalStateException("An Event must be loaded in order "
                    + "to perform actions on this controller");
        }

        if (this.phaseController == null) {
            throw new IllegalStateException(
                    "A super controller must be attached in order "
                            + "to perform actions on this controller");
        }
    }
}

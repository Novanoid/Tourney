package usspg31.tourney.controller.controls.eventphases;

import java.util.logging.Logger;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialog;
import org.controlsfx.dialog.Dialogs;

import usspg31.tourney.controller.EntryPoint;
import usspg31.tourney.controller.controls.EventUser;
import usspg31.tourney.controller.dialogs.DialogResult;
import usspg31.tourney.controller.dialogs.PlayerPreRegistrationDialogController;
import usspg31.tourney.model.Event;
import usspg31.tourney.model.Player;

@SuppressWarnings("deprecation")
public class PreRegistrationPhaseController implements EventUser {

	private static final Logger log = Logger
			.getLogger(PreRegistrationPhaseController.class.getName());

	@FXML private TextField textFieldPlayerSearch;
	@FXML private TableView<Player> tablePreRegisteredPlayers;
	@FXML private Button buttonAddPlayer;
	@FXML private Button buttonRemovePlayer;
	@FXML private Button buttonEditPlayer;

	private TableColumn<Player, String> tableColumnPlayerFirstName;
	private TableColumn<Player, String> tableColumnPlayerLastName;
	private TableColumn<Player, String> tableColumnPlayerNickName;
	private TableColumn<Player, String> tableColumnPlayerMailAddress;

	private Event loadedEvent;

	@FXML
	private void initialize() {
		this.initPlayerTable();

		// Bind the button's availability to the list selection
		this.buttonRemovePlayer.disableProperty().bind(
				this.tablePreRegisteredPlayers.getSelectionModel()
						.selectedItemProperty().isNull());
		this.buttonEditPlayer.disableProperty().bind(
				this.tablePreRegisteredPlayers.getSelectionModel()
						.selectedItemProperty().isNull());
	}

	private void initPlayerTable() {
		this.tableColumnPlayerFirstName = new TableColumn<>("Vorname");
		this.tableColumnPlayerFirstName
				.setCellValueFactory(cellData -> cellData.getValue()
						.firstNameProperty());
		this.tablePreRegisteredPlayers.getColumns().add(
				this.tableColumnPlayerFirstName);

		this.tableColumnPlayerLastName = new TableColumn<>("Nachname");
		this.tableColumnPlayerLastName.setCellValueFactory(cellData -> cellData
				.getValue().lastNameProperty());
		this.tablePreRegisteredPlayers.getColumns().add(
				this.tableColumnPlayerLastName);

		this.tableColumnPlayerNickName = new TableColumn<>("Nickname");
		this.tableColumnPlayerNickName.setCellValueFactory(cellData -> cellData
				.getValue().nickNameProperty());
		this.tablePreRegisteredPlayers.getColumns().add(
				this.tableColumnPlayerNickName);

		this.tableColumnPlayerMailAddress = new TableColumn<>("E-Mail");
		this.tableColumnPlayerMailAddress
				.setCellValueFactory(cellData -> cellData.getValue()
						.mailAdressProperty());
		this.tablePreRegisteredPlayers.getColumns().add(
				this.tableColumnPlayerMailAddress);
	}

	@Override
	public void loadEvent(Event event) {
		log.info("Loading Event");
		if (this.loadedEvent != null) {
			this.unloadEvent();
		}

		this.loadedEvent = event;

		// create table bindings
		this.tablePreRegisteredPlayers.setItems(this.loadedEvent
				.getRegisteredPlayers());
	}

	@Override
	public void unloadEvent() {
		log.info("Unloading Event");
		if (this.loadedEvent == null) {
			log.warning("Trying to unload an event, even though no event was loaded");
			return;
		}

		// TODO: unregister all listeners we registered to anything in the event

		this.loadedEvent = null;
	}

	@FXML
	private void onButtonAddPlayerClicked(ActionEvent event) {
		log.fine("Add Player Button clicked");
		this.checkEventLoaded();
		new PlayerPreRegistrationDialogController()
				.modalDialog()
				.properties(new Player())
				.properties(this.loadedEvent)
				.onResult(
						(result, returnValue) -> {
							if (result == DialogResult.OK
									&& returnValue != null) {
								this.loadedEvent.getRegisteredPlayers().add(
										returnValue);
								returnValue.setId(String
										.valueOf(this.loadedEvent
												.getRegisteredPlayers().size()));
							}
						}).show();
	}

	@FXML
	private void onButtonRemovePlayerClicked(ActionEvent event) {
		log.fine("Remove Player Button clicked");
		this.checkEventLoaded();

		Player selectedPlayer = this.tablePreRegisteredPlayers
				.getSelectionModel().getSelectedItem();
		if (selectedPlayer == null) {
			Dialogs.create()
					.owner(EntryPoint.getPrimaryStage())
					.title("Fehler")
					.message(
							"Bitte wählen Sie einen Spieler aus der Liste aus.")
					.showError();
		} else {
			Action response = Dialogs
					.create()
					.owner(EntryPoint.getPrimaryStage())
					.title("Spieler löschen")
					.message(
							"Wollen Sie den Spieler \""
									+ selectedPlayer.getFirstName() + " "
									+ selectedPlayer.getLastName()
									+ "\" wirklich löschen?").showConfirm();

			if (response == Dialog.ACTION_YES) {
				this.loadedEvent.getRegisteredPlayers().remove(selectedPlayer);
			}
		}
	}

	@FXML
	private void onButtonEditPlayerClicked(ActionEvent event) {
		log.fine("Edit Player Button clicked");
		this.checkEventLoaded();

		final Player selectedPlayer = this.tablePreRegisteredPlayers
				.getSelectionModel().getSelectedItem();
		if (selectedPlayer == null) {
			Dialogs.create()
					.owner(EntryPoint.getPrimaryStage())
					.title("Fehler")
					.message(
							"Bitte wählen Sie einen Spieler aus der Liste aus.")
					.showError();
		} else {
			new PlayerPreRegistrationDialogController()
					.modalDialog()
					.properties(selectedPlayer)
					.properties(this.loadedEvent)
					.onResult(
							(result, returnValue) -> {
								if (result == DialogResult.OK
										&& returnValue != null) {
									this.loadedEvent.getRegisteredPlayers()
											.remove(selectedPlayer);
									this.loadedEvent.getRegisteredPlayers()
											.add(returnValue);
								}
							}).show();
		}
	}

	private void checkEventLoaded() {
		if (this.loadedEvent == null) {
			throw new IllegalStateException("An Event must be loaded in order "
					+ "to perform actions on this controller");
		}
	}
}

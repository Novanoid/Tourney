package usspg31.tourney.controller;

import java.util.logging.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EntryPoint extends Application {

	private static final Logger log = Logger.getLogger(EntryPoint.class.getName());

	private static EntryPoint instance = null;

	public static void main(String[] args) {
		log.info("Starting Application");
		log.info("Running JavaFX Version " + System.getProperty("javafx.runtime.version"));

		try {
			launch(args);
		} catch (Throwable t) {
			log.severe(t.toString());
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		instance = this;

		try {
			FXMLLoader fxmlLoader = new FXMLLoader();
			fxmlLoader.setLocation(this.getClass().getResource("/ui/fxml/main-window.fxml"));
			Parent root = null;
			root = (Parent)fxmlLoader.load();
			Scene scene = new Scene(root);
			primaryStage.setScene(scene);
			primaryStage.setTitle("Tourney");

			primaryStage.show();
		} catch(Exception e) {
			log.severe(e.toString());
		}
	}

}

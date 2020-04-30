package application;
	
import javafx.application.Application;
import javafx.scene.image.*;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.geometry.*;
import javafx.event.*;
import javafx.scene.paint.*;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			guiStage = primaryStage;
			guiStage.setTitle("CHATEE");
			guiStage.getIcons().add(new Image(Main.class.getResource("/assets/icon.png").toExternalForm()));
			
			Http.loadToken();
			boolean res = Http.getAuth();
			if(res)
				guiStage.setScene(Dashboard.getScene());
			else
				guiStage.setScene(Login.getScene());
			guiStage.show();
			
			guiStage.setOnCloseRequest((WindowEvent event1) -> {
		        Dashboard.endgame=true;
		    });
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	private static Stage guiStage;

    public static Stage getStage() {
        return guiStage;
    }
	
	public static void main(String[] args) {
		launch(args);
	}
}

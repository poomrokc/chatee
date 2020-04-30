package application;
	
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.geometry.*;
import javafx.event.*;
import javafx.scene.paint.*;

import org.json.simple.JSONObject;

public class Login {

	private static Scene scene = null;
	
	public Login() {
	}
	
	public static Scene getScene() {
		if(scene==null) {
			GridPane grid = new GridPane();
			grid.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
			grid.setAlignment(Pos.CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(25, 25, 25, 25));
			Text scenetitle = new Text("Login to CHATEE");
			scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
			grid.add(scenetitle, 0, 0, 2, 1);

			Label userName = new Label("User Name:");
			grid.add(userName, 0, 1);

			TextField userTextField = new TextField();
			grid.add(userTextField, 1, 1);

			Label pw = new Label("Password:");
			grid.add(pw, 0, 2);

			PasswordField pwBox = new PasswordField();
			grid.add(pwBox, 1, 2);
			
			Button btn = new Button("Sign in");
			HBox hbBtn = new HBox(10);
			hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
			Button btn2 = new Button("Register");
			hbBtn.getChildren().add(btn2);
			hbBtn.getChildren().add(btn);
			grid.add(hbBtn, 1, 4);
			
			
			final Text actiontarget = new Text();
	        grid.add(actiontarget, 1, 5);
	        
	        btn.setOnAction(new EventHandler<ActionEvent>() {
	            @Override
	            public void handle(ActionEvent e) {
	            	try {
		            	JSONObject ret = Http.login(userTextField.getText(), pwBox.getText());
		            	if(!(boolean)ret.get("success")) {
			                actiontarget.setFill(Color.FIREBRICK);
			                actiontarget.setText((String)ret.get("msg"));
		            	}
		            	else
		            	{
		            		Http.getAuth();
		            		Main.getStage().setScene(Dashboard.getScene());
		            	}
	            	}
	            	catch(Exception err) {
	            		err.printStackTrace();
	            	}
	            }
	        });
	        
	        btn2.setOnAction(new EventHandler<ActionEvent>() {
	            @Override
	            public void handle(ActionEvent e) {
	                Main.getStage().setScene(Register.getScene());
	            }
	        });
	        
	        scene = new Scene(grid, 300, 275);
		}
        return scene;
    }
	
}

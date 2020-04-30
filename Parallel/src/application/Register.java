package application;
	
import org.json.simple.JSONObject;

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

public class Register {

	private static Scene scene = null;
	
	public Register() {
	}
	
	public static Scene getScene() {
		if(scene==null) {
			GridPane grid = new GridPane();
			grid.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
			grid.setAlignment(Pos.TOP_CENTER);
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(0, 25, 25, 25));
			Pane canvas = new Pane();
		    canvas.setPrefSize(0,10);
		    
			Button btnBack = new Button("Back");
			btnBack.setPrefSize(220, 20);
			grid.add(btnBack, 0, 0,2,1);
			
			grid.add(canvas, 0, 1);
			
			Text scenetitle = new Text("Register");
			scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
			grid.add(scenetitle, 0, 2, 2, 1);

			Label userName = new Label("User Name:");
			grid.add(userName, 0, 3);

			TextField userTextField = new TextField();
			grid.add(userTextField, 1, 3);

			Label pw = new Label("Password:");
			grid.add(pw, 0, 4);

			PasswordField pwBox = new PasswordField();
			grid.add(pwBox, 1, 4);
			
			Label chatName = new Label("Chat Name:");
			grid.add(chatName, 0, 5);

			TextField chatNameTextField = new TextField();
			grid.add(chatNameTextField, 1, 5);
			
			Button btn = new Button("Submit");
			HBox hbBtn = new HBox(10);
			hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
			hbBtn.getChildren().add(btn);
			grid.add(hbBtn, 1, 6);
			
			
			final Text actiontarget = new Text();
	        grid.add(actiontarget, 1, 7);
	        
	        btn.setOnAction(new EventHandler<ActionEvent>() {
	            @Override
	            public void handle(ActionEvent e) {
	            	try {
		            	JSONObject ret = Http.register(userTextField.getText(), pwBox.getText(),chatNameTextField.getText());
		            	if(!(boolean)ret.get("success")) {
			                actiontarget.setFill(Color.FIREBRICK);
			                actiontarget.setText((String)ret.get("msg"));
		            	}
		            	else {
		            		Http.getAuth();
		            		Main.getStage().setScene(Dashboard.getScene());
		            	}
	            	}
	            	catch(Exception err) {
	            		
	            	}
	            }
	        });
	        
	        btnBack.setOnAction(new EventHandler<ActionEvent>() {
	            @Override
	            public void handle(ActionEvent e) {
	                Main.getStage().setScene(Login.getScene());
	            }
	        });
	        
	        
	        scene = new Scene(grid, 300, 275);
		}
        return scene;
    }
	
}

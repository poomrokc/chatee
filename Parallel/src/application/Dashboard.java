package application;
	
import java.util.Objects;
import java.util.Optional;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.stage.Stage;
import javafx.scene.image.*;
import javafx.scene.Scene;
import javafx.scene.shape.*;
import javafx.scene.layout.*;
import javafx.scene.input.*;
import javafx.scene.control.*;
import javafx.scene.text.*;
import javafx.geometry.*;
import javafx.event.*;
import javafx.scene.paint.*;

public class Dashboard {

	public static boolean endgame=false;
	
	private static Scene scene = null;
	private static Circle iv = null;
	private static VBox groupList = null;
	private static VBox chatList = null;
	private static VBox empty = null;
	private static VBox right = null;
	private static Text gnameTab = null;
	private static TextField message;
	private static ScrollPane chatScroll;
	
	public static String selected = "";
	public static int dispnum = 30;
	
	private static Updater ut = null;
	
	public static JSONArray lastgroup=null;
	public static JSONArray lastchat=null;
	
	public static boolean isNewGroup(JSONArray newgroup) {
		if(Objects.isNull(lastgroup)) 
			return true;
		if(lastgroup.size()!=newgroup.size())
			return true;
		for(int i=0;i<lastgroup.size();i++) {
			JSONObject A = (JSONObject)lastgroup.get(i);
			JSONObject B = (JSONObject)newgroup.get(i);
			if(!((String)A.get("_id")).equals((String)B.get("_id")))
				return true;
			if(!((String)A.get("name")).equals((String)B.get("name")))
				return true;
			if(!((String)A.get("photo")).equals((String)B.get("photo")))
				return true;
			
			String aa = ""+A.get("memberCount");
			String bb = ""+B.get("memberCount");
			if(!aa.equals(bb))
				return true;
			
			aa = ""+A.get("unread");
			bb = ""+B.get("unread");
			
			if(!aa.equals(bb))
				return true;
		}
		
		return false;
	}
	
	public static void renderChats() throws ParseException, InterruptedException {
		
		JSONArray nowchat=(JSONArray)DB.chats.get(selected);
		if(Objects.isNull(nowchat))
			return;
		int updatesince=999999999;
		boolean change = false;
		
		for(int i=0;i<nowchat.size();i++) {
			JSONObject mess = (JSONObject)nowchat.get(i);
			int lsize;
			if(Objects.isNull(lastchat))
				lsize=0;
			else
				lsize=lastchat.size();
			
			if(i>=lsize) {
				updatesince=i;
			}
			else {
				JSONObject ll = (JSONObject)lastchat.get(i);
				if(!((String)mess.get("_id")).equals((String)ll.get("_id"))) {
					updatesince=i;
					while(chatList.getChildren().size()>i)
						chatList.getChildren().remove(chatList.getChildren().size()-1);
				}
			}
			
			if(updatesince<=i) {
				change=true;
				if(!((String)mess.get("user")).equals((String)DB.user.get("_id"))) {
					VBox con =new VBox();
					con.setSpacing(5);
					
					JSONObject memmem =(JSONObject)DB.member.get((String)mess.get("user"));
					
					String text;
					if(Objects.isNull(memmem))
						text="Unknown";
					else
						text=(String)memmem.get("name");
					
					Text row1 =new Text(text);
					row1.setFont(Font.font("verdana", FontWeight.BOLD, FontPosture.REGULAR, 10));
					HBox row2 =new HBox();
					row2.setAlignment(Pos.TOP_LEFT);
					row2.setSpacing(10);
					
					StackPane box = new StackPane();
					box.setAlignment(Pos.CENTER_LEFT);
					box.setMaxWidth(200);
					
					
					Label msg = new Label((String)mess.get("message"));
				    msg.setWrapText(true);
					
					
					box.getChildren().add(msg);
					box.setStyle("-fx-padding:5 10 5 10;-fx-border-color : #000; -fx-border-width : 1 1 1 1; -fx-background-color: #FFFFFF;-fx-border-radius:20;-fx-background-radius:20");
					
					Circle giv = new Circle();
					
					String phot;
					
					if(Objects.isNull(memmem))
						phot="";
					else {
						phot=(String)memmem.get("photo");
						Image im = new Image(phot,false);
				        giv.setFill(new ImagePattern(im));
					}
					
					giv.setRadius(15);
					row2.getChildren().add(giv);
					
					row2.getChildren().add(box);
					
					String[] time = ((String)mess.get("created")).substring(11, 19).split(":");
					String rtime = Integer.toString(((Integer.parseInt(time[0])+7)%24));
					if(rtime.length()==1)
						rtime="0"+rtime;
					
					
					Label row3 =new Label(rtime+":"+time[1]+":"+time[2]);
					row3.setFont(Font.font("verdana", FontWeight.NORMAL, FontPosture.REGULAR, 10));
					row3.setStyle("-fx-padding:0 0 0 40;");
					
					con.getChildren().add(row1);
					con.getChildren().add(row2);
					con.getChildren().add(row3);
					chatList.getChildren().add(con);
				}
				else {
					VBox con =new VBox();
					con.setSpacing(5);
					con.setAlignment(Pos.TOP_RIGHT);
					
					String text = (String)((JSONObject)DB.member.get((String)mess.get("user"))).get("name");
					HBox row2 =new HBox();
					row2.setAlignment(Pos.TOP_RIGHT);
					row2.setSpacing(10);
					
					StackPane box = new StackPane();
					box.setAlignment(Pos.CENTER_RIGHT);
					box.setMaxWidth(200);
					
					
					Label msg = new Label((String)mess.get("message"));
				    msg.setWrapText(true);
					
					
					box.getChildren().add(msg);
					box.setStyle("-fx-padding:5 10 5 10;-fx-border-color : #000; -fx-border-width : 1 1 1 1; -fx-background-color: #00DD00;-fx-border-radius:20;-fx-background-radius:20");
					
					row2.getChildren().add(box);
					
					String[] time = ((String)mess.get("created")).substring(11, 19).split(":");
					String rtime = Integer.toString(((Integer.parseInt(time[0])+7)%24));
					if(rtime.length()==1)
						rtime="0"+rtime;
					
					
					Label row3 =new Label(rtime+":"+time[1]+":"+time[2]);
					row3.setFont(Font.font("verdana", FontWeight.NORMAL, FontPosture.REGULAR, 10));
					row3.setStyle("-fx-padding:0 0 0 0;");
					
					con.getChildren().add(row2);
					con.getChildren().add(row3);
					chatList.getChildren().add(con);
				}
			}
		}
		if(change) {
			chatScroll.applyCss();
			chatScroll.layout();
			chatScroll.setVvalue(1.0f);
		}
		
		String middle = nowchat.toJSONString();
		JSONParser parser = new JSONParser();
		Object obj = parser.parse(middle);    
		lastchat = (JSONArray)obj;
	}
	
	private static void renderBlank() {
		right.getChildren().remove(1);
		right.getChildren().add(empty);
	}
	
	private static void renderChatBox(String nn) {
		VBox con =new VBox();
		HBox gInfo =new HBox();
		gInfo.setPadding(new Insets(0,10,0,10));
		gInfo.setPrefSize(420, 40);
		gInfo.setMaxWidth(420);
		gInfo.setAlignment(Pos.CENTER);
		gInfo.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		right.getChildren().remove(1);
		
		HBox idc = new HBox();
		idc.setSpacing(5);
		idc.setAlignment(Pos.CENTER_LEFT);
		
		Button ltt = new Button("Leave");
		ltt.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() { 
			   @Override 
			   public void handle(MouseEvent e) { 
				  try {
					  Http.leaveGroup(selected);
					  renderBlank();
				  }
				  catch(Exception err) {
					  
				  }
			   } 
			}
		);
		ltt.setPrefSize(80, 10);
		ltt.setStyle("-fx-text-fill: white;-fx-cursor: hand; -fx-background-color: #ff0000;-fx-border-radius:0;-fx-background-radius:0");
		idc.getChildren().add(ltt);
		
		Button btt = new Button("Copy ID");
		btt.setPrefSize(80, 10);
		btt.setStyle("-fx-border-radius:0;-fx-background-radius:0");
		
		btt.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() { 
			   @Override 
			   public void handle(MouseEvent e) { 
				  try {
					  StringSelection stringSelection = new StringSelection(selected);
					  Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
					  clipboard.setContents(stringSelection, null);
				  }
				  catch(Exception err) {
					  
				  }
			   } 
			}
		);
		idc.getChildren().add(btt);
		
		gnameTab = new Text(nn);
		gnameTab.setFont(Font.font("Tahoma", FontWeight.BOLD, 13));
		
		gInfo.getChildren().add(gnameTab);
		
		Region reg = new Region();
		gInfo.getChildren().add(reg);
		gInfo.setHgrow(reg, Priority.ALWAYS);
		
		
		gInfo.getChildren().add(idc);
		
		
		chatList = new VBox();
		chatList.setPrefWidth(404);
		chatList.setMinHeight(350);
		chatList.setBackground(new Background(new BackgroundFill(Color.LIGHTSKYBLUE, CornerRadii.EMPTY, Insets.EMPTY)));
		chatList.setSpacing(20);
		chatList.setPadding(new Insets(10,10,10,10));
		
		
		chatScroll = new ScrollPane();
		chatScroll.setContent(groupList);
		chatScroll.setPrefSize(404, 350);
		chatScroll.setContent(chatList);
		
		HBox type =new HBox();
		type.setPrefSize(420, 30);
		type.setAlignment(Pos.CENTER);
		type.setBackground(new Background(new BackgroundFill(Color.BISQUE, CornerRadii.EMPTY, Insets.EMPTY)));
		
		message = new TextField();
		message.setPrefSize(370, 30);
		message.addEventHandler(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() { 
			   @Override 
			   public void handle(KeyEvent e) { 
				  try {
					  if(e.getCode()==KeyCode.ENTER) {
						  Http.sendChat(selected,message.getText());
						  message.setText("");
					  }
				  }
				  catch(Exception err) {
					  
				  }
			   } 
			}
		);
		
		Button send = new Button("Send");
		send.setStyle("-fx-text-fill: white;-fx-cursor: hand;-fx-border-color : #3399ff; -fx-border-width : 1 1 1 1; -fx-background-color: #3399ff;-fx-border-radius:0;-fx-background-radius:0");
		send.setPrefSize(50, 30);
		
		send.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() { 
			   @Override 
			   public void handle(MouseEvent e) { 
				  try {
					  Http.sendChat(selected,message.getText());
					  message.setText("");
				  }
				  catch(Exception err) {
					  
				  }
			   } 
			}
		);
		
		type.getChildren().add(message);
		type.getChildren().add(send);
		
		con.getChildren().add(gInfo);
		con.getChildren().add(chatScroll);
		con.getChildren().add(type);
		
		right.getChildren().add(con);
		
	}
	
	public static void renderGroups() {
		JSONArray groups = DB.groupsInfo;
		groupList.getChildren().clear();
		for(int i=0;i<groups.size();i++) {
			JSONObject g = (JSONObject)groups.get(i);
			HBox gr = new HBox();
			gr.setPrefSize(204, 60);
			gr.setPadding(new Insets(0,0,0,7));
			gr.setAlignment(Pos.CENTER_LEFT);
			gr.setSpacing(10);
			Circle giv = new Circle();
			if(!Objects.isNull(g.get("photo"))) {
				if(((String)g.get("photo")).length()!=0) {
					Image im = new Image((String)g.get("photo"),false);
		        	giv.setFill(new ImagePattern(im));
				}
			}
			giv.setRadius(20);
			
			Text gname = new Text((String)g.get("name") + " ("+Integer.toString((int)g.get("memberCount"))+")");
			gname.setFont(Font.font("Tahoma", FontWeight.BOLD, 13));
			
			gr.getChildren().add(giv);
			gr.getChildren().add(gname);
			
			String noti =""+g.get("unread");
			if(!noti.equals("0")&&!noti.equals("")&&!noti.equals("null")) {
				Circle no = new Circle();
				no.setRadius(13);
				no.setFill(Color.LIME);
				Text text = new Text(noti);
				text.setBoundsType(TextBoundsType.VISUAL); 
				StackPane stack = new StackPane();
				stack.getChildren().add(no);
				stack.getChildren().add(text);
				gr.getChildren().add(stack);
			}
			gr.setStyle("-fx-border-color : #CCC; -fx-border-width : 0 0 1 0; -fx-cursor: hand;");
			gr.styleProperty().bind(Bindings.when(gr.hoverProperty())
                    .then("-fx-background-color: #EAEAEA;-fx-border-color : #CCC; -fx-border-width : 0 0 1 0; -fx-cursor: hand;")
                    .otherwise("-fx-background-color: #FFFFFF;-fx-border-color : #CCC; -fx-border-width : 0 0 1 0; -fx-cursor: hand;"));
			
			gr.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() { 
				   @Override 
				   public void handle(MouseEvent e) { 
					  String id = (String)g.get("_id");
					  selected=id;
					  lastchat = new JSONArray();
					  dispnum = 30;
				      DB.clearUnread(id);
				      renderChatBox((String)g.get("name") + " ("+Integer.toString((int)g.get("memberCount"))+")");
				   } 
				}
			);
			
			groupList.getChildren().add(gr);
		}
		String middle = DB.groupsInfo.toJSONString();
		JSONParser parser = new JSONParser();
		try {
			Object obj = parser.parse(middle);    
			Dashboard.lastgroup = (JSONArray)obj;
		}
		catch(Exception e)
		{
			
		}
	}
	
	public Dashboard() {
	}
	
	public static Scene getScene() {
		if(scene==null) {
			DB.initDB();
			lastgroup=null;
			
			HBox hbox = new HBox();
			
			VBox left = new VBox();
			left.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
			left.setPrefSize(220, 480);
			
			HBox gManage = new HBox();
			gManage.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
			gManage.setPrefWidth(220);
			gManage.setMinHeight(60);
			gManage.setAlignment(Pos.CENTER);
			gManage.setSpacing(20);
			
			Button nGroup = new Button("New Group");
			nGroup.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() { 
				   @Override 
				   public void handle(MouseEvent e) { 
					  try {
						  TextInputDialog dialog = new TextInputDialog("New Group");
						  
						  dialog.setTitle("New Group");
						  dialog.setHeaderText(null);
						  dialog.setContentText("Group Name:");
						   
						  Optional<String> result = dialog.showAndWait();
						   
						  result.ifPresent(name -> {
						      try {
								Http.newGroup(name);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						  });
					  }
					  catch(Exception err) {
						  
					  }
				   } 
				}
			);
			nGroup.setStyle("-fx-cursor: hand;-fx-border-color : #eee; -fx-border-width : 1 1 1 1; -fx-background-color: #FFFFFF;-fx-border-radius:0;-fx-background-radius:0");
			Button jGroup = new Button("Join Group");
			jGroup.setStyle("-fx-cursor: hand;-fx-border-color : #eee; -fx-border-width : 1 1 1 1; -fx-background-color: #FFFFFF;-fx-border-radius:0;-fx-background-radius:0");
			jGroup.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() { 
				   @Override 
				   public void handle(MouseEvent e) { 
					  try {
						  TextInputDialog dialog = new TextInputDialog("GroupID");
						  
						  dialog.setTitle("Join Group");
						  dialog.setHeaderText(null);
						  dialog.setContentText("Group ID:");
						   
						  Optional<String> result = dialog.showAndWait();
						   
						  result.ifPresent(name -> {
						      try {
								Http.joinGroup(name);
							} catch (IOException e1) {
								e1.printStackTrace();
							}
						  });
					  }
					  catch(Exception err) {
						  
					  }
				   } 
				}
			);
			
			gManage.getChildren().add(nGroup);
			gManage.getChildren().add(jGroup);
			
			groupList = new VBox();
			groupList.setPrefWidth(204);
			groupList.setMinHeight(420);
			groupList.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
			
			ScrollPane leftScroll = new ScrollPane();
			leftScroll.setContent(groupList);
			leftScroll.setPrefSize(204, 480);
			
			left.getChildren().add(gManage);
			left.getChildren().add(leftScroll);
			
			right = new VBox();
			right.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
			right.setPrefSize(420, 420);
			
			HBox userManage = new HBox();
			userManage.setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
			userManage.setPrefSize(420, 60);
			userManage.setAlignment(Pos.CENTER_RIGHT);
			userManage.setSpacing(10);
			userManage.setPadding(new Insets(0,10,0,0));
			
			iv = new Circle();
			if(!Objects.isNull(DB.user) &&!Objects.isNull(DB.user.get("photo"))) {
				Image im = new Image((String)DB.user.get("photo"),false);
	        	iv.setFill(new ImagePattern(im));
			}
			//Dashboard.class.getResource("/assets/user.png").toExternalForm();
			iv.setRadius(20);
			
			Text name = new Text((String)DB.user.get("name"));
			name.setFont(Font.font("Tahoma", FontWeight.BOLD, 15));
			name.setFill(Color.WHITE);
			Button logout = new Button("Logout");
			logout.setStyle("-fx-cursor: hand;-fx-border-color : #eee; -fx-border-width : 1 1 1 1; -fx-background-color: #FFFFFF;-fx-border-radius:0;-fx-background-radius:0");
			
			logout.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() { 
				   @Override 
				   public void handle(MouseEvent e) { 
					  try {
						  endgame = true;
						  Http.newToken("");
						  scene=null;
						  Main.getStage().setScene(Login.getScene());
					  }
					  catch(Exception err) {
						  
					  }
				   } 
				}
			);
			
	        userManage.getChildren().add(iv);
	        userManage.getChildren().add(name);
	        userManage.getChildren().add(logout);
	        
	        
	        empty = new VBox();
	        empty.setPrefSize(420, 420);
	        empty.setSpacing(30);
	        empty.setAlignment(Pos.CENTER);
	        empty.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
	        Text n1 = new Text("No Group Selected");
	        n1.setFont(Font.font("Tahoma", FontWeight.NORMAL, 24));
	        n1.setFill(Color.LIGHTGREY);
	        Text n2 = new Text("Click a group from the left");
	        n2.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
	        n2.setFill(Color.LIGHTGREY);
	        empty.getChildren().add(n1);
	        empty.getChildren().add(n2);
	        
			
			right.getChildren().add(userManage);
			right.getChildren().add(empty);
			
			hbox.getChildren().add(left);
			hbox.getChildren().add(right);
			
	        scene = new Scene(hbox, 640, 480);
	        
	        ut = new Updater(); 
            ut.start(); 
	       
		}
        return scene;
    }
	
}

class Updater extends Thread 
{ 
    public void run() 
    { 
        try
        { 
            while(true) {
            	DB.updateGroups();
            	if(Dashboard.isNewGroup(DB.groupsInfo)) {
            		
	            	Platform.runLater(() -> {
	                    try {
	                    	Dashboard.renderGroups();
	                    } catch (Exception e) {
	                    	e.printStackTrace();
	                    }
	                });
            	}
            	
            	DB.getAllChat();
            	Platform.runLater(() -> {
                    try {
                    	Dashboard.renderChats();
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }
                });
            	if(Dashboard.endgame)
            		break;
            	Thread.sleep(200);
            }
            Dashboard.endgame=false;
            System.out.println ("Out"); 
        } 
        catch (Exception e) 
        { 
        	e.printStackTrace();
            // Throwing an exception 
            System.out.println ("Exception is caught"); 
        } 
    } 
} 

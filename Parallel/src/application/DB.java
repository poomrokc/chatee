package application;
import java.util.Objects;
import java.io.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DB {
	
	private static final String appPath="";
	
	public static JSONObject member = new JSONObject();
	public static JSONArray groupsInfo = new JSONArray();
	public static JSONObject groupsLatest = new JSONObject();
	public static JSONObject groupsUnread = new JSONObject();
	public static JSONObject chats = new JSONObject();
	
	public static JSONObject user = new JSONObject();
	
	
	public static void updateGroups() {
		try {
			JSONArray groups = Http.getGroups();
			if(Objects.isNull(groups))
				return;
			groupsInfo = new JSONArray();
			for(int i=0;i<groups.size();i++) {
				JSONObject x = (JSONObject)groups.get(i);
				JSONObject g = new JSONObject();
				g.put("name", x.get("name"));
				g.put("_id", x.get("_id"));
				g.put("photo", x.get("photo"));
				g.put("memberCount", ((JSONArray)x.get("members")).size());
				g.put("latest", groupsLatest.get(x.get("_id")));
				g.put("unread", groupsUnread.get(x.get("_id")));
				groupsInfo.add(g);
				
				JSONArray memb = (JSONArray)x.get("members");
				for(int i2=0;i2<memb.size();i2++) {
					JSONObject m = (JSONObject)memb.get(i2);
					JSONObject f = new JSONObject();
					f.put("name", m.get("name"));
					f.put("photo", m.get("photo"));
					member.put(m.get("_id"), f);
				}
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void clearUnread(String id) {
		try {
			groupsUnread.put(id, 0);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void getAllChat() {
		try {
			for(int i=0;i<groupsInfo.size();i++) {
				JSONObject x = (JSONObject)groupsInfo.get(i);
				String id = (String)x.get("_id");
				String latest = Objects.isNull(groupsLatest.get(id))?"2000-01-01T00:00:00.000Z":(String)groupsLatest.get(id);
				JSONArray chat = Http.getChat(id, latest);
				JSONArray curr = Objects.isNull(chats.get(id))?new JSONArray():(JSONArray)chats.get(id);
				curr.addAll(chat);
				
				if(chat.size()>0) {
					JSONObject y = (JSONObject)chat.get(chat.size()-1);
					latest = (String)y.get("created");
				}
				int unread = Objects.isNull(groupsUnread.get(id))?0:Integer.parseInt(groupsUnread.get(id).toString());
				if(!id.equals(Dashboard.selected))
					unread += chat.size();
				
				groupsUnread.put(id, unread);
				groupsLatest.put(id, latest);
				chats.put(id, curr);
			}
			saveFileJSON((String)user.get("_id")+"_unread",groupsUnread.toJSONString());
			saveFileJSON((String)user.get("_id")+"_latest",groupsLatest.toJSONString());
			saveFileJSON((String)user.get("_id")+"_chats",chats.toJSONString());
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void saveFileJSON(String name,String data) {
		File file = new File(appPath+name);
	    try {
	    	if(!file.exists())
	    		file.createNewFile();
		    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(appPath+name, false), StandardCharsets.UTF_8));
		    writer.append(data);
		    writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void initDB() {
		JSONParser parser = new JSONParser();
		
		member = new JSONObject();
		groupsInfo = new JSONArray();
		groupsLatest = new JSONObject();
		groupsUnread = new JSONObject();
		chats = new JSONObject();
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(appPath+user.get("_id")+"_unread"), "UTF8"));
			String ur=reader.readLine();
			reader.close();
			Object obj = parser.parse(ur);    
			groupsUnread = (JSONObject)obj;
		} catch (Exception e) {
			groupsUnread = new JSONObject();
		}
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(appPath+user.get("_id")+"_latest"), "UTF8"));
			String lt=reader.readLine();
			reader.close();
			Object obj = parser.parse(lt);    
			groupsLatest = (JSONObject)obj;
		} catch (Exception e) {
			groupsLatest = new JSONObject();
		}
		
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(appPath+user.get("_id")+"_chats"), "UTF8"));
			String ch=reader.readLine();
			reader.close();
			Object obj = parser.parse(ch);    
			chats = (JSONObject)obj;
		} catch (Exception e) {
			chats = new JSONObject();
		}
	}
}

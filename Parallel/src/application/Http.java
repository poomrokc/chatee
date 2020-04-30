package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Objects;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import javafx.application.Platform;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;

public class Http {
	public static String token;
	private static final String appPath="";
	
	private static String getRequest(String url) throws IOException {
		final CloseableHttpClient httpclient = HttpClients.createDefault();
		final HttpGet httpget = new HttpGet(url);
		httpget.addHeader("Authorization", "JWT "+token);
		final HttpClientResponseHandler<String> responseHandler = new HttpClientResponseHandler<String>() {
            @Override
            public String handleResponse(
                final ClassicHttpResponse response) throws IOException {
                final int status = response.getCode();
                Header[] hh = response.getHeaders();
                for(int i=0;i<hh.length;i++) {
                	if(hh[i].getName().equals("Active-Server")) {
                		String sss = hh[i].getValue();
                		Platform.runLater(() -> {
    	                    try {
    	                    	Main.getStage().setTitle("CHATEE (Active Server: "+sss+")");
    	                    } catch (Exception e) {
    	                    	e.printStackTrace();
    	                    }
    	                });
                	}
                }
                final HttpEntity entity = response.getEntity();
                try {
                    return entity != null ? EntityUtils.toString(entity) : null;
                } catch (final ParseException ex) {
                    return null;
                }
            }
        };
        
        final String responseBody = httpclient.execute(httpget, responseHandler);
        return responseBody;
	}
	
	private static String postRequest(String url,JSONObject data) throws IOException {
		final CloseableHttpClient httpclient = HttpClients.createDefault();
		final HttpPost httpget = new HttpPost(url);
		httpget.addHeader("Authorization", "JWT "+token);
		StringEntity requestEntity = new StringEntity(
			    data.toJSONString(),
			    ContentType.APPLICATION_JSON);
		httpget.setEntity(requestEntity);
		final HttpClientResponseHandler<String> responseHandler = new HttpClientResponseHandler<String>() {
            @Override
            public String handleResponse(
                final ClassicHttpResponse response) throws IOException {
                final int status = response.getCode();
                Header[] hh = response.getHeaders();
                for(int i=0;i<hh.length;i++) {
                	if(hh[i].getName().equals("Active-Server")) {
                		String sss = hh[i].getValue();
                		Platform.runLater(() -> {
    	                    try {
    	                    	Main.getStage().setTitle("CHATEE (Active Server: "+sss+")");
    	                    } catch (Exception e) {
    	                    	e.printStackTrace();
    	                    }
    	                });
                	}
                }
                final HttpEntity entity = response.getEntity();
                try {
                    return entity != null ? EntityUtils.toString(entity) : null;
                } catch (final ParseException ex) {
                    return null;
                }
            }
        };
        
        final String responseBody = httpclient.execute(httpget, responseHandler);
        return responseBody;
	}
	
	public static void newToken(String tok) {
		token=tok;
	    File file = new File(appPath+"token");
	    try {
	    	if(!file.exists())
	    		file.createNewFile();
		    BufferedWriter writer = new BufferedWriter(new FileWriter(appPath+"token", false));
		    writer.append(tok);
		    writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void loadToken() {
		File tmpDir = new File(appPath+"token");
	    boolean exists = tmpDir.exists();
	    if(!exists)
	    	newToken("");
	    try {
			BufferedReader reader = new BufferedReader(new FileReader(appPath+"token"));
			token=reader.readLine();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean getAuth() throws IOException {
		String response = getRequest("https://hueco.ml/chat/profile");
		System.out.println(response);
		JSONParser parser = new JSONParser();
		try{
			Object obj = parser.parse(response);    
			JSONObject array = (JSONObject)obj;
			DB.user=array;
			if(Objects.isNull(array))
				return false;
			return true;
		}catch(org.json.simple.parser.ParseException pe) {
			newToken("");
			return false;
		}
	}
	
	public static JSONObject login(String username,String password) throws IOException {
		JSONObject data = new JSONObject();
		data.put("username", username);
		data.put("password", password);
		String response = postRequest("https://hueco.ml/chat/login",data);
		JSONObject ret = new JSONObject();
		JSONParser parser = new JSONParser();
		try{
			Object obj = parser.parse(response);    
			JSONObject array = (JSONObject)obj;
			newToken((String)array.get("token"));
			ret.put("success", true);
			return ret;
		}catch(org.json.simple.parser.ParseException pe) {
			ret.put("success", false);
			ret.put("msg", response);
			return ret;
		}
	}
	
	public static JSONObject register(String username,String password,String name) throws IOException {
		JSONObject data = new JSONObject();
		data.put("username", username);
		data.put("password", password);
		data.put("name", name);
		String response = postRequest("https://hueco.ml/chat/register",data);
		JSONObject ret = new JSONObject();
		JSONParser parser = new JSONParser();
		try{
			Object obj = parser.parse(response);    
			JSONObject array = (JSONObject)obj;
			newToken((String)array.get("token"));
			ret.put("success", true);
			return ret;
		}catch(org.json.simple.parser.ParseException pe) {
			ret.put("success", false);
			ret.put("msg", response);
			return ret;
		}
	}
	
	public static JSONArray getGroups() throws IOException {
		String response = getRequest("https://hueco.ml/chat/mygroups");
		JSONParser parser = new JSONParser();
		try{
			Object obj = parser.parse(response);    
			JSONObject array = (JSONObject)obj;
			return (JSONArray)array.get("data");
		}catch(org.json.simple.parser.ParseException pe) {
			return null;
		}
	}
	
	public static void sendChat(String groupID,String message) throws IOException {
		JSONObject data = new JSONObject();
		data.put("message", message);
		data.put("groupID", groupID);
		String response = postRequest("https://hueco.ml/chat/chat",data);
	}
	
	public static JSONArray getChat(String groupID,String timeStamp) throws IOException {
		String response = getRequest("https://hueco.ml/chat/myChat?groupID="+groupID+"&timeStamp="+timeStamp);
		JSONParser parser = new JSONParser();
		try{
			Object obj = parser.parse(response);    
			JSONObject array = (JSONObject)obj;
			return (JSONArray)array.get("data");
		}catch(org.json.simple.parser.ParseException pe) {
			return null;
		}
	}
	
	public static void newGroup(String groupName) throws IOException {
		JSONObject data = new JSONObject();
		data.put("name", groupName);
		String response = postRequest("https://hueco.ml/chat/createGroup",data);
	}
	
	public static void joinGroup(String groupID) throws IOException {
		JSONObject data = new JSONObject();
		data.put("groupID", groupID);
		String response = postRequest("https://hueco.ml/chat/joinGroup",data);
		System.out.println(response);
	}
	
	public static void leaveGroup(String groupID) throws IOException {
		JSONObject data = new JSONObject();
		data.put("groupID", groupID);
		String response = postRequest("https://hueco.ml/chat/leaveGroup",data);
		System.out.println(response);
	}
	
}

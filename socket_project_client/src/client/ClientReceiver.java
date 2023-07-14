package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.google.gson.Gson;

import dto.RequestBodyDto;
import lombok.Getter;

public class ClientReceiver extends Thread {

	private Gson gson = new Gson();
	
	@Override
	public void run() {
		gson = new Gson();
		SimpleGUIClient simpleGUIClient = SimpleGUIClient.getInstance();
		
		while (true) {
			try {
				BufferedReader bufferedReader =
						new BufferedReader(new InputStreamReader(simpleGUIClient.getSocket().getInputStream()));
				String requestBody = bufferedReader.readLine();
				
				requestController(requestBody);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void requestController(String requestBody) {
		
		String resource = gson.fromJson(requestBody, RequestBodyDto.class).getResource();
		
		switch (resource) {
		case "updateRoomList":
			updateRoomList(requestBody);
			break;
		case "showMessage":
			showMessage(requestBody);
			break;
		case "updateUserList":
			updateUserList(requestBody);
			break;
		case "exitUserList":
			exitUserList(requestBody);
			break;
			


		default:
			break;
		}
		
	}
	
	private void updateRoomList(String requestBody) {
		List<String> roomList = (List<String>) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		SimpleGUIClient.getInstance().getRoomListModel().clear();
		SimpleGUIClient.getInstance().getRoomListModel().addAll(roomList);
	}
	
	private void showMessage(String requestBody) {
		String messageContent = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		SimpleGUIClient.getInstance().getChattingTextArea().append(messageContent + "\n");;
	}
	
	private void updateUserList(String requestBody) {
		List<String> usernameList = (List<String>) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		SimpleGUIClient.getInstance().getUserListModel().clear();
		SimpleGUIClient.getInstance().getUserListModel().addAll(usernameList);
	}
	private void exitUserList(String requestBody) {
		List<String> usernameList = (List<String>) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		SimpleGUIClient.getInstance().getUserListModel().clear();
		SimpleGUIClient.getInstance().getUserListModel().addAll(usernameList);
	}
	
	
	
	
}








package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import dto.RequestBodyDto;
import dto.SendMessageDto;
import entity.Room;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConnectedSocket extends Thread {

	private final Socket socket;
	private Gson gson = new Gson();
	
	private String username;
	
	@Override
	public void run() {
		gson = new Gson();
		
		while (true) {
			try {
				// 지정된 파일의 입력을 버퍼링, readLine()을 호출할 때마다 파일에서 바이트를 읽고 문자로 변환한 다음 반환
				BufferedReader bufferedReader = 
						// 바이트를 읽고 지정된 문자 집합을 사용하여 문자로 디코딩합니다.
						// 최고의 효율성을 위해 InputStreamReader를 BufferedReader 내에 래핑하는 것을 고려합니다. 
						new BufferedReader(new InputStreamReader(socket.getInputStream()));
				// 으로 들어온 요청을 변환한 json을 requestBody에 대입
				String requestBody = bufferedReader.readLine();
				
				requestController(requestBody);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void requestController(String requestBody) {
		
		String resource = gson.fromJson(requestBody, RequestBodyDto.class).getResource();
		
		System.out.println(resource);
		switch (resource) {
			case "connection" : 
				connection(requestBody);
				break;
				
			case "createRoom" : 
				createRoom(requestBody);
				break;
				
			case "join" : 
				join(requestBody);
				break;
				
			case "sendMessage" : 
				sendMessage(requestBody);
				break;
				
			case "exitRoom" : 
				System.out.println(requestBody);
				exitRoom(requestBody);
				break;
				
				
				
			default :
				break;
		}
	}
	

	private void connection(String requestBody) {
		username = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		
		List<String> roomNameList = new ArrayList<>();
		
		SimpleGUIServer.roomList.forEach(room -> {
			roomNameList.add(room.getRoomName());
		});
		
		RequestBodyDto<List<String>> updateRoomListRequestBody = 
				new RequestBodyDto<List<String>>("updateRoomList", roomNameList);
		
		ServerSender.getInstance().send(socket, updateRoomListRequestBody);
	}
	
	
	private void createRoom(String requestBody) {
		String roomName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		
		username = username + "(방장)";
		
		Room newRoom = Room.builder()
				.roomName(roomName)
				.owner(username)
				.userList(new ArrayList<ConnectedSocket>())
				.build();
		
		SimpleGUIServer.roomList.add(newRoom);
		
		List<String> roomNameList = new ArrayList<>();
		
		SimpleGUIServer.roomList.forEach(room -> {
			roomNameList.add(room.getRoomName());
		});
		
		RequestBodyDto<List<String>> updateRoomListRequestBodyDto = 
				new RequestBodyDto<List<String>>("updateRoomList", roomNameList);

		SimpleGUIServer.connectedSocketList.forEach(con -> {
			ServerSender.getInstance().send(con.socket, updateRoomListRequestBodyDto);
		});
		
	}
	
	private void join(String requestBody) {
		String roomName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		
		
		
		SimpleGUIServer.roomList.forEach(room -> {
			if(room.getRoomName().equals(roomName)) {
				room.getUserList().add(this);
				
				List<String> usernameList = new ArrayList<>();
				room.getUserList().forEach(con-> {
					usernameList.add(con.username);
				});
				
				room.getUserList().forEach(connectedSocket -> {
					RequestBodyDto<List<String>> updateUserListDto = 
							new RequestBodyDto<List<String>>("updateUserList", usernameList);
					RequestBodyDto<String> joinMessageDto = 
							new RequestBodyDto<String>("showMessage", username + "님이 입장하셨습니다.");
					
					ServerSender.getInstance().send(connectedSocket.socket , updateUserListDto);

					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					ServerSender.getInstance().send(connectedSocket.socket, joinMessageDto);
				});
				
			}
		});		
		
	}
	
	private void sendMessage(String requestBody) {
		TypeToken<RequestBodyDto<SendMessageDto>> typeToken = new TypeToken<>() {};
		
		RequestBodyDto<SendMessageDto> requestBodyDto = gson.fromJson(requestBody, typeToken.getType());
		SendMessageDto sendMessage = requestBodyDto.getBody();
		
		SimpleGUIServer.roomList.forEach(room -> {
			if(room.getUserList().contains(this)) {
				room.getUserList().forEach(connectedSocket -> {
					RequestBodyDto<String> dto = 
							new RequestBodyDto<String>("showMessage", sendMessage.getFromUsername() + ": " + sendMessage.getMessageBody());
					ServerSender.getInstance().send(connectedSocket.socket, dto);
				});
			}
		});
		
	}
	
//	 소캣을 끊어버린다? 
//	 usernameList에서 지운다? > usernameList를 어캐 가져오지
	private void exitRoom(String requestBody) {
		
		String roomName = (String) gson.fromJson(requestBody, RequestBodyDto.class).getBody();
		
		SimpleGUIServer.roomList.forEach(room -> {
			if(room.getRoomName().equals(roomName)) {
				room.getUserList().remove(this);
				
				List<String> newUserList = new ArrayList<>();
				
				room.getUserList().forEach(con -> {
					newUserList.add(con.username);
				});
				
				room.getUserList().forEach(con -> {
					RequestBodyDto<List<String>> updateUserDto = 
							new RequestBodyDto<List<String>>("exitUserList", newUserList);
					
						ServerSender.getInstance().send(con.socket, updateUserDto);
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					RequestBodyDto<String> exitUserDto = 
							new RequestBodyDto<String>("showMessage", username + "님이 나가셨습니다.");
					
					ServerSender.getInstance().send(con.socket, exitUserDto);
					
				});
			}	
		});
	
	}
	
	private void clearRoom(String requestBody) {
		SimpleGUIServer.roomList.forEach(room -> {
			if(room.getOwner() == ) {
			
			}
			
		});
	}
	
}

package entity;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import server.ConnectedSocket;

@Builder
@Data
public class Room {
	// 서버에서 새로운 방을 만들때 사용할 틀

	private String roomName;
	private String owner;
	private List<ConnectedSocket> userList;
}

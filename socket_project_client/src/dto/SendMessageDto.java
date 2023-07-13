package dto;

import lombok.Builder;
import lombok.Data;

@Builder // > 클라이언트에서 채팅을 보낼 때 builder로 만들기 위해
@Data
public class SendMessageDto {
	// json으로 통신 할 때 requestBody의 body부분에 담아 보낼 객체

	private String fromUsername;
	private String toUsername; // 귓속말 할 때 사용할 것 같음
	private String messageBody;
	
}

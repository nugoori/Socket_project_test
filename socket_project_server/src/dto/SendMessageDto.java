package dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SendMessageDto {

	private String fromUsername;
	private String toUsername;
	private String messageBody;
}

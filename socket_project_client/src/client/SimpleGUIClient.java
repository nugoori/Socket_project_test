package client;

import java.awt.EventQueue;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Objects;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import dto.RequestBodyDto;
import dto.SendMessageDto;
import lombok.Getter;

import java.awt.CardLayout;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;

@Getter
public class SimpleGUIClient extends JFrame {
	
	// getInstance요청이 올 떄 마다 클라 생성?
	private static SimpleGUIClient instance;
	public static SimpleGUIClient getInstance() {
		if(instance == null) {
			instance = new SimpleGUIClient();
		}
		return instance;
	}
	
	private String username;
	private Socket socket;

	private JPanel mainCardPanel;
	private CardLayout mainCardLayout;
	
	private JTextField chattingTextField;
	
	private JPanel chattingRoomListPanel;
	private JScrollPane roomListScrollPanel;
	private DefaultListModel<String> roomListModel;
	private JList roomList;
	
	private JPanel chattingRoomPanel;
	private JTextArea chattingTextArea;
	
	private JScrollPane userListScrollPanel;
	private DefaultListModel<String> userListModel;
	private JList userList;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SimpleGUIClient frame = SimpleGUIClient.getInstance();
					frame.setVisible(true);
					
					ClientReceiver clientReceiver = new ClientReceiver();
					clientReceiver.start();
					
					RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("connection", frame.username);
					ClientSender.getInstance().send(requestBodyDto);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public SimpleGUIClient() {
		username = JOptionPane.showInputDialog(chattingRoomPanel, "아이디를 입력하세요");
		
		if(Objects.isNull(username)) {
			System.exit(0);
		}
		if(username.isBlank()) {
			System.exit(0);
		}
		
		try {
			socket = new Socket("127.0.0.1", 8800);
			
		}  catch (IOException e) {
			e.printStackTrace();
		}
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		setBounds(100, 100, 450, 300);
		
		mainCardLayout = new CardLayout();
		mainCardPanel = new JPanel();
		mainCardPanel.setLayout(mainCardLayout);
		setContentPane(mainCardPanel);
		
		chattingRoomListPanel = new JPanel();
		chattingRoomListPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
		mainCardPanel.add(chattingRoomListPanel, "chattingRoomListPanel");
		chattingRoomListPanel.setLayout(null);
		
		JButton createRoomButton = new JButton("방만들기");
		createRoomButton.setBounds(12, 10, 135, 30);
		createRoomButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String roomName = JOptionPane.showInputDialog(chattingRoomListPanel, "방 제목을 입력하세요.");
				if(Objects.isNull(roomName)) {
					return;
				}
				if(roomName.isBlank()) {
					JOptionPane.showMessageDialog(chattingRoomListPanel, "방 제목을 입력하세요.");
					return;
				}
				for(int i = 0; i < roomListModel.size(); i++) {
					if(roomListModel.get(i).equals(roomName)) {
						JOptionPane.showMessageDialog(chattingRoomListPanel, "이미 존재하는 방 제목입니다.");
						return;
					}
				}
				
				RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("createRoom", roomName);
				ClientSender.getInstance().send(requestBodyDto);
				
				mainCardLayout.show(mainCardPanel, "chattingRoomPanel");
				requestBodyDto = new RequestBodyDto<String>("join", roomName);
				ClientSender.getInstance().send(requestBodyDto);
				setTitle(roomName);
			}
		});
		chattingRoomListPanel.add(createRoomButton);
		
		roomListScrollPanel = new JScrollPane();
		roomListScrollPanel.setBounds(12, 50, 400, 191);
		chattingRoomListPanel.add(roomListScrollPanel);
		
		roomListModel = new DefaultListModel<String>();	
		roomList = new JList(roomListModel);
		
		roomList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2) {
					String roomName = roomListModel.get(roomList.getSelectedIndex());
					mainCardLayout.show(mainCardPanel, "chattingRoomPanel");
					RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("join", roomName);
					ClientSender.getInstance().send(requestBodyDto);
				}
			}
		});
		roomListScrollPanel.setViewportView(roomList);
		
		chattingRoomPanel = new JPanel();
		chattingRoomPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		chattingRoomPanel.setLayout(null);
		mainCardPanel.add(chattingRoomPanel, "chattingRoomPanel");
		
		JScrollPane chattingAreaScrollPanel = new JScrollPane();
		chattingAreaScrollPanel.setBounds(12, 10, 296, 189);
		chattingRoomPanel.add(chattingAreaScrollPanel);
		
		chattingTextArea = new JTextArea();
		chattingAreaScrollPanel.setViewportView(chattingTextArea);
		
		chattingTextField = new JTextField();
		chattingTextField.setBounds(12, 209, 296, 32);
		chattingTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					
					SendMessageDto sendMessageDto = SendMessageDto.builder()
							.fromUsername(username)
							.messageBody(chattingTextField.getText())
							.build();
					RequestBodyDto<SendMessageDto> requestBodyDto = 
							new RequestBodyDto<>("sendMessage", sendMessageDto); 
					
					ClientSender.getInstance().send(requestBodyDto);
					chattingTextField.setText("");
				}
			}
		});
		chattingRoomPanel.add(chattingTextField);
		chattingTextField.setColumns(10);
		
		userListScrollPanel = new JScrollPane();
		userListScrollPanel.setBounds(320, 10, 92, 189);
		chattingRoomPanel.add(userListScrollPanel);
		
		userListModel = new DefaultListModel<>();
		userList = new JList(userListModel);
		userListScrollPanel.setViewportView(userList);
		
		JButton exitRoomButton = new JButton("나가기");
		exitRoomButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JOptionPane.showConfirmDialog(chattingRoomPanel, "방을 나가시겠습니까.");
				
//				String roomName = roomListModel.get(roomList.getSelectedIndex()); > getSelectedIndex 오류남
				
				RequestBodyDto<String> requestBodyDto = new RequestBodyDto<String>("exitRoom", "");
				ClientSender.getInstance().send(requestBodyDto);

			}
			
		});
		exitRoomButton.setBounds(320, 209, 92, 32);
		
		chattingRoomPanel.add(exitRoomButton);
		
		

	}
}

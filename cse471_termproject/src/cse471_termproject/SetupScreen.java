package cse471_termproject;



import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;

public class SetupScreen {
	
	private static final int HEIGHT = 30; 
	public static JFrame frame;
    public static JTextField folderField, secretField;
    private static Peer currentPeer;
    public static StartScreen startScreenPanel;
    public static JPanel mainPanel;
    private static ArrayList<String> sharedFileNames = new ArrayList<>();; 
    
    
	public static void main(String[] args) {
		frame = new JFrame("P2P File Sharing App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 400); 


        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu helpMenu = new JMenu("Help");
        
        
        JMenuItem connectItem = new JMenuItem("Connect");
        JMenuItem disconnectItem = new JMenuItem("Disconnect");
        JMenuItem exitItem = new JMenuItem("Exit");

        connectItem.addActionListener(e -> connect());
        disconnectItem.addActionListener(e -> disconnect());
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(connectItem);
        fileMenu.add(disconnectItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutInfo());
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(helpMenu);
        
        mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.EAST;

        
        JLabel folderLabel = new JLabel("Shared Folder Location:");
        folderLabel.setPreferredSize(new Dimension(folderLabel.getPreferredSize().width+2, HEIGHT));
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(folderLabel, gbc);

      
        folderField = new JTextField(20);
        folderField.setPreferredSize(new Dimension(folderField.getPreferredSize().width, HEIGHT));
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(folderField, gbc);

        
        JLabel secretLabel = new JLabel("Shared Secret:");
        secretLabel.setPreferredSize(new Dimension(secretLabel.getPreferredSize().width, HEIGHT));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        mainPanel.add(secretLabel, gbc);

       
        secretField = new JTextField(20);
        secretField.setPreferredSize(new Dimension(secretField.getPreferredSize().width, HEIGHT));
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        mainPanel.add(secretField, gbc);

        
        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchToStartScreen(folderField,secretField);
            }
        });
        startButton.setPreferredSize(new Dimension(startButton.getPreferredSize().width, HEIGHT));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(startButton);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(buttonPanel, gbc);

       
        frame.setContentPane(mainPanel);
        frame.setJMenuBar(menuBar);

        
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

	}

	private static void showAboutInfo() {
		 JOptionPane.showMessageDialog(null, "Developer Info:\nEmirhan Onder\nemirhan.onder@std.yeditepe.edu.tr\n20190702008",
	                "About", JOptionPane.INFORMATION_MESSAGE);
		
	}

	private static void disconnect() {
		if(currentPeer != null) {
			currentPeer.stop();
			
			System.out.println("Peer durduruldu!!");
		}
	}

	private static void connect() {
		try {
		String sharedSecret = secretField.getText();
        if (sharedSecret.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Shared secret bos olamaz.", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int port = 8080;
        
        String myIp = NetworkUtils.getLocalAddress();
        //String myIp = InetAddress.getLocalHost().getHostAddress();
        System.out.println("benim ip: "+ myIp);
        String sharedFolderPath = folderField.getText();
        File sharedFolder = new File(sharedFolderPath);
        
        if(sharedFolder != null) {
		    String contents[] = sharedFolder.list();
		
		    for(int i=0; i<contents.length; i++) {
		        
		        sharedFileNames.add(contents[i]);
		    }
	    }
        
		PeerInfo peerInfo = new PeerInfo(1, myIp, port, sharedSecret, sharedFileNames,sharedFolderPath); 
		
		currentPeer = new Peer(peerInfo);
		
		new Thread(currentPeer).start();
		
		System.out.println("peer olustu: "+ sharedSecret);
		
		} catch (Exception e) {
	        e.printStackTrace();
	        JOptionPane.showMessageDialog(frame, "Peer baslatılırken hata olustu: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
	    }
		
	}
	
	
	private static void switchToStartScreen(JTextField folderField, JTextField secretField) {
        
		if(!folderField.getText().equalsIgnoreCase("") && !secretField.getText().equalsIgnoreCase(""))
		{
			frame.getContentPane().removeAll(); 
        	startScreenPanel = new StartScreen();
        	startScreenPanel.setPeer(currentPeer);
        	frame.setContentPane(startScreenPanel); 
        	frame.validate(); 
        	frame.repaint(); 
		}
    }

}

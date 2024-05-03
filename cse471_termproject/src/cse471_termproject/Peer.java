package cse471_termproject;



import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.SwingUtilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
public class Peer implements Runnable {
	private volatile boolean running = true;
	private static PeerInfo peerInfo;
	private static String sharedSecret;
	private HashMap<String,String> peersList;
	public static HashMap<String,String> fileList;
	public static HashMap<String,String> fileNetworkPairList;
	private HashMap<String,Long> lastBroadcastList;
	private DatagramSocket dSocket = null;
	private DatagramSocket dS = null;
	private DatagramSocket dS2 = null;
	private DatagramSocket dSckt = null;
	private Socket socket;
	private ServerSocket ds3;
	RandomAccessFile  rAF;
	public static HashMap<String, Set<String>> checksumPeerMap = new HashMap<>();
	public static HashMap<String, Set<String>> fileChecksumMap = new HashMap<>();

	
	
	public Peer(PeerInfo peerInfo) {
        this.peerInfo = peerInfo;
        Peer.sharedSecret = peerInfo.getPeerSharedSecret();
        this.peersList = new HashMap<String,String>();
        this.lastBroadcastList = new HashMap<String,Long>();
        this.fileList = new HashMap<String,String>();
        this.fileNetworkPairList = new HashMap<String,String>();
    }

	public void stop() {
		running = false;
		if(dSocket != null && dSocket.isClosed()) {
			dSocket.close();	
		}
		
		peersList.remove(peerInfo.getPeerAddress());
		fileList.remove(peerInfo.getSharedFiles().stream().collect(Collectors.joining(",")));
		if(peersList.size()==0)
		{
			SwingUtilities.invokeLater(() -> {
			    JList<String> networksList = SetupScreen.startScreenPanel.getComputersList();
			    JList<String> filesList = SetupScreen.startScreenPanel.getFilesList();
			    DefaultListModel<String> model = (DefaultListModel<String>) networksList.getModel();
			    DefaultListModel<String> model2 = (DefaultListModel<String>) filesList.getModel();
			    Set<String> keys = peersList.keySet();
			    Set<String> keys2 = fileList.keySet();
			    
			    model.clear();
			    model2.clear();
			    for (String ip : keys) {
			    	if (!model.contains(ip)) {
			            model.addElement(ip);
			        }
			    }
			    
			    for (String files : keys2)
			    {
			    	String [] splittedFileName = files.split(",");
			    	
			    	ArrayList<String> filesAdded = new ArrayList<>();
			    	for(String part : splittedFileName)
			    	{
			    		filesAdded.add(part);
			    	}
			    	
			    	for(String f1 : filesAdded)
			    	{
			    		if (!model2.contains(f1)) {
				            model2.addElement(f1);
				            
				        }
			    	}
			    	
			    }
   
			});
		}

	}
	
	
	@Override
	public void run() {
       
        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendBroadcast();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        sendThread.start();

        Thread listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                listenBroadcast();
            }
        });
        listenThread.start();
        
       
       Thread sendFileThread = new Thread(new Runnable() {
        	@Override
            public void run() {	
                try {
					sendFiles();
				} catch (Exception e) {
					
					e.printStackTrace();
				}
            }
        });
        sendFileThread.start();
        
       
        
        
        Thread listendownloadFileThread = new Thread(new Runnable() {
        	@Override
            public void run() {	
                try {
                	listenDownload();
				} catch (Exception e) {
					
					e.printStackTrace();
				}
            }
        });
        listendownloadFileThread.start();

    }
	
	public void listenDownload() throws Exception {
		
		ServerSocket serverSocket = new ServerSocket(5555);
		
		try {
			while (true) { 
				Socket incoming = serverSocket.accept(); 
				Thread thread = new Thread(() -> {
					try {
						handleClient(incoming); 
					} 
					catch (Exception e) {
						e.printStackTrace(); 
					}
				});
				thread.start(); 
			}
		} 	
		catch (Exception e) {
			e.printStackTrace(); 
		}
   
	}
	
	private void handleClient(Socket clientSocket) throws IOException {
		System.out.println("Baglant� kuruldu");
	    DataInputStream dIS = new DataInputStream(clientSocket.getInputStream());
	    DataOutputStream dOS = new DataOutputStream(clientSocket.getOutputStream());
	
	    int len = dIS.read();
	    System.out.println("len : " +len );
	    byte[] received = new byte[len];
	    dIS.readFully(received);
	    String all = new String(received, "UTF-8");
	    
	    String checkSum = all.split(":")[0];
	    int peerNumber = Integer.parseInt(all.split(":")[1]);
	    int numberOfPeers = Integer.parseInt(all.split(":")[2]);
	    
	    String fileNames = null;
	    
	    for (Map.Entry<String, String> entry : fileList.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            System.out.println("key : " + key);
            System.out.println("value : "+ value);
            System.out.println("peer �p : "+ peerInfo.getPeerAddress());
            if ( value.compareTo(peerInfo.getPeerAddress())== 0)
            {
            	System.out.println("ifin i�ine girrdi");
            	fileNames = key;
            }
            System.out.println("for i�inde file names :" + fileNames);
        }
	    String fileName = null;
	    
	    try {
	    	System.out.println("dosya ismi" +fileNames);
	    	System.out.println("checksum : "+ checkSum);
            // Dosya isimlerini bul ve her birinin checksum'ını hesapla
            fileName = findFileWithChecksum(SetupScreen.folderField.getText().toString(),fileNames, checkSum);
            
            if (fileName != null) {
                System.out.println("Eşleşen dosya: " + fileName);
            } else {
                System.out.println("Checksum ile eşleşen dosya bulunamadı.");
            }
        } 
	    catch (Exception e) {
        	e.printStackTrace();
        }
        
	    
	    
	    
	    System.out.println("peerNumber : " +peerNumber );
	    System.out.println("numberOfPeers : " +numberOfPeers );
	    System.out.println("File ismi received : " + fileName);
	    
	    String folderName = SetupScreen.folderField.getText().toString();
	    File file = new File(folderName, fileName);
	
	    System.out.println("file nameee:" + file.getName());
	    System.out.println("file parent" + file.getParent());
	    long fileSize = file.length();
	    System.out.println("File Size : "+ fileSize);
	    long chunkSize = 512000;
	    System.out.println("Chunk Size : "+ chunkSize);
	    long chunkCount = fileSize/chunkSize;
	    
	    long downloadedChunk = (peerNumber == numberOfPeers-1) ? chunkCount- ((chunkCount/numberOfPeers)*(numberOfPeers-1)) : chunkCount/numberOfPeers ;
	    
	    if (file.exists()) {
	        long startByte = peerNumber * chunkSize * (chunkCount / numberOfPeers);
	        long endByte = (peerNumber == numberOfPeers - 1) ? fileSize : (startByte + (chunkSize*downloadedChunk));
	        dOS.writeLong(endByte - startByte);
	        try (FileInputStream fis = new FileInputStream(file)) {
	            fis.skip(startByte);
	            byte[] buffer = new byte[4096];
	            long bytesSent = 0;
	            int bytesRead;
	            while (bytesSent < (endByte - startByte) && (bytesRead = fis.read(buffer, 0, (int) Math.min(buffer.length, (endByte - startByte) - bytesSent))) != -1) {
	                dOS.write(buffer, 0, bytesRead);
	                dOS.flush();
	                bytesSent += bytesRead;
	            }
	        }
	    }
	
	    dIS.close();
	    dOS.close();
	}
	
	
	public void sendFiles() throws Exception {
		try {
			dSckt = new DatagramSocket();
			
			String fileNamesData = peerInfo.getSharedFiles().stream().collect(Collectors.joining(","));
			String mySharedSecret = peerInfo.getPeerSharedSecret();
			String myIpAddress = peerInfo.getPeerAddress();
			String mySharedLocation = peerInfo.getSharedLocation();


			ArrayList<String> checkSumArr = new ArrayList<>();
			for(int i=0; i<peerInfo.getSharedFiles().size();i++)
			{
				File file = new File(mySharedLocation,peerInfo.getSharedFiles().get(i));
				String checksum = FileChecksumHelper.calculateChecksum(file);
				checkSumArr.add(checksum);
			}
			
			String checksumData = checkSumArr.stream().collect(Collectors.joining(":"));
			System.out.println("FileNameData : "+ fileNamesData);
			byte[] sendData = (fileNamesData+" "+mySharedSecret+" "+checksumData+" "+mySharedLocation).getBytes();
			DatagramPacket packet;
			
			while(running)
			{
				packet = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8082);
		        dSckt.send(packet);
		        Thread.sleep(2000);
			}
			
			
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			if(dSckt != null && dSckt.isClosed()) {
				dSckt.close();
			}
		}
		
	}
	
	public void sendBroadcast() throws IOException {
		try {
			System.out.println("sendbroadcast'e girdi");
			
				dSocket = new DatagramSocket();
		        String peerData = peerInfo.getPeerAddress() + "," + peerInfo.getPeerSharedSecret();
		        System.out.println("Peer Data : "+ peerData);
		        byte[] sendData = peerData.getBytes();
		        DatagramPacket packet;
		        while(running)
				{
					packet = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 8081);
			        dSocket.send(packet);
			        Thread.sleep(3000);
				}
        } 
        catch (Exception e) {
			
			e.printStackTrace();
		}
		finally {
			if(dSocket != null && dSocket.isClosed()) {
				dSocket.close();
			}
		}
	}
	
	public void listenBroadcast() {
		try {
	        dS = new DatagramSocket(8081);
	        dS2 = new DatagramSocket(8082);
	
	        byte[] receiveData = new byte[1024];
	        byte[] receiveData2 = new byte[2048];
	        
	        while(running) {
	            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	            dS.receive(receivePacket);
	            
	            DatagramPacket receivePacket2 = new DatagramPacket(receiveData2,receiveData2.length);
	            dS2.receive(receivePacket2);
	            
	            String receivedData = new String(receivePacket.getData(), receivePacket.getOffset(), receivePacket.getLength());
	            String[] parts = receivedData.split(",");
	            
	            String receivedData2 = new String(receivePacket2.getData(),receivePacket2.getOffset(),receivePacket2.getLength());
	            String[] parts2 = receivedData2.split(" ");
	            
	            if(parts.length == 2) {
	               // String receivedIP = parts[0];
	                String receivedSecret = parts[1];
	                String receivedIP = receivePacket.getAddress().getHostAddress();
	          //      System.out.println("ip broadcast - packet ip : "+ receivedIP);
	                
	                if(receivedSecret.equals(sharedSecret)) {
	                	
	                	//peersList.computeIfAbsent(receivedIP, k-> new PeerBroadcastData(receivedSecret)).updateLastBroadcastTime();
	                    peersList.put(receivedIP, receivedSecret);
	                    lastBroadcastList.put(receivedIP, System.currentTimeMillis());
	                    //System.out.println("Eslesen Peer: " + receivedIP);       
	                  
	                }
	            }
	            
	            if(parts2.length == 4) {
	            	String receivedFileNames = parts2[0];
	            	String receivedSecret = parts2[1];
	            	String receivedChecksum = parts2[2];
	            	String receivedFolderLocation = parts2[3];
	            	String receivedIp = receivePacket2.getAddress().getHostAddress();
	            	
	         
	            	if(receivedSecret.equals(sharedSecret)) {
	                	String[] file_Names = receivedFileNames.split(",");
	            		String[] checkSums = receivedChecksum.split(":"); 
	                    fileList.put(receivedFileNames, receivedIp);
	                    fileNetworkPairList.put(receivedFileNames, receivedFolderLocation);
	                    for(int i=0 ; i<checkSums.length; i++)
	                    {
	                    	String fileName = file_Names[i];
                			String checksum = checkSums[i];
	                    	checksumPeerMap.computeIfAbsent(checksum, k -> new HashSet<>()).add(receivedIp);
	                    	fileChecksumMap.computeIfAbsent(checksum, k -> new HashSet<>()).add(fileName);
	                    }
	                  

	                  
	                }
	            	
	            }
	            
	            HashMap<String,String> pairsToRemove = new HashMap<String,String>();
	            for (Map.Entry<String, String> entry : peersList.entrySet()) {
	            	String key = entry.getKey();
	            	long currentTime = System.currentTimeMillis();
	            	long broadcastTime = lastBroadcastList.getOrDefault(key, 0L);
	            	
	            	if(currentTime - broadcastTime > 10000)
	            	{
	            		System.out.println("Peer timed out!! ");
	            		pairsToRemove.put(entry.getKey(),entry.getValue());
	            	}
	            	
	            }
	            
	            for (Map.Entry<String, String> entry : pairsToRemove.entrySet())
	            {
	            	String key = entry.getKey();
	            	peersList.remove(key);
	            	lastBroadcastList.remove(key);
	            }
	            
	            Iterator<Map.Entry<String, String>> iter = fileList.entrySet().iterator();
	            
	            while(iter.hasNext())
	            {
	            	Map.Entry<String, String> entry = iter.next();
	            	String ipAddress = entry.getValue();

	                if (!peersList.containsKey(ipAddress)) {
	                    iter.remove();     
	                   
	                }
	            }
 
	            
	       /*     for (Map.Entry<String, String> entry : fileChecksumMap.entrySet()) {
	                String key = entry.getKey();
	                String value = entry.getValue();
	                System.out.println("Anahtar = " + key + ", Deger = " + value); 
	            }
	            */
	            if(SetupScreen.startScreenPanel!=null)
	            {
	            	SwingUtilities.invokeLater(() -> {
					    JList<String> networksList = SetupScreen.startScreenPanel.getComputersList();
					    JList<String> filesList = SetupScreen.startScreenPanel.getFilesList();
					    DefaultListModel<String> model = (DefaultListModel<String>) networksList.getModel();
					    DefaultListModel<String> model2 = (DefaultListModel<String>) filesList.getModel();
					    Set<String> keys = peersList.keySet();
					    Set<String> keys2 = fileList.keySet();
					    
					    model.clear();
					    model2.clear();
					    for (String ip : keys) {
					    	if (!model.contains(ip)) {
					            model.addElement(ip);
					        }
					    }
					    
					    for (String files : keys2)
					    {
					    	String [] splittedFileName = files.split(",");
					    	
					    	ArrayList<String> filesAdded = new ArrayList<>();
					    	for(String part : splittedFileName)
					    	{
					    		filesAdded.add(part);
					    	}
					    	
					    	for(String f1 : filesAdded)
					    	{
					    		if (!model2.contains(f1)) {
						            model2.addElement(f1);
						            
						        }
					    	}
					    	
					    }
					    
					    
					});
	            	
	            	
	            }
	            
	        }
	    } catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if(dS != null && dS.isClosed()) {
				dS.close();
			}
		}
		
	}

	private static String findFileWithChecksum(String folderPath, String files, String targetChecksum) throws Exception {
        
        String[] fileNames = files.split(",");

        System.out.println("Gelen folderPath : " + folderPath);
        System.out.println("Gelen files  : " + files);
        for (String fileName : fileNames) {
            File file = new File(folderPath,fileName);
            if (file.exists()) {
                
                String checksum = FileChecksumHelper.calculateChecksum(file);

                
                if (checksum.equals(targetChecksum)) {
                    return fileName;
                }
            }
        }
    
        return null;
    }
	public void setSharedSecret(String sharedSecret) {
		this.sharedSecret = sharedSecret;
	}

	public static String getSharedSecret() {
		return sharedSecret;
	}

	
	
}

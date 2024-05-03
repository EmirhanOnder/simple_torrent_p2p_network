																																																			package cse471_termproject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class StartScreen extends JPanel {

	private Peer currPeer;
    private JList<String> computersList;
    private JList<String> filesList;
    private JList<DownloadInfo> transfersList;
    private DefaultListModel<DownloadInfo> transfersModel;
    private Socket socket = null;
    private DataInputStream dis = null;
    private DataOutputStream dos = null;
  
   

    public StartScreen() {
        this.setLayout(new BorderLayout(10, 10));
        initUI();
        transfersModel = new DefaultListModel<>();
        transfersList.setModel(transfersModel);
        transfersList.setCellRenderer(new DownloadCellRenderer());
      
    }

    private void initUI() {
        
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new GridLayout(1, 2)); 

        JPanel computersPanel = new JPanel();
        computersPanel.setLayout(new BorderLayout());
        JLabel computersLabel = new JLabel("Computers in Network");
        computersList = new JList<>(new DefaultListModel<>());
        JScrollPane computersScrollPane = new JScrollPane(computersList);
        computersPanel.add(computersLabel, BorderLayout.NORTH);
        computersPanel.add(computersScrollPane, BorderLayout.CENTER);

        JPanel filesPanel = new JPanel();
        filesPanel.setLayout(new BorderLayout());
        JLabel filesLabel = new JLabel("Files Found");
        filesList = new JList<>(new DefaultListModel<>());
        JScrollPane filesScrollPane = new JScrollPane(filesList);
        filesPanel.add(filesLabel, BorderLayout.NORTH);
        filesPanel.add(filesScrollPane, BorderLayout.CENTER);

        northPanel.add(computersPanel);
        northPanel.add(filesPanel);

        JPanel transfersPanel = new JPanel();
        transfersPanel.setLayout(new BorderLayout());
        transfersPanel.setBorder(BorderFactory.createTitledBorder("File Transfers"));
        transfersList = new JList<>();
        JScrollPane transfersScrollPane = new JScrollPane(transfersList);
        transfersPanel.add(transfersScrollPane, BorderLayout.CENTER);
        
        filesList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
            	String ipToDownload = null ;
            	String folderPathToDownload = null;
            	String fileChecksum = null;
            	
                JList list = (JList)evt.getSource();
                if (evt.getClickCount() == 2) { 
                    int index = list.locationToIndex(evt.getPoint());
                    String selectedFile = (String)list.getModel().getElementAt(index);
                    
                    for (Map.Entry<String, Set<String>> entry : currPeer.fileChecksumMap.entrySet()) {
                        Set<String> files = entry.getValue();
                        if (files.contains(selectedFile)) {
                            fileChecksum = entry.getKey();
                            break;  
                        }
                    }

            	    
                    ArrayList<String> associatedIPs = new ArrayList<>();
                    
                    
                    for (Entry<String, Set<String>> entry : currPeer.checksumPeerMap.entrySet()) {
                        if (entry.getKey().contains(fileChecksum)) {
                            //System.out.println("Aranan kelimeyi içeren anahtar: " + entry.getKey());
                            //System.out.println("Bu anahtara karşılık gelen değer: " + entry.getValue());
                        	associatedIPs.addAll(entry.getValue());
                            
                        }
                    }
                    
                    for (Map.Entry<String, String> entry : currPeer.fileNetworkPairList.entrySet()) {
                        if (entry.getKey().contains(selectedFile)) {
                            //System.out.println("Aranan kelimeyi içeren anahtar: " + entry.getKey());
                            //System.out.println("Bu anahtara karşılık gelen değer: " + entry.getValue());
                            folderPathToDownload  = entry.getValue();
                            
                            break;
                        }
                    }
                    
                    final String finalChecksum = fileChecksum;
                    final String finalIpToDownload = ipToDownload;
                    final String finalFolderPathToDownload = folderPathToDownload;
                    
                    Thread downloadFileThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
									downloadFile(associatedIPs,finalFolderPathToDownload,selectedFile,finalChecksum);
								
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    downloadFileThread.start();    
                }
            }
        });
        

        this.add(northPanel, BorderLayout.NORTH);
        this.add(transfersPanel, BorderLayout.CENTER);
        
        
        
        
    }
    public void downloadFile(ArrayList<String> ipsToDownload,String folderPathToDownload,String selectedFile,String checksum) throws Exception {
    	try {
    			int threadCount = ipsToDownload.size();
    			CountDownLatch latch = new CountDownLatch(threadCount);
    			//long[] fileSize = new long[1];
    			ArrayList<File> partFiles = new ArrayList<>();
    			System.out.println("array size : " + ipsToDownload.size());
    			
    			for(int i=0; i<threadCount; i++)
    			{
    				final int a = i;
    				new Thread(new Runnable() {
    						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							
							try {
	    						Socket socket = new Socket(ipsToDownload.get(a), 5555);
	    						DataInputStream dis = new DataInputStream(socket.getInputStream());
	    						DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
	    		    	        

	    		    	        String name = checksum + ":" + a + ":"+ipsToDownload.size();
	    				        byte[] mesajBytes = name.getBytes("UTF-8");
	    				        
	    				        
	    				        dos.write((byte)mesajBytes.length);
	    				        dos.write(mesajBytes);
	    				        
	    				        dos.flush();
	    				        
	    				      //  synchronized(fileSize) {
	    				       // 	if(fileSize[0] == 0)
	    				       // 	{
	    				        	long fileSize = dis.readLong();
	    				        //	}
	    				        //}
	    				        
	    				        DownloadInfo newDownload = new DownloadInfo(selectedFile,fileSize);
	    				        SwingUtilities.invokeLater(() -> transfersModel.addElement(newDownload));
	    				        
	    				        long totalSize = fileSize;
	    				        System.out.println("Total size "+ totalSize);
	    				     /*   long chunkSize = totalSize / ipsToDownload.size();
	    				        long startByte = chunkSize * a;
	    				        long endByte = (a == ipsToDownload.size()-1) ? totalSize : startByte+chunkSize;*/
	    				        File tempFile = new File(SetupScreen.folderField.getText().toString(),selectedFile + ".part"+a);
	    				        partFiles.add(tempFile);
	    				        try(FileOutputStream fos= new FileOutputStream(tempFile))
	    				        {
	    				        	long remaining = totalSize;
	    				        	byte[] buffer = new byte[4096];
	    				        	while(remaining > 0 )
	    				        	{
	    				        		int read = dis.read(buffer,0,(int)Math.min(buffer.length, remaining));
	    				        		if(read== -1) break;
	    				        		fos.write(buffer,0,read);
	    				        		remaining -= read;
	    				        		
	    				        		newDownload.setDownloadedBytes(read);
	    				        		SwingUtilities.invokeLater(() -> updateProgress(newDownload));
	    				        	}
	    				        }
	    				        
	    				        newDownload.setComplete(true);
	    				        SwingUtilities.invokeLater(() -> updateProgress(newDownload));
	    				        
	    				    
	    					}catch(Exception e) {
	    						
	    					}
							finally {
		                        
		                        latch.countDown();
		                    }
						}}).start();
    			}
    			System.out.println("Thread sonland�rma �ncesi");
    			latch.await();
    			System.out.println("Thread sonland�rma sonras�");
    			mergeParts(partFiles,new File(SetupScreen.folderField.getText().toString(),selectedFile));
	
		} catch (Exception e) {
		
		e.printStackTrace();
	} 
	
    }
    
    public static void mergeParts(ArrayList<File> partsFiles, File outputFile) throws IOException {
    	System.out.println("Merge part fonksiyonuna girdi");
    	
    	partsFiles.sort((file1, file2) -> {
            String partNumber1 = file1.getName().substring(file1.getName().indexOf(".part") + 5);
            String partNumber2 = file2.getName().substring(file2.getName().indexOf(".part") + 5);
            return Integer.compare(Integer.parseInt(partNumber1), Integer.parseInt(partNumber2));
        });
    	
    	try(FileOutputStream fos = new FileOutputStream(outputFile);
    			BufferedOutputStream mergingStream = new BufferedOutputStream(fos)){
    		for(File f: partsFiles)
    		{
    			Files.copy(f.toPath(), mergingStream);
    			f.delete();
    		}
    	}
    			
    }
    
    private void updateProgress(DownloadInfo download) {
        SwingUtilities.invokeLater(() -> {
            int index = transfersModel.indexOf(download);
            if (index != -1) {
                transfersModel.set(index, download);
                transfersList.repaint();
            }
        });
    }
    
    public class DownloadInfo {
        private String fileName;
        private long downloadedBytes;
        private long totalBytes;
        private boolean isComplete;

        
        public DownloadInfo(String fileName, long totalBytes) {
            this.fileName = fileName;
            this.downloadedBytes = 0; 
            this.totalBytes = totalBytes;
            this.isComplete = false;
        }

        
        public String getFileName() {
            return fileName;
        }

        
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

       
        public long getDownloadedBytes() {
            return this.downloadedBytes;
        }

        
        public void setDownloadedBytes(long downloadedBytes) {
            this.downloadedBytes += downloadedBytes;
        }

        
        public long getTotalBytes() {
            return totalBytes;
        }

        
        public void setTotalBytes(long totalBytes) {
            this.totalBytes = totalBytes;
        }

       
        public boolean isComplete() {
            return isComplete;
        }

        
        public void setComplete(boolean complete) {
            isComplete = complete;
        }
    }
    
    class DownloadCellRenderer extends DefaultListCellRenderer {
        private final JProgressBar progressBar = new JProgressBar(0, 100);
        private final JPanel panel = new JPanel(new BorderLayout());
        private final JLabel label = new JLabel();

        public DownloadCellRenderer() {
            panel.add(label, BorderLayout.CENTER);
            panel.add(progressBar, BorderLayout.EAST);
            progressBar.setStringPainted(true);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (value instanceof DownloadInfo) {
                DownloadInfo info = (DownloadInfo) value;
                if(info.getTotalBytes()> (1024*1024*1024))
                {
                	label.setText(info.getFileName() + " - " + info.getDownloadedBytes()/(1024*1024*1024)+ " GB" + "/" + info.getTotalBytes()/(1024*1024*1024)+" GB");
                }
                else if(info.getTotalBytes() > (1024*1024))
                {
                	label.setText(info.getFileName() + " - " + info.getDownloadedBytes()/(1024*1024)+ " MB" + "/" + info.getTotalBytes()/(1024*1024)+" MB");
                }
                else if(info.getTotalBytes() > 1024)
                {
                	label.setText(info.getFileName() + " - " + info.getDownloadedBytes()/(1024)+ " KB" + "/" + info.getTotalBytes()/(1024)+" KB");
                }
                else
                {
                	label.setText(info.getFileName() + " - " + info.getDownloadedBytes()+ " Bytes" + "/" + info.getTotalBytes()+ " Bytes");
                }
                
                int progress = (int) ((info.getDownloadedBytes() * 100.0f) / info.getTotalBytes());
                progressBar.setValue(progress);
            }

            return panel;
        }
    }
    

    public JList<String> getComputersList() {
        return computersList;
    }
    
    public void setTransfersList(JList<DownloadInfo> transfersList) {
        this.transfersList = transfersList;
    }

    public JList<String> getFilesList() {
        return filesList;
    }

    public JList<DownloadInfo> getTransfersList() {
        return transfersList;
    }
    
    public void setPeer(Peer peer) {
        this.currPeer = peer;
    }
}

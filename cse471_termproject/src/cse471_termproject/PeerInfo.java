package cse471_termproject;

import java.util.ArrayList;
import java.util.List;

public class PeerInfo {

	private int peerId;
	private String peerAddress;
	private int peerPort;
	private String peerSharedSecret;
	private ArrayList<String> sharedFiles;
	private String sharedFolderLocation;

	
	
	public PeerInfo(int pId, String pAddress, int pPort, String pSharedSecret,ArrayList<String> sharedFiles,String sharedFolderLocation) {
		this.peerId = pId;
		this.peerAddress = pAddress;
		this.peerPort = pPort;
		this.peerSharedSecret = pSharedSecret;
		this.sharedFiles= sharedFiles;
		this.sharedFolderLocation = sharedFolderLocation;
		
	}

	public int getPeerId() {
		return peerId;
	}

	public String getPeerAddress() {
		return peerAddress;
	}

	public int getPeerPort() {
		return peerPort;
	}
	public String getPeerSharedSecret() {
		return peerSharedSecret;
	}
	public ArrayList<String> getSharedFiles(){
		return sharedFiles;
	}
	
	public String getSharedLocation() {
		return sharedFolderLocation;
	}
	
	public void setSharedFiles() {
		this.sharedFiles = sharedFiles;
	}
	
	public void setPeerSharedSecret(String peerSharedSecret) {
		this.peerSharedSecret = peerSharedSecret;
	}

	public void setPeerAddress(String peerAddress) {
		this.peerAddress = peerAddress;
	}

	public void setPeerPort(int peerPort) {
		this.peerPort = peerPort;
	}
	
}

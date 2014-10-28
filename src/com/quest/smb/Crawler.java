package com.quest.smb;

import java.net.MalformedURLException;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.NtlmPasswordAuthentication;

public class Crawler {
   private SmbFile dir;
   private NtlmPasswordAuthentication fileAuth;
   public Crawler(Authenticator auth){
	 try {
	   jcifs.Config.setProperty("jcifs.smb.client.disablePlainTextPasswords","false");
       this.fileAuth = new NtlmPasswordAuthentication(auth.getDomain(),auth.getUsername(),auth.getPassword()); 
	   this.dir = new SmbFile(auth.getAddress(), fileAuth);
	   if(this.dir==null){
		   throw new RuntimeException();
	   }
	 } catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
   }
   
   public SmbFile getDirectory(){
	   return this.dir;
   }
   
   public SmbFile[] getFiles(){
	 try {
		return this.dir.listFiles();
	} catch (SmbException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return null;
	}
   }
}

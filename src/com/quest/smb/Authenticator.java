package com.quest.smb;

public class Authenticator {
	private String domain;
	private String username;
	private String pass;
	private String address;
   public Authenticator(String domain, String username, String pass,String address){
	 this.domain=domain;
	 this.username=username;
	 this.pass=pass;
	 this.address=address;
   }
   
   public void setPassword(String pass){
	 this.pass=pass;   
   }
   
   public void setUsername(String username){
	 this.username=username;
   }
   public void setDomain(String domain){
	 this.domain=domain;  
	}
   public void setAddress(String address){
		 this.address=address;  
    }
   
   public String getPassword(){
	   return this.pass;
   }
   
   public String getAddress(){
	   return this.address;
   }
   
   public String getUsername(){
	   return this.username;
   }
   
   public String getDomain(){
	   return this.domain;
   }
}

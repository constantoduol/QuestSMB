package com.quest.smb;

public interface Callback {
       
   public void doneAtBeginning();
   
   public Object doneInBackground();
   
   public void doneAtEnd(Object result);
   
}

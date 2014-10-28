package com.quest.smb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

public class FolderActivity extends Activity {
    private static ArrayList<HashMap<String,ArrayList>> fileHistory=new ArrayList<HashMap<String,ArrayList>>();
    private static ArrayList<String> fileAddress=new ArrayList();
    private boolean autocomplete=true;
    private static int currentLocation=0;
    private AutoCompleteTextView location;
	private SQLiteDatabase dbRead;
	private SQLiteDatabase dbWrite;
	private Database db;
	private AutoCompleteTextView username;
	private EditText domain;
	private EditText password;
	private static final String[] docExt=new String[]{"doc","docx","log","msg","odt","pages","rtf","tex","txt","wpd","wps"}; 
	private static final String[] audioExt=new String[]{"aif","iff","m3u","m4a","mid","mp3","mpa","ra","wav","wma"};
	private static final String[] videoExt=new String[]{"3g2","3gp","asf","asx","avi","flv","m4v","mov","mp4","mpg","rm","vob","wmv","mkv"};
	private static final String[] imageExt=new String[]{"bmp","dds","gif","jpg","png","psd","pspimage","tga","thm","tif","tiff","yuv"};
	private static final String[] sourceExt=new String[]{"c","cpp","cs","java","php","py","lua","h","sh","pl"};
	static {
	  Arrays.sort(docExt);
	  Arrays.sort(audioExt);
	  Arrays.sort(videoExt);
	  Arrays.sort(imageExt);
	  Arrays.sort(sourceExt);
	}

	private static String[][] allExt =new String[][]{docExt,audioExt,videoExt,imageExt,sourceExt};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.folder_activity);
	 
		TabHost tabs=(TabHost)findViewById(R.id.tabhost); 
		tabs.setup(); 
		TabHost.TabSpec spec=tabs.newTabSpec("tag1"); 
		spec.setContent(R.id.tab1); 
		spec.setIndicator("Open", getResources().getDrawable(R.drawable.folder));
		tabs.addTab(spec);
		
		spec=tabs.newTabSpec("tag2"); 
		spec.setContent(R.id.tab2); 
		spec.setIndicator("Search", getResources().getDrawable(R.drawable.search));
		tabs.addTab(spec);
		
	    spec=tabs.newTabSpec("tag3"); 
		spec.setContent(R.id.tab3); 
		spec.setIndicator("Settings", getResources().getDrawable(R.drawable.settings));
		tabs.addTab(spec);
		tabs.setCurrentTab(0);
		
		db=new Database(this);
		dbRead=db.getReadableDatabase();
		dbWrite=db.getWritableDatabase();
		ImageButton go=(ImageButton)findViewById(R.id.go_button);
		location=(AutoCompleteTextView)findViewById(R.id.location_bar);
		location.setTextColor(Color.BLACK);
		ImageButton recent=(ImageButton)findViewById(R.id.recent_button);
		username=(AutoCompleteTextView)findViewById(R.id.username);
		recent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                   username.showDropDown();
            }
    });
		password=(EditText)findViewById(R.id.password);
		domain=(EditText)findViewById(R.id.domain);
		final ImageButton back=(ImageButton)findViewById(R.id.back_button);
		final ImageButton forward=(ImageButton)findViewById(R.id.forward_button);
		location.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub

			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable edit) {
				// TODO Auto-generated method stub
			  setUpAutocomplete(edit.toString()); 
			}
		});
		
		back.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				goBack();
			}
		});
		forward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				goForward();
			}
		});
		go.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String address=location.getText().toString().toLowerCase();
				if(address.equals("")){
				  return;
				}
				String name=getUserName();
				String pass=password.getText().toString();
				String dom=domain.getText().toString();
			    String addr=parseAddress(address);
				location.setText(addr);
				if(name.equals("")){
					name=null;
				}
				if(pass.equals("")){
					pass=null;
				}
				if(dom.equals("")){
					dom=null;
				}
				
				 FolderRunner runner=new FolderRunner(addr, name, pass, dom, FolderActivity.this,address);
				 runner.execute();
			}
		});
		fetchRecentUsernames();
	  	 
	}
	
 public String getUserName(){
	return username.getText().toString();
 }
	
 public void enableAutoComplete(boolean state){
   autocomplete=state;
 }
 public void setUpAutocomplete(final String text){
	 Callback cb=new Callback() {
			@Override
			public Object doneInBackground() {
				Cursor cs=dbRead.rawQuery("select * from ADDRESS where AD_NAME like '"+text+"%' order by DATE_ENTERED desc limit 7", null);
				ArrayList<String> list=new ArrayList<String>();
                try{
					while(cs.moveToNext()){
						String address=cs.getString(1);
						list.add(address);
					}
				  cs.close();
				  return list;
              }
              catch(Exception e){
             	return null;
              }
              
				
			}
			
			@Override
			public void doneAtEnd(Object result) {
				 ArrayList<String> list=(ArrayList<String>)result;
			   if(autocomplete==false || result==null){
				  list=new ArrayList<String>();
				  return; 
			   }
			   System.out.println("auto-data: "+list);
			   //show this in  a drop down
			   ArrayAdapter<String> adapter=new ArrayAdapter<String>(FolderActivity.this, android.R.layout.simple_dropdown_item_1line, list);
			   location.setAdapter(adapter);
			}
			
			@Override
			public void doneAtBeginning() {
				// TODO Auto-generated method stub
				
			}
		};	
		
		AsyncHandler handler=new AsyncHandler(cb);
		handler.execute();
 
 }
 public String parseAddress(String address){
	   address=address.replace("\\", "/");
		Uri uri=Uri.parse(address);
		address=uri.toString();
	    if(address.startsWith("//")){
			address="smb:"+address;
		}
	    else if(address.startsWith("/")){
			address="smb:/"+address;					
		}
		else if(!address.startsWith("smb://")) {
		  address="smb://"+address;
		}
	    
	    if(!address.endsWith("/")){
	      address=address+"/";
	    }
	   return address;
 }
 
 public void rememberAddress(final String address){
	 if(db.ifValueExists(address, "AD_NAME", "ADDRESS")){
		  //update its time stamp
		  dbWrite.execSQL("UPDATE ADDRESS SET DATE_ENTERED="+System.currentTimeMillis()+" WHERE AD_NAME='"+address+"'");
		  return;
		}
	  Callback cb=new Callback() {
		@Override
		public Object doneInBackground() {
			// TODO Auto-generated method stub
			ContentValues values = new ContentValues();
			values.put("DATE_ENTERED", System.currentTimeMillis());
			values.put("AD_NAME", address);
			return dbWrite.insert("ADDRESS", null, values);
		}
		
		@Override
		public void doneAtEnd(Object result) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void doneAtBeginning() {
			// TODO Auto-generated method stub
			
		}
	};
	AsyncHandler handler=new AsyncHandler(cb);
	handler.execute();
	 
 }
 
 public void fetchRecentUsernames(){
	 Callback cb=new Callback() {
			@Override
			public Object doneInBackground() {
				// TODO Auto-generated method stub
				Cursor cs=dbRead.rawQuery("SELECT * FROM CREDENTIALS LIMIT 10", null);
				ArrayList<HashMap> cred=new ArrayList<HashMap>();
				ArrayList<String> list=new ArrayList<String>();
				ArrayList[] lists=new ArrayList[]{cred,list};
				while(cs.moveToNext()){
				  HashMap<String,String> map=new HashMap<String, String>();
				  String name=cs.getString(1);
				  map.put("username", name);
				  list.add(name);
				  map.put("password",cs.getString(2));
				  map.put("domain", cs.getString(3));
				  cred.add(map);
				}
				
			    return lists;
			}
			
			@Override
			public void doneAtEnd(Object result) {
				// TODO Auto-generated method stub
			  final ArrayList[] lists=(ArrayList[])result;
			  final ArrayList<HashMap<String,String>> cred=lists[0];
			  final ArrayList list=lists[1];
			  System.out.println("model from fetch addresses: "+list);
			  ArrayAdapter<String> adapter=new ArrayAdapter<String>(FolderActivity.this, android.R.layout.simple_dropdown_item_1line, list);
			  username.setAdapter(adapter);
			  if(cred.size()>0){
				  HashMap<String,String> latestCred= cred.get(0);
				  username.setText(latestCred.get("username"));
				  domain.setText(latestCred.get("domain"));
				  password.setText(latestCred.get("password"));
			  }
			  username.setOnItemClickListener(new OnItemClickListener() {

				@Override
			  public void onItemClick(AdapterView<?> arg0, View view,
						int pos, long arg3) {
					// TODO Auto-generated method stub
					  String txt= (String) list.get(pos);
					  for(int x=0; x<cred.size(); x++){
						 String name=cred.get(x).get("username");
						 if(name.equals(txt)){
							//we have found a match, display the domain and password
							domain.setText(cred.get(x).get("domain"));
							password.setText(cred.get(x).get("password"));
							return;
						 }
					  }
				  }
			
			  });
			  
			}
			
			@Override
			public void doneAtBeginning() {
				// TODO Auto-generated method stub
				
			}
		};
		AsyncHandler handler=new AsyncHandler(cb);
		handler.execute();  
 }
 
 public void rememberCredentials(final String username,final String password,final String domain){
	 if(db.ifValueExists(username, "USER_NAME", "CREDENTIALS")){
		  //update its time stamp
		  System.out.println("these username already exist");
		  Cursor cs=dbRead.rawQuery("SELECT * FROM CREDENTIALS WHERE USER_NAME='"+username+"'", null);
		  String passFromDb="";
		  String domainFromDb="";
		  String compPass=password;
		  String compDomain=domain;
		  while(cs.moveToNext()){
			 passFromDb=cs.getString(2);
			 domainFromDb=cs.getString(3);
		  }
		  System.out.println("domain remember: "+domain+": "+domainFromDb);
		  System.out.println("pass remember: "+password+": "+passFromDb);
		  if(passFromDb==null){
			  passFromDb="";
		  }
		  if(domainFromDb==null){
			  domainFromDb="";
		  }
		  if(compPass==null){
			compPass="";  
		  }
		  if(compDomain==null){
			 compDomain="";
		  }
		  if(passFromDb.equals(compPass) && domainFromDb.equals(compDomain)){
			// this value already exists so update its timestamp and return, otherwise save this new value
			  System.out.println("username value exists: "+username);
			  dbWrite.execSQL("UPDATE CREDENTIALS SET DATE_ENTERED="+System.currentTimeMillis()+" WHERE USER_NAME='"+username+"'");
			  return;
		  }
		}
	  Callback cb=new Callback() {
		@Override
		public Object doneInBackground() {
			// TODO Auto-generated method stub
			ContentValues values = new ContentValues();
			values.put("DATE_ENTERED", System.currentTimeMillis());
			values.put("USER_NAME", username);
			values.put("PASS_WORD", password);
			values.put("DOMAIN", domain);
			System.out.println("remember: "+username);
			return dbWrite.insert("CREDENTIALS", null, values);
		}
		
		@Override
		public void doneAtEnd(Object result) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void doneAtBeginning() {
			// TODO Auto-generated method stub
			
		}
	};
	AsyncHandler handler=new AsyncHandler(cb);
	handler.execute(); 
 }
 
	
 public SmbFile[] openFolder(String address,String username,String pass,String domain){
	   Authenticator auth=new Authenticator(domain, username, pass, address);
	   Crawler crawler=new Crawler(auth);
	   SmbFile[] files=crawler.getFiles();
	   return files;
	 
	}
	
 public void saveFileHistory(String address,ArrayList fileData){
	if(fileAddress.contains(address)){
	  return;
	}
	HashMap<String, ArrayList> map=new HashMap<String, ArrayList>();
	map.put(address, fileData);
	fileAddress.add(address);
	fileHistory.add(map);
	currentLocation=fileHistory.size()-1;
 }
 
 public void goBack(){
	if(currentLocation==0){
	  return;	
	}
	enableAutoComplete(false);
	currentLocation--;
	HashMap<String,ArrayList> map=fileHistory.get(currentLocation);
	String address=(String) map.keySet().toArray()[0];
	ArrayList files=map.get(address);
	location.setText(address);
	displayFiles(files);
	enableAutoComplete(true);
 }
 
 public void goForward(){
	if(currentLocation==fileHistory.size()-1){
		return;
	}
	else if(currentLocation==0 && fileHistory.size()==1){
	  return;
	}
    enableAutoComplete(false);
	currentLocation++;
	//load and show the data
	HashMap<String,ArrayList> map=fileHistory.get(currentLocation);
	String address=(String) map.keySet().toArray()[0];
	ArrayList files=map.get(address);
	location.setText(address);
	displayFiles(files);
	enableAutoComplete(true);
 }
 
 public void displayFiles(ArrayList<HashMap> files){
	 ArrayList<String> nameList=new ArrayList<String>();
	 ArrayList<String> typeList=new ArrayList<String>();
	 ArrayList<String> sizeList=new ArrayList<String>();
		for(int x=0; x<files.size(); x++){
			  HashMap file=files.get(x);
			  String fileName=(String)file.get("name");
			  nameList.add(fileName);
			  String ext=getExtension(fileName);
			  Long size=((Long)file.get("size")/1024);
			  String fileSize="";
			  fileSize=size.toString()+" KB";
			  if(size>=1048576){
				  Double siz=size.doubleValue()/1048576;
				  String round=siz.toString().substring(0,siz.toString().indexOf(".")+2);
				  fileSize=round+" GB";
				 
				  
			  }
			  else if(size>=1024){
				 size=size/1024;
				 fileSize=size.toString()+" MB";
			  }
		
			  if(ext.equals("") && size==0){
				  ext="folder";
				  fileSize="";
			  }
			  else if(ext.equals("") && size>0){
				 ext="file"; 
			  }
			  else {
				 ext=ext+" file"; 
			  }
			  typeList.add(ext);
			  sizeList.add(fileSize);
			  
	} 
			IconAdapter adapter=new IconAdapter(nameList,typeList,sizeList,files);
			ListView av = (ListView)findViewById(R.id.display_area_name);
			av.setAdapter(adapter);
 }
 
 public void displayDummyFiles(DummyFile[] files){
	 ArrayList<String> nameList=new ArrayList<String>();
	 ArrayList<String> typeList=new ArrayList<String>();
	 ArrayList<String> sizeList=new ArrayList<String>();
			for(DummyFile file:files){
			  nameList.add(file.name);
			  typeList.add(file.type);
			  sizeList.add(file.size);
			  
	} 
			System.out.println(nameList);
			
			DummyIconAdapter adapter=new DummyIconAdapter(nameList,files);
			ListView av = (ListView)findViewById(R.id.display_area_name);
			av.setAdapter(adapter);
			
 }
 
 
 private String getExtension(String fileName) {
	    String ext ="";
	    String s = fileName;
	    int i = s.lastIndexOf('.');
	    if (i > 0 &&  i < s.length() - 1) {
	        ext = s.substring(i+1).toLowerCase();
	    }
	    return ext;
	}
 
 private class DummyFile{
	 public String name;
	 public String type;
	 public String size;
	 public String path;
	 @SuppressWarnings("unused")
	public DummyFile(String name,String type,String size,String path){
		this.name=name;
		this.size=size;
		this.type=type;
		this.path=path;
	 }
 }
 
  
	
	private class FolderRunner extends AsyncTask<Void,Void,SmbFile[]>{ //param, progress,result
		private String address;
		private String username;
		private String pass;
		private String domain;
		private Context cxt;
		private ProgressDialog diag;
		private String userAddress;
		public FolderRunner(String address,String username,String pass,String domain,Context cxt,String userAddress){
		  this.address=address;
		  this.username=username;
		  this.pass=pass;
		  this.domain=domain;
		  this.cxt=cxt;
		  this.userAddress=userAddress;
		  
		}
		
		@Override
		protected SmbFile[] doInBackground(Void ... args) {
			// TODO Auto-generated method stub
			return openFolder(address,username,pass,domain);
			
		}
		
		protected void onPreExecute(){
		  // start a loading dialog
			diag=new ProgressDialog(cxt);
			String diagTitle=cxt.getResources().getString(R.string.folder_dialog_title);
			String diagMessage=cxt.getResources().getString(R.string.folder_dialog_message);
			diag.setTitle(diagTitle);
			diag.setMessage(diagMessage);
			diag.setIndeterminate(true);
			diag.setCancelable(false);
			diag.show();
		  
		     
		}
		
		
		protected void onPostExecute(SmbFile[] files){
			//display these files and folders
			
			if(files==null){
			  diag.cancel();
			  String notAvailable=cxt.getResources().getString(R.string.address_not_available);
			  Toast.makeText(cxt, address+" "+notAvailable, Toast.LENGTH_LONG).show();	
			  return;
			} 
			diag.cancel();
			rememberAddress(userAddress);
			rememberCredentials(username, pass, domain);
			/*
			DummyFile file1=new DummyFile("music","folder","0KB","smb://conny/");
			DummyFile file2=new DummyFile("shared","folder","0KB","smb://conny/");
			DummyFile file3=new DummyFile("movies","folder","0KB","smb://conny/");
			DummyFile file4=new DummyFile("data","folder","0KB","smb://conny/");
			DummyFile[] filez=new DummyFile[]{file1,file2,file3,file4};
			displayDummyFiles(filez);
			*/
			//store all the file data here length and name
			ArrayList fileData=new ArrayList();
			for(SmbFile file: files){
			  try {
				long size=file.length();
				String name=file.getName();
				String path=file.getPath();
				long date=file.getDate();
				long lastModif=file.getLastModified();
			    HashMap map=new HashMap();
			    map.put("size", size);
			    map.put("name", name);
			    map.put("path", path);
			    map.put("date", date);
			    map.put("last_modif", lastModif);
			    fileData.add(map);
			    
			} catch (SmbException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  
			}
			saveFileHistory(address, fileData);
		    displayFiles(fileData);
		}
		
		
	}
	
	private class IconAdapter extends ArrayAdapter {
		private ArrayList<String> model;
		private ArrayList<String> typeList;
		private ArrayList<String> sizeList;
		private ArrayList files;
	    public IconAdapter(ArrayList<String> model,ArrayList<String> typeList,ArrayList<String> sizeList,ArrayList files) {
			  super(FolderActivity.this,android.R.layout.simple_list_item_1,model);
			  this.model=model;
			  this.typeList=typeList;
			  this.sizeList=sizeList;
			  this.files=files;
			}
		public View getView(final int position, View convertView,ViewGroup parent) {
			View row=convertView;
			if (row==null) {                  
				LayoutInflater inflater=getLayoutInflater();
				row=inflater.inflate(R.drawable.row, null);
			}
			
			row.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View view) {
					 String name=getUserName();
					 String pass=password.getText().toString();
					 String dom=domain.getText().toString();
					 String path=(String)((HashMap)files.get(position)).get("path");
					 location.setText(path);
					 FolderRunner runner=new FolderRunner(path, name, pass, dom, FolderActivity.this,path);
					 runner.execute();	
					
				}
			});
			
			String name=(String)((HashMap)files.get(position)).get("name");
			String type=typeList.get(position);
			String size=sizeList.get(position);
			((TextView)row.findViewById(R.id.file_name)).setText(name);
			((TextView)row.findViewById(R.id.file_type)).setText(type+"  "+size);
			ImageView icon=(ImageView)row.findViewById(R.id.file_icon);
			if(type.equals("folder") || type.equals("file")){
				
			}
			else{
				type=type.substring(0,type.length()-5);	
			}
			if(type.equals("folder")){
				icon.setImageResource(R.drawable.folder);	
			}
			else if(type.equals("pdf")){
				icon.setImageResource(R.drawable.pdf_icon);
			}
			else{
			   for(int x=0; x<allExt.length; x++){
				  if(Arrays.binarySearch(allExt[x], type)>-1){
					 if(x==0){
						 icon.setImageResource(R.drawable.doc_icon); //private static String[][] allExt =new String[][]{docExt,audioExt,videoExt,imageExt,sourceExt};
					 }
					 else if(x==1){
						 icon.setImageResource(R.drawable.audio_icon);  
					 }
					 else if(x==2){
						 icon.setImageResource(R.drawable.video_icon);  
					 }
					 else if(x==3){
						 icon.setImageResource(R.drawable.pic_icon);  
					 }
					 else if(x==4){
						 icon.setImageResource(R.drawable.source_icon);  
					 }
					 break;
				  }
				  else if(x==allExt.length-1) {
						 icon.setImageResource(R.drawable.unknown_icon); 
						 break;
				  }
				  else{
					continue;  
				  }
			   }
			}
			
            return row;
		
	}
	
	
  

	}
	
	private class DummyIconAdapter extends ArrayAdapter {
		private ArrayList<String> model;
		private DummyFile[] files;
	    public DummyIconAdapter(ArrayList<String> model,DummyFile[] files) {
			  super(FolderActivity.this,android.R.layout.simple_list_item_1,model);
			  this.model=model;
			  this.files=files;
			}
		public View getView(final int position, View convertView,ViewGroup parent) {
			View row=convertView;
			if (row==null) {                  
				LayoutInflater inflater=getLayoutInflater();
				row=inflater.inflate(R.drawable.row, null);
			}
			
			row.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View view) {
					 String name=getUserName();
					 String pass=password.getText().toString();
					 String dom=domain.getText().toString();
					 String path=files[position].path;
					 location.setText(path);
					 FolderRunner runner=new FolderRunner(path, name, pass, dom, FolderActivity.this,path);
					 runner.execute();	
					
				}
			});
			
			String name=files[position].name;
			String type=files[position].type;
			String size=files[position].size;
			((TextView)row.findViewById(R.id.file_name)).setText(name);
			((TextView)row.findViewById(R.id.file_type)).setText(type+" "+size);
			ImageView icon=(ImageView)row.findViewById(R.id.file_icon);
			icon.setImageResource(R.drawable.folder);
            return row;
		
	}
	
	
  

	}
}

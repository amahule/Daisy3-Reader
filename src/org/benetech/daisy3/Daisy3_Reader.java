package org.benetech.daisy3;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class Daisy3_Reader extends Activity implements OnInitListener, OnUtteranceCompletedListener {

	//	static String DAISY3_BOOK_PATH = "/sdcard/daisy3/Edison__His_Life_and_Inventions.xml";
	//	static String DAISY3_BOOK_PATH = "/sdcard/daisy3/Life_and_Letters_of_Charles_.xml";
		static String DAISY3_BOOK_PATH = "/sdcard/daisy3/Alfred_Tennyson.xml";
	//	static String DAISY3_BOOK_PATH = "/sdcard/daisy3/The_Writings_of_Abraham_Linc.xml";

	static int BOOK_LOADING_COMPLETE = 0;
	TextView txt_View;
	private static final int CHECK_TTS_INSTALLED = 0;
	private static final String PARAGRAPHUTTERANCE="PARAGRAPHUTTERANCE";
	static final int ACTIVE = 1;
	static final int INACTIVE = 0;
	private int state = INACTIVE;

	private TextToSpeech mTts=null;
	HashMap<String,String> hashmap;
	private ProgressDialog pd_spinning;
	File daisy3_file;
	String text_for_speaking;
	static int LOADING_BOOK = 0;
	
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
		System.out.println("****** Inside onSaveInstanceState ******");
		if(mTts != null){
			mTts.stop();
		}
		savedInstanceState.putString("Book", text_for_speaking);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);
		System.out.println("****** Inside onRestoreInstanceState ******");
		text_for_speaking = savedInstanceState.getString("Book");
	}
	
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setFullScreen();
		setContentView(R.layout.main);

			System.out.println("********** Inside onCreate *********");
			txt_View = (TextView)findViewById(R.id.txt_View);
			txt_View.setMovementMethod(new ScrollingMovementMethod());
			daisy3_file = new File(DAISY3_BOOK_PATH);
	
			Intent checkIntent = new Intent();
			checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			
			/*
			 * After this method returns, onActivityResult will be called with CHECK_TTS_INSTALLED
			 * as the requestCode parameter.
			 */
			startActivityForResult(checkIntent, CHECK_TTS_INSTALLED);
	
			//Show the progress dialog. Will call onCreateDialog()
//			showDialog(LOADING_BOOK);

			if(savedInstanceState != null && savedInstanceState.containsKey("Book")){
				System.out.println("******** Length of book = "+savedInstanceState.getString("Book").length()+"********");
				txt_View.setText(savedInstanceState.getString("Book"));
				txt_View.setTextSize(18);
				txt_View.setTypeface(Typeface.DEFAULT);
			}
			else{
				
				pd_spinning = ProgressDialog.show(Daisy3_Reader.this, null, "Opening book. Please wait...", Boolean.TRUE);
				Thread thread_book_load;
				
				/**
				 *  Execute the book loading logic inside a non-UI thread, 
				 *  otherwise the progress dialog will not be shown
				 */
				thread_book_load = new Thread(){
					public void run(){
						showBook();
						
						Message msg = Message.obtain();
						msg.what = BOOK_LOADING_COMPLETE;
						msg.setTarget(handler);
						System.out.println("*********** Before Message.sentToTarget **********");
						msg.sendToTarget();
					}
				};
				
				thread_book_load.start();
			}
	}
	
	Handler handler = new Handler(){
		
		@Override
		public void handleMessage(Message msg){
			//Dismiss the progress dialog once the book loads
//			dismissDialog(LOADING_BOOK);

			pd_spinning.cancel();

			if(msg.what == BOOK_LOADING_COMPLETE){
				System.out.println("****** Inside handleMessage ******");
				txt_View.setText(text_for_speaking);
				txt_View.setTextSize(18);
				txt_View.setTypeface(Typeface.DEFAULT);
			}
		}
	};

	/**
	 * Parse the book and load it into the TextView of the layout
	 */
	public synchronized void showBook(){
		
		System.out.println("********* Inside showBook ******");
		//get the factory
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document doc;
		StringBuffer str_buf = new StringBuffer();

		try{
			str_buf.append("\n\nThis is a Daisy 3 book, brought to you by Bookshare.\n\n");
			//Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file.
			//Entire XML document is returned
			doc = db.parse(daisy3_file);

			//Get the root element of the xml document
			Element root = doc.getDocumentElement();

			System.out.println("******* Name of the root: "+root);
			NodeList doctitle = root.getElementsByTagName("doctitle");
			String str_doctitle = doctitle.item(0).getTextContent();
			str_buf.append("Book Title: "+str_doctitle+".\n\n");

			StringBuffer authors = new StringBuffer();
			NodeList docauthor_list = root.getElementsByTagName("docauthor");

			if(docauthor_list != null && docauthor_list.getLength() > 0){
				for(int i = 0; i < docauthor_list.getLength(); i++){
					authors.append(docauthor_list.item(i).getTextContent());
					if(i < docauthor_list.getLength() - 1){
						authors.append(", ");
					}
				}
			}

			authors.append(".");

			if(docauthor_list.getLength() > 1){
				str_buf.append("Authors: ");
			}else if(docauthor_list.getLength() == 1){
				str_buf.append("Author: ");
			}
			str_buf.append(authors+"\n\n\n");


			NodeList nodelist = root.getElementsByTagName("p");
			Node node;

			//Check if the nodelist is populated
			if(nodelist != null && nodelist.getLength() > 0){
				for(int i = 0; i < nodelist.getLength(); i++){
					node = nodelist.item(i);
					str_buf.append(node.getTextContent()+"\n");
				}
				text_for_speaking = str_buf.toString();
			}
		}
		catch(ParserConfigurationException e){
			e.printStackTrace();}
		catch(SAXException e){
			e.printStackTrace();}
		catch(IOException ioe){
			ioe.printStackTrace();}
	}

	protected void onActivityResult(
			int requestCode, int resultCode, Intent data) {
		if (requestCode == CHECK_TTS_INSTALLED) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				mTts = new TextToSpeech(this, this);
				hashmap= new HashMap<String, String>();
				hashmap.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, PARAGRAPHUTTERANCE);
			} else {
				// missing resource files, take the user to Android Market for installation
				Intent installIntent = new Intent();
				installIntent.setAction(
						TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
		}
	}
	int START = 0;
	int END = 200;
	int INCREMENT= END;

	private void speakString(String s){
		System.out.println(s.length()+"********");
		mTts.speak(s, TextToSpeech.QUEUE_FLUSH, hashmap);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(Menu.NONE, Menu.NONE, Menu.CATEGORY_CONTAINER,"Stop TTS");		
		menu.add(Menu.NONE, Menu.NONE, Menu.FIRST, "Talk");
		menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Exit");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuitem){		
		if(menuitem.getTitle().equals("Exit")){
			finish();
		}
		else if(menuitem.getTitle().equals("Talk")){
			mTts.stop();
			START = 0;
			END = INCREMENT;
			speakString(text_for_speaking.substring(0, INCREMENT));
		}
		else if(menuitem.getTitle().equals("Stop TTS")){
			stopTalking();
		}
		return true;
	}

	public void onInit(int status) {
		mTts.setOnUtteranceCompletedListener(this);
		setState(ACTIVE);
	}

	public void onUtteranceCompleted(String utteranceId) {
		System.out.println("***** Utterance Completed ****");
		START += END;
		END += INCREMENT;
		
		if(text_for_speaking.charAt(END)!='.'){
			while(text_for_speaking.charAt(END)!='.')
			{
				System.out.println("Char at index: "+END+" = "+text_for_speaking.charAt(END));
				END++;
			}
		}
		System.out.println("*********** START = "+START+". END = "+END+"*****************");
		System.out.println(text_for_speaking.substring(START, END));
		speakString(text_for_speaking.substring(START, END));

	}

	private void setState(int value){
		state = value;

		if(state==ACTIVE){
			//		pausebutton.post(new UpdateControls(UpdateControls.PAUSE));			 
		}else if(state==INACTIVE) {
			//		pausebutton.post(new UpdateControls(UpdateControls.PLAY));			 
		}
	}

	private PhoneStateListener mPhoneListener = new PhoneStateListener()
	{
		public void onCallStateChanged(int state, String incomingNumber)
		{
			if(state == TelephonyManager.CALL_STATE_RINGING) {
				stopTalking();
				finish();
			}
		}
	};

	private void stopTalking(){
		System.out.println("************ Inside stop talking **********");
		if(mTts!=null){
			System.out.println("************  Inside stop talking. mTts != null ************ ");
			mTts.stop();
		}
	}

	@Override
	public void  onBackPressed(){
		stopTalking();
		super.onBackPressed();
	}

	@Override
	protected void  onStart(){			
		super.onStart();
		System.out.println("****** onStart called ****");
	}

	@Override
	protected void  onResume(){			
		super.onResume();
		System.out.println("****** onResume called ****");
	}
	

	@Override
	protected void  onPause(){
		super.onPause();
		System.out.println("****** onPause called ****");
	}

	@Override
	protected void  onStop(){
		super.onStop();
		System.out.println("****** onStop called ****");
	}
	
	
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		if(pd_spinning != null){
			pd_spinning.cancel();
		}
		System.out.println("**** onDestroy called ******");
		if(mTts != null){
			mTts.shutdown();
			mTts = null;
		}	
	}

	public void setFullScreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
}


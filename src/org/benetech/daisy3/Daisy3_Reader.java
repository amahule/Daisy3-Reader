package org.benetech.daisy3;


import java.io.File;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Daisy3_Reader extends Activity implements OnInitListener, OnUtteranceCompletedListener {

	int BOOK_LOADING_COMPLETE = 0;
	TextView txt_View;
	private static final int CHECK_TTS_INSTALLED = 0;
	private static final String PARAGRAPHUTTERANCE="PARAGRAPHUTTERANCE";
	static final int ACTIVE = 1;
	static final int INACTIVE = 0;
	boolean day_flag = true;

	private TextToSpeech mTts=null;
	HashMap<String,String> hashmap;
	private ProgressDialog pd_spinning;
	File daisy3_file;
	String text_for_speaking;
	int LOADING_BOOK = 0;
	boolean isTalking = false;
	boolean isPaused = false;
	boolean isStopped = false;
	
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
	
	//Ignore the screen orientation changes 
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
	}
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setFullScreen();
		setContentView(R.layout.book_render);

			System.out.println("********** Inside onCreate *********");
			txt_View = (TextView)findViewById(R.id.txt_View);
			txt_View.setMovementMethod(new ScrollingMovementMethod());
			
			Bundle bundle = getIntent().getExtras();
			String filename = bundle.getString("file_to_be_opened");

			daisy3_file = new File(filename);
			
			Intent checkIntent = new Intent();
			checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
			
			/*
			 * After this method returns, onActivityResult will be called with CHECK_TTS_INSTALLED
			 * as the requestCode parameter.
			 */
			startActivityForResult(checkIntent, CHECK_TTS_INSTALLED);
	
			//Check if the the state was saved previously. (i.e. app was killed for lack of memory)
			if(savedInstanceState != null && savedInstanceState.containsKey("Book")){
				System.out.println("******** Length of book = "+savedInstanceState.getString("Book").length()+"********");
				txt_View.setText(savedInstanceState.getString("Book"));
				txt_View.setTextSize(18);
				txt_View.setTypeface(Typeface.DEFAULT);
			}
			//Fresh visit to the Activity
			else{
				
				pd_spinning = ProgressDialog.show(Daisy3_Reader.this, null, "Opening book. Please wait...", Boolean.TRUE);
				Thread thread_book_load;
				
				/**
				 *  Execute the book loading logic inside a non-UI thread, 
				 *  otherwise the progress dialog will not be shown
				 */
				thread_book_load = new Thread(){
					public void run(){
						text_for_speaking = new Daisy3_Parser().parseFile(daisy3_file);						
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
			
			//Dismiss the progress dialog
			pd_spinning.cancel();

			if(msg.what == BOOK_LOADING_COMPLETE){
				System.out.println("****** Inside handleMessage ******");
				txt_View.setText(text_for_speaking);
				txt_View.setTextSize(18);
				txt_View.setTypeface(Typeface.DEFAULT);
			}
		}
	};


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
	int END = 1000;
	int INCREMENT= END;

	private void speakString(String s){
		System.out.println(s.length()+"********");
		mTts.speak(s, TextToSpeech.QUEUE_FLUSH, hashmap);
	}
	
	public void onUtteranceCompleted(String utteranceId) {
		System.out.println("***** Utterance Completed ****");
		
		if(!isStopped && !isPaused){
			
			START += INCREMENT;
			END += INCREMENT;
	
			System.out.println("*********** START = "+START+". END = "+END+"*****************");
			System.out.println(text_for_speaking.substring(START, END));
			
			speakString(text_for_speaking.substring(START, END));
		}

	}

	//Stop talking if there is an incoming call	
	private PhoneStateListener mPhoneListener = new PhoneStateListener()
	{
		public void onCallStateChanged(int state, String incomingNumber)
		{
			if(state == TelephonyManager.CALL_STATE_RINGING) {
				stopTalking("Incoming Call");
				finish();
			}
		}
	};

	//Stop talking
	private void stopTalking(String status){
		System.out.println("************ Inside stop talking **********");
		if(mTts!=null){
			System.out.println("************  Inside stop talking. mTts != null ************ ");
			mTts.stop();
		}
		
		Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Called for the first time the options menu is shown.
	 * @param menu - Menu which will hold the items and submenus
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu){

		MenuInflater menuinflater = getMenuInflater();
		
		//Inflate the xml contents to the menu
		menuinflater.inflate(R.menu.menu_options, menu);
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		if(isStopped){
			MenuItem speak = menu.findItem(R.id.speak);
			speak.setTitle("Speak");
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem menuitem){
		switch(menuitem.getItemId()){
			case R.id.speak :

				//When speak is pressed for first time or after stop has been pressed
				if(menuitem.getTitle().equals("Speak") && !isTalking && !isPaused){
					START = 0;
					END = INCREMENT;
					speakString(text_for_speaking.substring(0, INCREMENT));
					Toast.makeText(this, "Text To Speech started", Toast.LENGTH_SHORT).show();
					isTalking = true;
					isStopped = false;
					menuitem.setTitle("Pause");
				}
				
				//Speak pressed after pause was pressed
				else if(menuitem.getTitle().equals("Speak") && isPaused){
					System.out.println("***** Inside speak after paused*******");
					System.out.println("START = "+START+", END = "+END);
					speakString(text_for_speaking.substring(START, END));
					menuitem.setTitle("Pause");
					
					isPaused = false;
				}
				
				//When pause is pressed (pause will be shown in this case)
				else if(menuitem.getTitle().equals("Pause") ){
					isPaused = true;
					isTalking = false;
					stopTalking("Text to speech paused");
					menuitem.setTitle("Speak");
				}
				
				break;

			case R.id.Exit :
				if(!isStopped)
				{
					isStopped = true;
					stopTalking("Text to speech stopped");
					isTalking = false;
					isPaused = false;
				}
				finish();
				break;

			case R.id.stop :
				if(!isStopped)
				{
					isStopped = true;
					stopTalking("Text to speech stopped");
					isTalking = false;
					isPaused = false;
				}
				break;
				
			case R.id.day_night :
				toggle_Day_Night();
		}
			return true;
	}

	/**
	 * Toggle the status day/night vision on screen
	 */
	public void toggle_Day_Night(){
		RelativeLayout relativelayout = (RelativeLayout)findViewById(R.id.parent_layout);

		//Set night mode - background = black and text = white
		if(day_flag){
			relativelayout.setBackgroundColor(Color.BLACK);
			txt_View.setTextColor(Color.WHITE);
			day_flag = false;
		}
		else{
			relativelayout.setBackgroundColor(Color.WHITE);
			txt_View.setTextColor(Color.BLACK);
			day_flag = true;
		}
	}

	public void onInit(int status) {
		mTts.setOnUtteranceCompletedListener(this);
	}

	@Override
	public void  onBackPressed(){
		stopTalking("Back button pressed");
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

	/**
	 * Shutdown the Text To Speech when the Activity is being destroyed
	 */
	@Override
	public void onDestroy(){
		super.onDestroy();
		if(pd_spinning != null){
			pd_spinning.cancel();
		}
		System.out.println("**** onDestroy called ******");
		if(mTts != null){
			mTts.stop();
			mTts.shutdown();
			mTts = null;
		}	
	}

	//Set the full screen mode
	public void setFullScreen() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}
}


package org.benetech.daisy3;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;

/**
 * This class represents the Entry screen to the Daisy Book Reader application
 *
 */
public class StartUp extends Activity{

	Button btn_Exit;
	Button btn_OpenBook;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setFullScreen();
		setContentView(R.layout.startup);

		btn_OpenBook = (Button)findViewById(R.id.btn_openbook);
		btn_OpenBook.setOnClickListener(new View.OnClickListener(){
			
			public void onClick(View v){
				Intent list_books = new Intent();
				list_books.setClassName("org.benetech.daisy3","org.benetech.daisy3.List_Books");
				startActivity(list_books);
			}
		});
		
		btn_Exit = (Button)findViewById(R.id.btn_Exit);
		btn_Exit.setOnClickListener(new View.OnClickListener(){

			public void onClick(View v){
				finish();
			}
		});
	}
	
	//Set full screen mode
	public void setFullScreen(){
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);
	}
}

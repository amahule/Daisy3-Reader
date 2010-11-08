package org.benetech.daisy3;

import java.io.File;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class List_Books extends ListActivity{

	String daisy3_books;
	String file_to_be_opened;
	boolean flag_valid_file_found = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setFullScreen();
		
		File external_storage_dir = Environment.getExternalStorageDirectory();
		
		//Get absolute path for the daisy3 books storage
		daisy3_books = external_storage_dir.getAbsolutePath() + "/daisy3";
		
		File daisy3_books_path = new File(daisy3_books);

		String[] Books = daisy3_books_path.list();
		
		if(!daisy3_books_path.exists()){
			call_alert_dialog("No daisy3 directory found",
					"Make sure that there is a directory named daisy3 on your external storage (/sdcard/ or /mnt/sdcard/) ");
		}
		
		else if(Books == null || Books.length == 0){
			call_alert_dialog("No books found",
					"No books were found inside the daisy3 directory on sdcard");
		}
		
		else{
			
			setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, Books));
	
			ListView lv = getListView();
			lv.setTextFilterEnabled(true);
	
			lv.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					
					CharSequence dir_name = ((TextView) view).getText();
					
					File file = new File(daisy3_books + "/" + dir_name);
					

	
					if(file.isDirectory()){
						String [] files_in_daisy3book = file.list();
						if(files_in_daisy3book != null && files_in_daisy3book.length > 0){
							for(String str_file : files_in_daisy3book){
								System.out.println("****** extensions: "+str_file.substring(str_file.length() - 4));
								if(str_file.substring(str_file.length() - 4).equals(".xml")){
									file_to_be_opened = file.getAbsolutePath() + "/" + str_file;
									System.out.println("*****  file_to_be_opened = "+file_to_be_opened);
									
									File f = new File(file_to_be_opened);
									if(f.exists()){
										System.out.println("File exists and can be opened*********");
										flag_valid_file_found = true;
										open_book(file_to_be_opened);
									}
									break;
								}
							}
						}
					}
					
					// When clicked, show a toast with selected book name
					Toast.makeText(getApplicationContext(), flag_valid_file_found ? ((TextView) view).getText() : "Valid file not found",
							Toast.LENGTH_SHORT).show();
					flag_valid_file_found = false;
				}
			});
		}
	}
	
	public void call_alert_dialog(String title, String message){
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		
		builder
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton(R.string.dialog_close_button, null)
			.show();
	}
	
	public void open_book(String file_to_be_opened){
		System.out.println("Inside open_book: "+file_to_be_opened);
		Intent intent = new Intent();
		intent.setClassName("org.benetech.daisy3", "org.benetech.daisy3.Daisy3_Reader");
		Bundle bundle = new Bundle();
		bundle.putString("file_to_be_opened", file_to_be_opened);
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	public void setFullScreen(){
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);
	}
}

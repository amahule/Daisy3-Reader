package org.benetech.daisy3;

import java.io.File;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * This class is a ListActivity and is used to List the Daisy3 books 
 * 
 */
public class List_Books extends ListActivity{

	String daisy3_books;
	String file_to_be_opened;
	boolean flag_valid_file_found = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setFullScreen();
		
		File external_storage_dir = Environment.getExternalStorageDirectory();
		
		// Get absolute path for the daisy3 books storage
		daisy3_books = external_storage_dir.getAbsolutePath() + "/daisy3";
		
		File daisy3_books_path = new File(daisy3_books);

		// List of Daisy 3 books present at the specified location
		String[] Books_All = daisy3_books_path.list();
		Vector<String> Books_vector = new Vector<String>();

		
		// The daisy3 directory does not exist
		if(!daisy3_books_path.exists()){
			call_alert_dialog("No daisy3 directory found",
					"Make sure that there is a directory named daisy3 on your external storage (/sdcard/ or /mnt/sdcard/) ");
		}
		
		// No books found in the daisy3 directory
		else if(Books_All == null || Books_All.length == 0){
			call_alert_dialog("No books found",
					"No books were found inside the daisy3 directory on sdcard");
		}
		
		// Books found inside the daisy3 directory
		else{

			/* Remove the directory entries that OS X sometimes creates while copying the files to SD card
			 * These directory names start with "._"
			 */ 
			for(String entry: Books_All){
				if(!(entry.substring(0, 2).equals("._"))){
					Books_vector.add(entry);
				}
			}
			
			String Books[] = new String[Books_vector.size()];
			
			Books = (String[])Books_vector.toArray(new String[0]);

			// Populate the view with the String array
			setListAdapter(new ArrayAdapter<String>(this, R.layout.list_item, Books));
	
			ListView lv = getListView();
			lv.setTextFilterEnabled(true);
	
			lv.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					
					CharSequence dir_name = ((TextView) view).getText();
					File file = new File(daisy3_books + "/" + dir_name);
					
					// Check is the selected name is a directory
					if(file.isDirectory()){
						
						// List all the files inside a daisy3 book.
						String [] files_in_daisy3book = file.list();
						
						System.out.println("******** "+files_in_daisy3book.length+" **********");
						if(files_in_daisy3book != null && files_in_daisy3book.length > 0){
							
							// Look for a file with .xml extension among all files inside a daisy3 book directory
							for(String str_file : files_in_daisy3book){
								System.out.println("*****************"+str_file+"***********");

								// XML file found. Make sure it is not a OS X dirty file
								if((str_file.substring(str_file.length() - 4).equals(".xml")) && !str_file.substring(0,2).equals("._")){

									// Absolute path name for the XML file
									file_to_be_opened = file.getAbsolutePath() + "/" + str_file;
									
									File f = new File(file_to_be_opened);
									
									// Another check for existence of the file with absolute path name 
									if(f.exists()){
										flag_valid_file_found = true;
										open_book(file_to_be_opened);
									}
									break;
								}
							}
						}
					}
					
					// Show a toast with selected book name or error message 
					Toast.makeText(getApplicationContext(), flag_valid_file_found ? ((TextView) view).getText() : "Invalid Daisy 3 file",
							Toast.LENGTH_LONG).show();
					flag_valid_file_found = false;
				}
			});
		}
	}

	// Create a menu with Exit button
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		menu.add(Menu.NONE, Menu.NONE, Menu.NONE, "Exit");
		return true;
	}
	
	// Finish the current ListActivity when the Exit button is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem menuitem){
		if(menuitem.getTitle().equals("Exit")){
			finish();
		}
		return true;
	}
	
	// Build and show an AletDialog
	public void call_alert_dialog(String title, String message){
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		
		builder
			.setTitle(title)
			.setMessage(message)
			.setPositiveButton(R.string.dialog_close_button, new close_button_listener())
			.show();
	}
	
	
	// Class for DialogInterface.OnClickListener Listener
	private class close_button_listener implements DialogInterface.OnClickListener{
		public void onClick(DialogInterface d,int which){
			if(which == DialogInterface.BUTTON_POSITIVE)
				finish();
		}
	}
	
	/**
	 * Send the filename in a Bundle to the next Daisy3_Reader Activity
	 * @param file_to_be_opened The filename of the XML file to be parsed
	 */
	public void open_book(String file_to_be_opened){
		System.out.println("Inside open_book: "+file_to_be_opened);
		Intent intent = new Intent();
		intent.setClassName("org.benetech.daisy3", "org.benetech.daisy3.Daisy3_Reader");
		Bundle bundle = new Bundle();
		bundle.putString("file_to_be_opened", file_to_be_opened);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	//Set the full screen mode
	public void setFullScreen(){
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);
	}
}

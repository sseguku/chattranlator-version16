package com.lecode.chatranslator;

/*
 * Developed by Jingo Kisakye
 * Rights reserved by lecode
 * Dedicated to my Maama - Marion Sebunya Namuddu.
 * She has been great in my live
 * I love you Mom
 * */

import java.util.ArrayList;
import java.util.List;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;

import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;
import com.memetix.mst.detect.*;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint({ "DefaultLocale", "NewApi" })
public class MainActivity extends Activity {
	
	final DatabaseHandler db = new DatabaseHandler(this);

    com.lecode.chatranslator.ChatArrayAdapter adapter;
    final Context context = this;
    
    /*The lists in chat translator*/
    ListView lv;
    ListView speechInText;
    
    List <ChatTags> cTags;
	
	Dialog matchTextDialog;
	ArrayList<String> matchesText;
	

	EditText edittextFromLang;
	String TranslatedText, langToBeTranslated, langSelected, originalText= null ;
    Spinner froSpinnerLang, toSpinnerLang;	
    private static final int REQUEST_CODE = 1001;
    ImageView mic_ImageView, send_now_ImageView;
    
    //db operations
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint({ "NewApi", "ClickableViewAccessibility" })
	@Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
       
        
        cTags = db.getAllChatTags();
        
        //declarations 
        //microphone
        mic_ImageView =(ImageView)findViewById(R.id.mic);
        send_now_ImageView =(ImageView)findViewById(R.id.send_now);
     
        
        //spinners
        addItemsOnFroSpinner();
        addItemsOnToSpinner();
        //edit texts
        edittextFromLang = (EditText) findViewById(R.id.edittext_fro_lang);	
        //adapters
        adapter = new ChatArrayAdapter(getApplicationContext(), R.layout.listitem);
	
        //lists
        lv = (ListView) findViewById(R.id.listView1);			
		lv.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
		lv.setStackFromBottom(true);		
		//lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		//lv.setLongClickable(true);
	
        lv.setAdapter(adapter);
        lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        //when too mus
    
    
        lv.setMultiChoiceModeListener(new MultiChoiceModeListener() {

            LinearLayout  actionLayout = (LinearLayout)findViewById(R.id.action_bar_layout);
        	private int nr = 0;
        	
			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				return false;
			}
			
			@Override
			public void onDestroyActionMode(ActionMode mode) {
				
				actionLayout.setVisibility(View.VISIBLE);
				adapter.clearSelection();
			}
			
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				nr = 0;
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.contextual_menu, menu);
                actionLayout.setVisibility(View.INVISIBLE);
                //actionLayout.setSystemUiVisibilit
                return true;
			}
			
			/*
			 Method for contextual action bar wen it shows up
			 */
			
			@TargetApi(Build.VERSION_CODES.HONEYCOMB)
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			
				StringBuilder sb = new StringBuilder();			
				
				 switch (item.getItemId()) {
				 
				 case R.id.item_copy:{
                
                	SparseBooleanArray checked = lv.getCheckedItemPositions();
     				//ArrayList<String> seletedItems = new ArrayList<String>();
     				for(int i=0;i<checked.size();i++){
     					int position = checked.keyAt(i);
     					if(checked.valueAt(i))
     						sb.append(" "+adapter.getItem(position).comment+" ");
     				}
     				
                	 ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                 	
                	 ClipData clip = ClipData.newPlainText("", sb.toString());
                	   clipboard.setPrimaryClip(clip);
                	// clipboard.setText(clip)
                     nr = 0;
                     adapter.clearSelection();
                     mode.finish();
                     actionLayout.setVisibility(View.VISIBLE);
                   break;    
                 }
                
                 case R.id.item_share:{
                	
                	 SparseBooleanArray checked = lv.getCheckedItemPositions();
      				
                	 //ArrayList<String> seletedItems = new ArrayList<String>();
      				for(int i=0;i<checked.size();i++){
      					int position = checked.keyAt(i);
      					if(checked.valueAt(i))
      						sb.append(" "+adapter.getItem(position).comment+" ");
      				}
      				
      				//sending a single text.. 
      				if(checked.size() <= 1){
                	 Intent sendIntent = new Intent();
                	 sendIntent.setAction(Intent.ACTION_SEND);
                	 sendIntent.putExtra(Intent.EXTRA_TEXT,  sb.toString()+"\n ~by chattranslator");
                	 sendIntent.setType("text/plain");
                	 startActivity(sendIntent);
      				}
      				
      				 //for sending multiple translations
      				else{
      					//ArrayList<String> mTranslations = new ArrayList<String>();
      					for(int i=0;i<checked.size();i++){
          					int position = checked.keyAt(i);
          					if(checked.valueAt(i))
          						sb.append("\n"+adapter.getItem(position).comment+" ");
          				}
      					
      					 Intent sendIntent = new Intent();
                    	 sendIntent.setAction(Intent.ACTION_SEND);
                    	 sendIntent.putExtra(Intent.ACTION_SEND, sb.toString()+"\n ~by chattranslator");

                    	 sendIntent.setType("text/plain");
                    	 startActivity(sendIntent);

      				
      				}
                	 
                	//sending multiple texts.
                	 
                //clear selection
                	 nr = 0;
                     adapter.clearSelection();
                     mode.finish();
                	 break;
                
                	 
                 }
                 
                 
                 case R.id.item_delete:{
                	 // retrieve selected items and delete them out
                	
                	 SparseBooleanArray checked = lv.getCheckedItemPositions();
      				//ArrayList<String> seletedItems = new ArrayList<String>();
      				for(int i=0;i<checked.size();i++){
         				
      					if(checked.valueAt(i)){
      					db.deleteChatTags(cTags.get(i));
      					adapter.removeItem(i);
                        
      					adapter.notifyDataSetChanged();
      					}
      					nr = 0;
                        adapter.clearSelection();
    	               
      				}
                	 
                	 mode.finish(); // Action picked, so close the CAB
                     return true;
                	 
                 }
               
             }
				return false;
			}
			
			@SuppressLint("NewApi")
			@Override
			public void onItemCheckedStateChanged(ActionMode mode, int position,
					long id, boolean checked) {
				
                if (checked) {
                       nr++;
                       adapter.setNewSelection(position, checked);                   
                   } else {
                       nr--;
                       adapter.removeSelection(position);                
                   }
                   mode.setTitle(nr + " selected");
			}
		});
        
        
        lv.setOnItemLongClickListener(new OnItemLongClickListener() {
  
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                    int position, long arg3) {
                
                lv.setItemChecked(position, !adapter.isPositionChecked(position));
                
                return false;
            }
        });
        
        //previous translations and conversation
        for(ChatTags ct: cTags){
        	adapter.add(new OneComment(true, ct.get_left_user().toString()));
        	adapter.add(new OneComment(false, ct.get_right_machine().toString()));
    		
        }
       
        //lower layout Listeners
        //mic 
        mic_ImageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				if(isConnected())
					checkVoiceRecognition();
				else
				//not connected to the internet
                  noInternetConnection();
			}
		});
        
        send_now_ImageView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                	if(isConnected()){	
          		   		 if (edittextFromLang!=null &&edittextFromLang.length()>0) 
          		   		 { 
          		   			 addTranslation();
          		   			 
          		   		 }}
          		   	
          		   		else{
          		   		noInternetConnection();
          		   		}
                    break;
                }
                case MotionEvent.ACTION_UP:{
                      edittextFromLang.setText("");
                    break;
                }
                case MotionEvent.ACTION_CANCEL: {
                    ImageView view = (ImageView) v;
                    //clear the overlay
                    view.getDrawable().clearColorFilter();
                    view.invalidate();
                    break;
                }
            }
				return true;
			}
		});

        
        
    	/*
    	 * Sends info on Enter EditText
    	 */
        edittextFromLang.setOnKeyListener(new OnKeyListener() {
		
			public boolean onKey(View v, int keyCode, KeyEvent event) {
		// If the event is a key-down event on the "enter" button
			if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
		// Perform action on key press
		//check for internet connection
		   		if(isConnected()){	
		   		 if (edittextFromLang!=null &&edittextFromLang.length()>0) 
		   		 { 
		   			 addTranslation();
		   		 }}
		   	
		   		else{
		   		noInternetConnection();
		   		}
		   		    
		   		 return true;
							}
				return false;
					}
				});
	       }//end of Enter EditText
    

	/*
	 * Method translation process
	 */
    public void addTranslation(){   	
    	//add to left chat
    	adapter.add(new OneComment(true, edittextFromLang.getText().toString()));
		
    	class bgStuff extends AsyncTask<Void, Void, Void>{                   
            
            @Override
            protected Void doInBackground(Void... params) {
            	
            	Translate.setClientId("etranslator123");
            	Translate.setClientSecret("FP556NJH3bcPYNMff2OEIWhxKMqd4TZO5PyDsC5fLBM=");
            	originalText=edittextFromLang.getText().toString();
                langToBeTranslated=froSpinnerLang.getSelectedItem().toString().toUpperCase();
                langSelected=toSpinnerLang.getSelectedItem().toString();
            
            do{	
               try {
            	   Language detectedLanguage = Detect.execute(originalText);//detect lang from 
                	 
            	   if(langToBeTranslated=="AUTO_DETECT"){
            		   TranslatedText = Translate.execute(originalText,  Language.valueOf(langSelected));
                       db.addChatTags(new ChatTags(originalText, TranslatedText));
            	   }
            	   else{
                  	 
                	   //checks if the lang z equal to detected lang
              	    if(froSpinnerLang.getSelectedItem().toString().toUpperCase().equalsIgnoreCase(detectedLanguage.getName(Language.ENGLISH))){
                    TranslatedText = Translate.execute(originalText, Language.valueOf(langToBeTranslated), Language.valueOf(langSelected));
                    db.addChatTags(new ChatTags(originalText, TranslatedText));  
              	    }  
              	   else{
              		   TranslatedText ="That seems to be " + detectedLanguage.getName(Language.ENGLISH).toUpperCase()+ ". Type only words in "+ langToBeTranslated.toUpperCase()+ " or Change to Auto Detect";
              		   
              	   }
              	   
                 }
        		
            	   
            	  // TranslatedText = Translate.execute(originalText, Language.GERMAN);
					
				} catch (Exception e) {
					TranslatedText = originalText;
					
				    //e.printStackTrace();
				}//end try catch block
               
            }while(TranslatedText=="########");
				return null;
                
            }//end doinbackground method

            @Override
            protected void onPostExecute(Void result) {
            
            	adapter.add(new OneComment(false, TranslatedText));
            	edittextFromLang.setText("");
            }
    	}
    	//end of async task class
    	 new bgStuff().execute();                          
         

    }
    
      /*
       *  Inside the menu.
       *  Method Adding items to spinner the first spinner */
    
    public void  addItemsOnFroSpinner(){
    	
    	//Strings to be added in the spinner
    	List <String> frolist = new ArrayList<String>();
    	froSpinnerLang =(Spinner)findViewById(R.id.fro_spinner);
    	
    	for(Language lang: Language.values()){
    		frolist.add(lang.name());
    	}
    	
        // Create an ArrayAdapter using the string array and a default spinner layout
    	ArrayAdapter<String> sp1adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, frolist);
    	// Specify the layout to use when the list of choices appears
    	sp1adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
    	// Apply the adapter to the spinner
    	froSpinnerLang.setAdapter(sp1adapter);
    }

    /* 
     * Inside the menu.
     * Method Adding items to spinner the Second spinner */
    
    public void  addItemsOnToSpinner(){
    	toSpinnerLang = (Spinner)findViewById(R.id.to_spinner);
        List <String> frolist = new ArrayList<String>();
    	
    	for(Language lang: Language.values()){
    		frolist.add(lang.name());
    	}
    	
    	frolist.remove(0);
       // Create an ArrayAdapter using the string array and a default spinner layout
    	ArrayAdapter<String> sp2adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, frolist);
    	// Specify the layout to use when the list of choices appears
    	sp2adapter.setDropDownViewResource(R.layout.spinner_dropdown_item2);
    	// Apply the adapter to the spinner
    	toSpinnerLang.setAdapter(sp2adapter); 
    	toSpinnerLang.setSelection(12);
    	
    }
    
    /*
     * Method checks for voice recognition check presence*/
    public void checkVoiceRecognition(){
    	
    	PackageManager pm = getPackageManager();
    	List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
         
    	if(activities.size()==0){
     	  //dialog for voice recognizer
    		noVoiceRecognizerDialog();
    	
    	}
    	else{
    		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    		startActivityForResult(intent, REQUEST_CODE);
    		
    	}
    	
    }
    //end of voice recognition
    
    /*
     * Method checks Internet boolean    
     */
    public boolean isConnected(){
    	ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo net = cm.getActiveNetworkInfo();
    	if(net!=null && net.isAvailable() && net.isConnected()){
    		return true;
    	}
    	else{
    	return false;}
    	
    }

	@Override
/*
 * Method is for voice recognizing(non-Javadoc)
 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
 */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     	
		if(requestCode ==REQUEST_CODE && resultCode==RESULT_OK){
			if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			     matchTextDialog = new Dialog(MainActivity.this);
			     matchTextDialog.setContentView(R.layout.dialog_matches_frag);
			     matchTextDialog.setTitle("Select Matching Text");
			     speechInText = (ListView)matchTextDialog.findViewById(R.id.speakin_list);
			     matchesText = data
					     .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			     ArrayAdapter<String> adapterVoice =    new ArrayAdapter<String>(this,
			    	     android.R.layout.simple_list_item_1, matchesText);
			     speechInText.setAdapter(adapterVoice);
			     speechInText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			     @Override
			     public void onItemClick(AdapterView<?> parent, View view,
			                             int position, long id) {
			    	 
			    	 originalText=matchesText.get(position).toString();
					adapter.add(new OneComment(true,originalText));
						
						//translation process
			    	class bgStuff extends AsyncTask<Void, Void, Void>{                   
			            
			            @Override
			            protected Void doInBackground(Void... params) {
			            	
			            	Translate.setClientId("etranslator123");
			            	Translate.setClientSecret("FP556NJH3bcPYNMff2OEIWhxKMqd4TZO5PyDsC5fLBM=");
			                langToBeTranslated=froSpinnerLang.getSelectedItem().toString().toUpperCase();
			                langSelected=toSpinnerLang.getSelectedItem().toString();
			            
			            do{	
			               try {
			            	   Language detectedLanguage = Detect.execute(originalText);//detect lang from 
			                	 
			            	   if(langToBeTranslated=="AUTO_DETECT"){
			            		   TranslatedText = Translate.execute(originalText,  Language.valueOf(langSelected));
			                       db.addChatTags(new ChatTags(originalText, TranslatedText));
			            	   }
			            	   else{
			                  	 
			                	   //checks if the lang z equal to detected lang
			              	    if(froSpinnerLang.getSelectedItem().toString().toUpperCase().equalsIgnoreCase(detectedLanguage.getName(Language.ENGLISH))){
			                    TranslatedText = Translate.execute(originalText, Language.valueOf(langToBeTranslated), Language.valueOf(langSelected));
			                    db.addChatTags(new ChatTags(originalText, TranslatedText));  
			              	    }  
			              	   else{
			              		   TranslatedText ="That seems to be " + detectedLanguage.getName(Language.ENGLISH).toUpperCase()+ ". Type only words in "+ langToBeTranslated.toUpperCase()+ " or Change to Auto Detect";
			              		   
			              	   }
			              	   
			                 }
			        		
			            	   
			            	  // TranslatedText = Translate.execute(originalText, Language.GERMAN);
								
							} catch (Exception e) {
								TranslatedText = originalText;
								
							   // e.printStackTrace();
							}//end try catch block
			               
			            }while(TranslatedText=="########");
							return null;
			                
			            }//end doinbackground method

			            @Override
			            protected void onPostExecute(Void result) {
			            	adapter.add(new OneComment(false, TranslatedText));
			        		 }
			    	}
			    	//end of asynctask class
			    	 new bgStuff().execute();                          
			         

						

						
						//end of translation process
						
			    	 matchTextDialog.hide();
			     }
			 });
			     matchTextDialog.show();
			     }
	
		
		}
		super.onActivityResult(requestCode, resultCode, data);
	}//Ends method voice recognition

/*
 * Method displays dialog for no recognizer
 */
	public void noVoiceRecognizerDialog(){
		
		final Dialog dialogVoice = new Dialog(context);
		dialogVoice.setContentView(R.layout.no_voice_recognizer);
		dialogVoice.setTitle("Warning");
		
		Button dialogButton = (Button) dialogVoice.findViewById(R.id.no_voice_button);
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialogVoice.dismiss();
			}
		});

		dialogVoice.show();

	}//ends method dialog for No recognizer

/*
 * Method to display dialog for no connection
 */
	public void noInternetConnection(){
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.no_connection);
		dialog.setTitle("Warning");
		
		Button dialogButton = (Button) dialog.findViewById(R.id.no_connection_button);
		dialogButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				}
			});

			dialog.show();
		
		}//ends Method display dialog for No connecton

	}//end of the Main Activity
    


package com.example.fragmenttabs;


import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

public class Config extends Activity {

	private CheckBox chkDelWarn;
	private CheckBox chkDelPageWarn;
	private Button btnBack;

	SharedPreferences setting;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
		setting = getSharedPreferences("delete_warn", 0);
		
		chkDelWarn = (CheckBox) findViewById(R.id.chkDeleteWarn);
		chkDelPageWarn = (CheckBox) findViewById(R.id.chkDeletePageWarn);
		
//		//force
//		setting.edit().putString("KEY_DELETE_WARN", "yes").commit();
//		
		//initialization: item
		if(setting.getString("KEY_DELETE_WARN","").equalsIgnoreCase("yes")){
			chkDelWarn.setChecked(true);
			setting.edit().putString("KEY_DELETE_WARN", "yes").commit();
		}else{
			chkDelWarn.setChecked(false);
			setting.edit().putString("KEY_DELETE_WARN", "no").commit();
		}
		
		//initialization: page
		if(setting.getString("KEY_DELETE_PAGE_WARN","").equalsIgnoreCase("yes")){
			chkDelPageWarn.setChecked(true);
			setting.edit().putString("KEY_DELETE_PAGE_WARN", "yes").commit();
		}else{
			chkDelPageWarn.setChecked(false);
			setting.edit().putString("KEY_DELETE_PAGE_WARN", "no").commit();
		}
		
		
//		addListenerOnButton();
		
		chkDelWarn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() 
		{     @Override
			   public void onCheckedChanged(CompoundButton buttonView,boolean isChecked){
				   if(isChecked)
					   setting.edit().putString("KEY_DELETE_WARN", "yes").commit();
				   else
					   setting.edit().putString("KEY_DELETE_WARN", "no").commit();}
		});
		
		chkDelPageWarn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() 
		{	   @Override
			   public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
				   if(isChecked)
					   setting.edit().putString("KEY_DELETE_PAGE_WARN", "yes").commit();
				   else
					   setting.edit().putString("KEY_DELETE_PAGE_WARN", "no").commit();}
		});
	}

//	public void addListenerOnButton(){
//		btnBack = (Button) findViewById(R.id.btnDisplay);
//		btnBack.setOnClickListener(new OnClickListener() 
//		{	@Override
//			public void onClick(View v) {
//				finish();}});
//	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    // Respond to the action bar's Up/Home button
	    case android.R.id.home:
	        finish();
//	        NavUtils.navigateUpFromSameTask(this); //became slow, why?
	        return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
}
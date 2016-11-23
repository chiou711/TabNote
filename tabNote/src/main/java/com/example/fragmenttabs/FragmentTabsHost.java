package com.example.fragmenttabs;

import java.lang.reflect.Field;
import java.util.ArrayList;

import yuku.iconcontextmenu.IconContextMenu;
import yuku.iconcontextmenu.IconContextMenu.IconContextItemSelectedListener;


import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.EditText;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

/* Bug:
 * 
 * 
 */
/**
 * Switching between the tabs of a TabHost through fragments, using FragmentTabHost.
 */
public class FragmentTabsHost extends FragmentActivity implements IconContextItemSelectedListener
{
	private FragmentTabHost mTabHost;
	static int mTabCount = 5;
	String TAB_SPEC_PREFIX = "tab";
	String TAB_SPEC;
	boolean bTabNameByDefault = true;
	// for DB
	private static Cursor mNotesCursor;
	private static DB mDbHelper;

	private static SharedPreferences lastPageViewPreferences;
	private static SharedPreferences setting;
	private static int mLastTabIndex;
	private static int mCurrentTabIndex;
	private ArrayList<String> tabIndicatorArrayList = new ArrayList<String>();
	private static Context mContext;
	private static int mFirstExistTabId =0;
	private static int mLastExistTabId =0;
	private static HorizontalScrollView mHorScrollView;

	@Override
	protected void onCreate(final Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		System.out.println("================start==================");
		setContentView(R.layout.activity_main);
		getOverflowMenu();
		// horizontal scroll tab host
		TabWidget tw = (TabWidget) findViewById(android.R.id.tabs);
		LinearLayout ll = (LinearLayout) tw.getParent();
		HorizontalScrollView hs = new HorizontalScrollView(this);
		mHorScrollView = hs;
		hs.setLayoutParams(new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT));
		ll.addView(hs, 0);
		ll.removeView(tw);
		hs.addView(tw);
		hs.setHorizontalScrollBarEnabled(false);

		mTabHost = (FragmentTabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
		//set tab indicator
		setTabIndicator();

		// set listener
		setListener();

		mContext = this.getBaseContext();

//    	if(mContext.equals(mTabHost.getContext()))
//    		Toast.makeText(mContext,"same",Toast.LENGTH_SHORT).show();
//    	else
//    		Toast.makeText(mContext,"not the same",Toast.LENGTH_SHORT).show();
	}


	protected void setTabIndicator()
	{
		// set default tab indicator
		if(bTabNameByDefault)
		{
			// get last view tab
			lastPageViewPreferences = getSharedPreferences("last_page_view", 0);
			String strLastPageViewNum = lastPageViewPreferences.getString("KEY_LAST_PAGE_VIEW","");
			System.out.println("strLastPageViewNum = " + strLastPageViewNum);
			if(strLastPageViewNum.equalsIgnoreCase("") )
			{
				// set default tab : first existence of tab
				strLastPageViewNum = "1"; //initialization
			}
			else
			{
				strLastPageViewNum = lastPageViewPreferences.getString("KEY_LAST_PAGE_VIEW","");
			}

			Context context = getApplicationContext();
			DB.setTableNumber(strLastPageViewNum);
			mDbHelper = new DB(context).open();
			mNotesCursor = mDbHelper.getAllTab();


			// insert when table is empty, activated only for the first time 
			if(mNotesCursor.getCount() == 0)
			{
				mDbHelper.insertTab("TAB_INFO","N1");
				mDbHelper.insertTab("TAB_INFO","N2");
				mDbHelper.insertTab("TAB_INFO","N3");
				mDbHelper.insertTab("TAB_INFO","N4");
				mDbHelper.insertTab("TAB_INFO","N5");
			}

			mNotesCursor = mDbHelper.getAllTab();
			mTabCount = mNotesCursor.getCount();
			int i = 0;
			while(i < mTabCount)
			{
				mNotesCursor.moveToPosition(i);
				String strTabName = mNotesCursor.getString(mNotesCursor.getColumnIndex("tab_name"));
				tabIndicatorArrayList.add(i,strTabName);

//	    		System.out.println("strTabName =" + strTabName);

				if(mNotesCursor.isFirst())
				{
					mFirstExistTabId = mNotesCursor.getInt(mNotesCursor.getColumnIndex("tab_id")) ;
//					System.out.println("mFirstExistTabId =" + mFirstExistTabId);
				}
				if(mNotesCursor.isLast())
				{

					mLastExistTabId = mNotesCursor.getInt(mNotesCursor.getColumnIndex("tab_id")) ;
//					System.out.println("mLastExistTabId =" + mLastExistTabId);
				}
				i++;
			}

			mNotesCursor = mDbHelper.getAllTab();
			for(int iPosition =0;iPosition<mTabCount;iPosition++)
			{
				mNotesCursor.moveToPosition(iPosition);
				if(Integer.valueOf(strLastPageViewNum) ==
						mNotesCursor.getInt(mNotesCursor.getColumnIndex("tab_id")))
				{
					mLastTabIndex = iPosition;	// mLastTabIndex starts from 0
//					System.out.println("mLastTabIndex = " + mLastTabIndex);
				}
			}
//	    	System.out.println("_setTabIndicator mLastTabIndex = " + mLastTabIndex);
		}
		else
		{
			tabIndicatorArrayList.add(0,"購物");
			tabIndicatorArrayList.add(1,"待辦");
			tabIndicatorArrayList.add(2,"普通");
			tabIndicatorArrayList.add(3,"重要");
			tabIndicatorArrayList.add(4,"極重要");
		}
	}

	@SuppressWarnings("deprecation")
	protected void setListener()
	{
		mTabHost.getTabWidget().setStripEnabled(false);
		int i = 0;
		while(i < mTabCount)
		{
			mNotesCursor.moveToPosition(i);
			int tabId = mNotesCursor.getInt(mNotesCursor.getColumnIndex("tab_id"));
//        	System.out.println("_setListener tabId =" + tabId);
			TAB_SPEC = TAB_SPEC_PREFIX.concat(String.valueOf(tabId));
//        	System.out.println("tabIndicatorArrayList.get(i) = " + tabIndicatorArrayList.get(i));

			mTabHost.addTab(mTabHost
							.newTabSpec(TAB_SPEC)
							.setIndicator(tabIndicatorArrayList.get(i)),
					NoteFragment.class, //interconnection
					null);

			//set text color
			TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
			tv.setTextColor(Color.rgb(0, 0, 0));

			//unselected background color
			Drawable draw = getResources().getDrawable(R.drawable.btn_square_unsel);
			if(Build.VERSION.SDK_INT >= 16)
				mTabHost.getTabWidget().getChildAt(i).setBackground(draw);
			else
				mTabHost.getTabWidget().getChildAt(i).setBackgroundDrawable(draw);

			// set tab text center
			int tabCount = mTabHost.getTabWidget().getTabCount();
			for (int j = 0; j < tabCount; j++) {
				final View view = mTabHost.getTabWidget().getChildTabViewAt(j);
				if ( view != null ) {
					//  get title text view
					final View textView = view.findViewById(android.R.id.title);
					if ( textView instanceof TextView ) {
						((TextView) textView).setGravity(Gravity.CENTER);
						((TextView) textView).setSingleLine(true);
						((TextView) textView).setPadding(6, 0, 6, 0);
						((TextView) textView).setMinimumWidth(96);
						textView.getLayoutParams().height = ViewGroup.LayoutParams.FILL_PARENT;
					}
				}
			}
			i++;
		}
//        System.out.println("_setListener mLastTabIndex = " + mLastTabIndex);
		//check high light position
		int highLightPosition = 0;
		mNotesCursor = mDbHelper.getAllTab();
		for(int iPosition =0;iPosition<mTabCount;iPosition++)
		{
			if(mLastTabIndex == iPosition)
			{
				highLightPosition = iPosition;
				System.out.println("highLightPosition = " + highLightPosition);
			}
		}

		//set background color to selected tab
		mTabHost.setCurrentTab(highLightPosition);

		//last selected background
		Drawable draw = getResources().getDrawable(R.drawable.btn_square_sel);
		if(Build.VERSION.SDK_INT >= 16)
			mTabHost.getTabWidget().getChildAt(highLightPosition).setBackground(draw); //null error ????
		else
			mTabHost.getTabWidget().getChildAt(highLightPosition).setBackgroundDrawable(draw);

		mCurrentTabIndex = mLastTabIndex;

		// scroll to last view
		mHorScrollView.post(new Runnable() {
			@Override
			public void run() {
				lastPageViewPreferences = getSharedPreferences("last_page_view", 0);
				int scrollX = lastPageViewPreferences.getInt("KEY_LAST_SCROLL_X", 0);
				System.out.println("scrollX = " + scrollX);
				mHorScrollView.scrollTo(scrollX, 0);
			}
		});

		// set on tab changed listener
		mTabHost.setOnTabChangedListener(new OnTabChangeListener()
										 {
											 @Override
											 public void onTabChanged(String tabSpec)
											 {
												 // get scroll X
												 int scrollX = mHorScrollView.getScrollX();
												 System.out.println("getScrollX() = " + scrollX);
												 lastPageViewPreferences = getSharedPreferences("last_page_view", 0);
												 lastPageViewPreferences.edit().putInt("KEY_LAST_SCROLL_X",scrollX).commit();

												 mNotesCursor = mDbHelper.getAllTab();
												 for(int i=0;i<mTabCount;i++)
												 {
													 mNotesCursor.moveToPosition(i);
													 int iTabId = mNotesCursor.getInt(mNotesCursor.getColumnIndex("tab_id"));
													 TAB_SPEC = TAB_SPEC_PREFIX.concat(String.valueOf(iTabId)); // TAB_SPEC starts from 1

													 if(TAB_SPEC.equals(tabSpec) )
													 {
														 System.out.println("_onTabChanged iTabId = " + iTabId);
														 mCurrentTabIndex = i;
														 System.out.println("onTabChanged mCurrentTabIndex " + mCurrentTabIndex);
														 lastPageViewPreferences.edit().putString("KEY_LAST_PAGE_VIEW",
																 String.valueOf(iTabId)).commit();

														 mTabHost.setCurrentTab(mCurrentTabIndex);

														 //selected background
														 Drawable draw = getResources().getDrawable(R.drawable.btn_square_sel);
														 if(Build.VERSION.SDK_INT >= 16)
															 mTabHost.getTabWidget().getChildAt(i).setBackground(draw);
														 else
															 mTabHost.getTabWidget().getChildAt(i).setBackgroundDrawable(draw);

														 for(int j=0;j<mTabCount;j++){
															 if(j != i){

																 //unselected background
																 Drawable draw2 = getResources().getDrawable(R.drawable.btn_square_unsel);
																 if(Build.VERSION.SDK_INT >= 16)
																	 mTabHost.getTabWidget().getChildAt(j).setBackground(draw2);
																 else
																	 mTabHost.getTabWidget().getChildAt(j).setBackgroundDrawable(draw2);
															 }
														 }
														 new FragmentLoader(String.valueOf(iTabId)); //load selected tab data
													 }
												 }
											 }
										 }
		);

		// set listener for editing tab info
		i = 0;
		while(i < mTabCount)
		{
			final int tabCursor = i;
			View tabView= mTabHost.getTabWidget().getChildAt(i);

			// on long click listener
			tabView.setOnLongClickListener(new OnLongClickListener()
			{
				@Override
				public boolean onLongClick(View v)
				{
					mNotesCursor = mDbHelper.getAllTab();
					mNotesCursor.moveToPosition(tabCursor);
					final int tabId =  mNotesCursor.getInt(mNotesCursor.getColumnIndex("tab_id"));

					if(mNotesCursor.isFirst())
						mFirstExistTabId = tabId;

//					System.out.println(" _onLongClick tabCursor = " + tabCursor);
//					System.out.println(" _onLongClick mCurrentTabIndex = " + mCurrentTabIndex);
//					System.out.println(" _onLongClick tabId = " + tabId);

					if(tabCursor == mCurrentTabIndex )
					{
						// get tab name
						String tabName = mNotesCursor.getString(mNotesCursor.getColumnIndex("tab_name"));

						final EditText editText1 = new EditText(getBaseContext());
						editText1.setText(tabName);
						editText1.setSelection(tabName.length()); // set edit text start position
						//update tab info
						Builder builder = new Builder(mTabHost.getContext());
						builder.setTitle("修改標籤")
								.setMessage("輸入標籤內容")
								.setView(editText1)
								.setNegativeButton("更新", new OnClickListener()
								{   @Override
								public void onClick(DialogInterface dialog, int which)
								{
									mNotesCursor = mDbHelper.getAllTab();
									mNotesCursor.moveToPosition(mCurrentTabIndex);
									final int tabId =  mNotesCursor.getInt(mNotesCursor.getColumnIndex("tab_id"));
									mDbHelper.updateTab("TAB_INFO", tabId, editText1.getText().toString());
									// Before _recreate, store late page number currently viewed
									lastPageViewPreferences.edit().putString("KEY_LAST_PAGE_VIEW", String.valueOf(tabId)).commit();
									System.out.println("====recreate=====");
									recreate();
								}
								})
								.setNeutralButton("刪除", new OnClickListener()
								{   @Override
								public void onClick(DialogInterface dialog, int which){
									//增加確認修改選擇:start
									setting = mContext.getSharedPreferences("delete_warn", 0);
									if(setting.getString("KEY_DELETE_PAGE_WARN","").equalsIgnoreCase("yes")){
										Builder builder1 = new Builder(mTabHost.getContext());
										builder1.setTitle("確認")
												.setMessage("要刪除此一頁?")
												.setNegativeButton("否", new OnClickListener(){
													@Override
													public void onClick(DialogInterface dialog1, int which1){
					                            		/*nothing to do*/}})
												.setPositiveButton("是,直接刪除", new OnClickListener(){
													@Override
													public void onClick(DialogInterface dialog1, int which1){
														deletePage(tabId);}})
												.show();} //增加確認修改選擇:end
									else{
										deletePage(tabId);
									}
								}
								})
								.setPositiveButton("取消", new OnClickListener(){
									@Override
									public void onClick(DialogInterface dialog, int which)
									{/*nothing*/}})
								.show();
					}
					return true;
				}
			});
			i++;
		}

	}//setListener()

	//?
	/*
    private void openOptionsDialog() {
//    	Toast.makeText(Bmi.this, "BMI 計算器", Toast.LENGTH_SHORT).show();

        new AlertDialog.Builder(FragmentTabsHost.this)
        .setTitle("title")
        .setMessage("message")
        .setPositiveButton("確認",
        new DialogInterface.OnClickListener(){
            public void onClick(
                DialogInterface dialoginterface, int i){
            }
        })
       .setNegativeButton("lable",
    	new DialogInterface.OnClickListener(){
        public void onClick(
          DialogInterface dialoginterface, int i){
        	//Home Page
            Uri uri = Uri.parse("http://sites.google.com/site/gasodroid/");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        	//MAP
//        	Uri uri = Uri.parse("geo:25.047192, 121.516981");
//        	final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        	//DIAL
//            Uri uri = Uri.parse("tel:12345678");
//            Intent intent = new Intent(Intent.ACTION_DIAL, uri);
            startActivity(intent);
        }
        })
        .show();
    }

    protected static final int MENU_ABOUT = Menu.FIRST;
    protected static final int MENU_QUIT = Menu.FIRST+1;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ABOUT, 0, "關於...").setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, MENU_QUIT, 0, "結束").setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()) {
		case MENU_ABOUT:
		    openOptionsDialog();
		    break;
		case MENU_QUIT:
		    finish();
		    break;
		}
		return super.onOptionsItemSelected(item);
	}
*/

	/**
	 * 功能選項
	 */
	// Menu identifiers
	static final int ADD_NEW_ID = R.id.ADD_NEW_ID;
	static final int ADD_NEW_PAGE = R.id.ADD_NEW_PAGE;
	static final int CONFIG_ID = R.id.CONFIG_ID;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

//		MenuInflater menuInflater = this.getMenuInflater();
//		getMenuInflater().inflate(R.menu.settings, menu);


		menu.add(0, ADD_NEW_ID, 1, "Add New Note")
				.setIcon(R.drawable.add_new)
				.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		SubMenu subMenu = menu.addSubMenu(0, 0, 2, "More");

		subMenu.add(0, ADD_NEW_PAGE, 2, "Add New Page")
				.setIcon(R.drawable.add_new_page);

		subMenu.add(0, CONFIG_ID, 3, "Settings")
				.setIcon(R.drawable.settings);

		MenuItem subMenuItem = subMenu.getItem();
		subMenuItem.setIcon(R.drawable.ic_action_overflow);
		subMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return super.onCreateOptionsMenu(menu);
	}

//	//icon context menu
//	public IconContextItemSelectedListener setOnIconContextItemSelected()
//	{
//		return null;
//		
//	}

	private void getOverflowMenu() {

		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if(menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override public boolean onOptionsItemSelected(MenuItem item) {
		System.out.println("item.getItemId() = " + item.getItemId());
		switch (item.getItemId()) {
			case ADD_NEW_PAGE:
				lastPageViewPreferences.edit().putString("KEY_LAST_PAGE_VIEW",
						String.valueOf(mLastExistTabId + 1)).commit(); //why no use???????????
				addNewPage(mLastExistTabId + 1);
				return true;
			case CONFIG_ID:
				Intent intent = new Intent(mContext, Config.class);
				startActivity(intent);
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	public  void addNewPage(int newTabId) {
		// set scroll X
		lastPageViewPreferences = getSharedPreferences("last_page_view", 0);
		int scrollX = (mTabCount) * 60 * 5; //over the last scroll X
		System.out.println("_addNewPage scrollX = " + scrollX);
		lastPageViewPreferences.edit().putInt("KEY_LAST_SCROLL_X",scrollX).commit();

		// insert tab name
		mDbHelper.insertTab("TAB_INFO","N".concat(String.valueOf(newTabId)));

		// insert table for new tab
		mDbHelper.insertNewTable(newTabId);
		mTabCount++;
		mDbHelper.close();
		mTabHost.clearAllTabs(); //must add this in order to clear onTanChange event
		System.out.println("==== recreate new tab =====");
		recreate();
	}

	public  void deletePage(int TabId) {

		//if current page is the first page and will be delete,
		//try to get next existence of page
		if(mNotesCursor.getCount() != 1){
			mNotesCursor = mDbHelper.getAllTab();
			mNotesCursor.moveToPosition(mCurrentTabIndex);
			final int tabId =  mNotesCursor.getInt(mNotesCursor.getColumnIndex("tab_id"));
			if(tabId == mFirstExistTabId)
			{
				int cGetNextExistIndex = mCurrentTabIndex+1;
				boolean bGotNext = false;
				while(!bGotNext){
					mNotesCursor = mDbHelper.getAllTab();
					mNotesCursor.moveToPosition(cGetNextExistIndex);
					try{
						mFirstExistTabId =  mNotesCursor.getInt(mNotesCursor.getColumnIndex("tab_id"));
						System.out.println("tab is GOT!");
						bGotNext = true;
					}catch(Exception e){
						System.out.println("tab is not got!");
						bGotNext = false;
						cGetNextExistIndex++;}}
				System.out.println("change strLastPageViewNum to: " + mFirstExistTabId);
			}
			//change to first existing page
			lastPageViewPreferences.edit()
					.putString("KEY_LAST_PAGE_VIEW",String.valueOf(mFirstExistTabId)).commit();
		}
		else{
			Toast.makeText(FragmentTabsHost.this, "Please keep one note at least", Toast.LENGTH_SHORT).show();
		}

		// set scroll X
		lastPageViewPreferences = getSharedPreferences("last_page_view", 0);
		int scrollX = 0; //over the last scroll X
		System.out.println("_deletePage scrollX = " + scrollX);
		lastPageViewPreferences.edit().putInt("KEY_LAST_SCROLL_X",scrollX).commit();

		// delete tab name
		mNotesCursor = mDbHelper.getAllTab();
		mDbHelper.deleteTabInfo("TAB_INFO",TabId);

		// drop tab
		mDbHelper.dropTable(TabId);
		mTabCount--;
		mDbHelper.close();
		mTabHost.clearAllTabs();
		System.out.println("==== recreate delete tab =====");
		recreate();
	}


	@Override
	public void onIconContextItemSelected(MenuItem arg0, Object arg1) {
		// TODO Auto-generated method stub

	}



}
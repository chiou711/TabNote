package com.example.fragmenttabs;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fragmenttabs.FragmentLoader.NoteEntry;
import com.example.fragmenttabs.FragmentLoader.NoteListAdapter;
import com.example.fragmenttabs.FragmentLoader.NoteListLoader;
import com.terlici.dragndroplist.DragNDropCursorAdapter;
import com.terlici.dragndroplist.DragNDropListView;
import com.terlici.dragndroplist.DragNDropListView.OnItemDragNDropListener;

// main control
public class NoteFragment extends ListFragment implements
		LoaderManager.LoaderCallbacks<List<NoteEntry>>
{
	private static Cursor mNotesCursor;
	private static DB mDbHelper;
	public static SharedPreferences setting;
	private static EditText editText1;
	private static EditText editText2;
	private static String editString1;
	private static int mColumnIndex = 0;
	private static int mNoteNumber1 = 1;
	private static String mNoteString1;
	private static int mMarkingIndex1;
	private static long mCreatedTime1;
	private static int mNoteNumber2 ;
	private static String mNoteString2;
	private static int mMarkingIndex2;
	private static long mCreatedTime2;
	private static List<Boolean> mHighlightList = new ArrayList<Boolean>();

	// This is the Adapter being used to display the list's data.
	NoteListAdapter mAdapter;
	DragNDropListView mDndListView;
	ImageView mView2;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mDndListView = (DragNDropListView)getActivity().findViewById(R.id.list1);
		mDndListView.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> mAdapter, View v, int position, long id)
			{
//				onListItemClick2(mDndListView, v, position, id);

			}
		});

		mDndListView.setOnItemLongClickListener(new OnItemLongClickListener()
		{
			@Override
			public boolean onItemLongClick(AdapterView<?> mAdapter, View v, int position, long id)
			{
				onListItemClick2(mDndListView, v, position, id);
				return true;
			}
		});


		// set drag and drop listener
		OnItemDragNDropListener listener = new OnItemDragNDropListener()
		{
			// on drag
			@Override
			public void onItemDrag(DragNDropListView parent, View view, int position, long id)
			{
				view.findViewById(R.id.text).setBackgroundColor(Color.rgb(255,128,0));

				mHighlightList.set(position, !mHighlightList.get(position));
				System.out.println("position = " + position);
			}

			// on drop
			@Override
			public void onItemDrop(DragNDropListView parent, View view,
								   int startPosition, int endPosition, long id)
			{
//				view.setBackgroundColor(parent.getCacheColorHint());
//				view.findViewById(R.id.text).setBackgroundColor(Color.rgb(128,128,0));

				mHighlightList.set(startPosition, true);
				mHighlightList.set(endPosition, true);

				System.out.println("startPosition = " + startPosition);
				System.out.println("endPosition = " + endPosition);

				//reorder data base storage
				int loop = Math.abs(startPosition-endPosition);
				for(int i=0;i< loop;i++)
				{
					swapRows(startPosition,endPosition);
					if((startPosition-endPosition) >0)
						endPosition++;
					else
						endPosition--;
				}
			}

			// on click
			@Override
			public void onItemClick(DragNDropListView parent, View view,
									int position, long id)
			{
				final Context context = getActivity();
				mDbHelper = new DB(context);
				Cursor cursor = mDbHelper.getAll();
				System.out.println("_onItemClick position = " + position);
				System.out.println("_onItemClick id = " + id);
				cursor.moveToPosition(position);
				String strNote = cursor.getString(cursor.getColumnIndex("note"));
				int idDB = cursor.getInt(cursor.getColumnIndex("_id"));
//                System.out.println(" marking = " +  cursor.getLong(cursor.getColumnIndex("marking")));

				ImageView image = (ImageView) view.findViewById(R.id.handler2);

				if( cursor.getLong(cursor.getColumnIndex("marking")) == 0)
				{
					image.setImageResource(android.R.drawable.checkbox_on_background);
					mDbHelper.update(idDB, strNote,1);
				}
				else
				{
					image.setImageResource(android.R.drawable.checkbox_off_background);
					mDbHelper.update(idDB, strNote,0);
				}

				fillData();
			}

			// on click
			@Override
			public void onItemLongClick(DragNDropListView parent, View view,
										int position, long id)
			{
			}
		};



		mDndListView.setOnItemDragNDropListener(listener);

		// Give some text to display if there is no data. In a real
		// application this would come from a resource.

		// We have a menu item to show in action bar.
		setHasOptionsMenu(true);

		// Create an empty adapter we will use to display the loaded data.
		mAdapter = new NoteListAdapter(getActivity());
		setListAdapter(mAdapter);

		// Start out with a progress indicator.
		setListShown(false); //cw@ / 不用progress indicator看起來比較快

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(0, null, this);
	}

	protected void swapRows(int startPosition, int endPosition) {

		mNotesCursor = mDbHelper.getAll();

		mNotesCursor.moveToPosition((int)startPosition);
		mNoteNumber1 = mNotesCursor.getInt(mNotesCursor.getColumnIndex("_id"));
		mNoteString1 = mNotesCursor.getString(mNotesCursor.getColumnIndex("note")) ;
		mMarkingIndex1 = mNotesCursor.getInt(mNotesCursor.getColumnIndex("marking")) ;
		mCreatedTime1 = mNotesCursor.getLong(mNotesCursor.getColumnIndex("created")) ;

		mNotesCursor.moveToPosition((int)endPosition);
		mNoteNumber2 = mNotesCursor.getInt(mNotesCursor.getColumnIndex("_id"));
		mNoteString2 = mNotesCursor.getString(mNotesCursor.getColumnIndex("note")) ;
		mMarkingIndex2 = mNotesCursor.getInt(mNotesCursor.getColumnIndex("marking")) ;
		mCreatedTime2 = mNotesCursor.getLong(mNotesCursor.getColumnIndex("created")) ;

		mDbHelper.update(mNoteNumber2,
				mNoteString1,
				mMarkingIndex1);

		mDbHelper.update(mNoteNumber1,
				mNoteString2,
				mMarkingIndex2);
	}

	@Override
	public Loader<List<NoteEntry>> onCreateLoader(int id, Bundle args) {
		// This is called when a new Loader needs to be created. This
		// sample only has one Loader with no arguments, so it is simple.
		return new NoteListLoader(getActivity());
	}

	@Override
	public void onLoadFinished(Loader<List<NoteEntry>> loader,
							   List<NoteEntry> data)
	{
		// Set the new data in the adapter.
		mAdapter.setData(data);

		// The list should now be shown.
		if (isResumed())
		{
			setListShown(true);
		}
		else
		{
			setListShownNoAnimation(true);
		}
//		mSetDefaultBackgroundColor = true;
		fillData();


	}

	@Override
	public void onLoaderReset(Loader<List<NoteEntry>> loader) {
		// Clear the data in the adapter.
		mAdapter.setData(null);
	}


	// 顯示資料
	public void fillData()
	{
		final Context context = this.getActivity();
		mDbHelper = new DB(context);

		mNotesCursor = mDbHelper.getAll();

		String[] from = new String[] { DB.KEY_NOTE };
		int[] to = new int[] { R.id.text };

//        int vw = findViewById(R.id.text).getBackground();

		DragNDropCursorAdapter adapter = new DragNDropCursorAdapter(getActivity().getBaseContext(),
				R.layout.select_item,
				mNotesCursor,
				from,
				to,
//          											R.id.handler,
				R.id.handler2 , //drag
				R.id.handler2, //click
				R.id.text); //long click
		//change list item color
		// 1. create a new ViewBinder
		DragNDropCursorAdapter.ViewBinder binder = new DragNDropCursorAdapter.ViewBinder()
		{
			@Override
			public boolean setViewValue(View view, Cursor cursor, int columnIndex)
			{
				int getIndex = cursor.getColumnIndex("note");
				String empname = cursor.getString(getIndex);
				int markingIndex = cursor.getColumnIndex("marking");

				View parentView = (View) view.getParent();

//                System.out.println("markingIndex = " + markingIndex);
				TextView tv = (TextView) view;
				tv.setText(empname);

//                ((ImageView) parentView.findViewById(R.id.handler)).setBackgroundColor(Color.rgb(15, 30, 15));
				((ImageView) parentView.findViewById(R.id.handler2)).setBackgroundColor(Color.rgb(15, 30, 15));
				if( cursor.getLong(markingIndex) == 1)
				{
					((ImageView) parentView.findViewById(R.id.handler2)).setImageResource(android.R.drawable.checkbox_on_background);
					tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
					tv.setBackgroundColor(Color.rgb(55,127,19));
				}
				else
				{
					((ImageView) parentView.findViewById(R.id.handler2)).setImageResource(android.R.drawable.checkbox_off_background);
					tv.setPaintFlags(tv.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
					tv.setBackgroundColor(Color.rgb(186,249,142));
				}
				return true;
			}
		};

		//2. set the new ViewBinder for adapter
		adapter.setViewBinder(binder);

		//footer
		if(mDndListView.getFooterViewsCount() == 0)
		{
			mDndListView.addFooterView(getActivity().getLayoutInflater().inflate(R.layout.footer, null));
		}

		mDndListView.setDragNDropAdapter(adapter);

		// for highlight
		mNotesCursor = mDbHelper.getAll();
		for(int i=0; i< mNotesCursor.getCount() ; i++ )
		{
			mHighlightList.add(true);
		}
		for(int i=0; i< mNotesCursor.getCount() ; i++ )
		{
			mHighlightList.set(i,true);
		}
	}

	// add new note item
	public void addNewItem()
	{
		final Context context = getActivity();
		String noteName = "";
		editText2 = new EditText(context);
		editText2.setText(noteName);
		editText2.setSelection(noteName.length()); // set cursor start
		Builder builder1 = new Builder(context);
		builder1.setTitle("新增記事?")
				.setMessage("輸入記事內容")
				.setView(editText2)
				.setPositiveButton("新增",
						new OnClickListener()
						{
							@Override
							public void onClick(DialogInterface dialog, int which)
							{
								editString1 = editText2.getText().toString();
								if (!editString1.equals(""))
								{
									mDbHelper.insert(editString1);
								}
								fillData();
							}
						}).show();
	}

	// 修改|劃掉 |刪除
//	@Override
	public void onListItemClick2(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
//		v.setBackgroundColor(Color.BLACK);

		mNotesCursor = mDbHelper.getAll();

		System.out.println("onListItemClick2 position = " + position);
		System.out.println("mNotesCursor.getCount()" + mNotesCursor.getCount());

		if(position >= mNotesCursor.getCount()) // avoid footer error
			return;
		else
			mNotesCursor.moveToPosition((int)position);

		// get DB Id number
		mColumnIndex = mNotesCursor.getColumnIndex("_id");
		mNoteNumber1 = mNotesCursor.getInt(mColumnIndex);

		//get Note string
		mColumnIndex = mNotesCursor.getColumnIndex("note");
		String str = mNotesCursor.getString(mColumnIndex) ;
		System.out.println("str = " + str);

		final Context context = getActivity();
		editText1 = new EditText(context);
		editText1.setText(str);
		editText1.setSelection(str.length()); // set edit text start position

		Builder builder = new Builder(context);
		builder.setTitle("修改記事")
				.setMessage("輸入記事內容")
				.setView(editText1)
				.setNegativeButton("更新", new OnClickListener()
				{   @Override
				public void onClick(DialogInterface dialog, int which)
				{
					editString1 = editText1.getText().toString();
					if(!editString1.equals(""))
					{
						mDbHelper.update(mNoteNumber1, editText1.getText().toString(),0);
						fillData();
					}
				}
				})
				.setNeutralButton("劃掉", new OnClickListener()
				{   @Override
				public void onClick(DialogInterface dialog, int which)
				{
					editString1 = editText1.getText().toString();
					mDbHelper.update(mNoteNumber1, editText1.getText().toString(),1);
					fillData();
				}
				})
				.setPositiveButton("刪除", new OnClickListener()
				{   @Override
				public void onClick(DialogInterface dialog, int which)
				{
					//增加確認修改選擇:start
					setting = context.getSharedPreferences("delete_warn", 0);
					if(setting.getString("KEY_DELETE_WARN","").equalsIgnoreCase("yes"))
					{
						Builder builder1 = new Builder(context);
						builder1.setTitle("確認")
								.setMessage("要刪除此一記事內容?")
								.setNegativeButton("否", new OnClickListener()
								{   @Override
								public void onClick(DialogInterface dialog1, int which1){
	                            	/*nothing to do*/}})
								.setPositiveButton("是,直接刪除", new OnClickListener()
								{   @Override
								public void onClick(DialogInterface dialog1, int which1){
									mDbHelper.delete(mNoteNumber1);
									fillData();}})
								.show();//增加確認修改選擇:end
					}
					else{
						//不增加確認修改選擇:start
						mDbHelper.delete(mNoteNumber1);
						fillData();
					}
				}
				})
				.show();
	}

	/**
	 * 功能選項
	 *
	 */
	// Menu identifiers
//    static final int ADD_NEW_ID = Menu.FIRST;
	static final int ADD_NEW_ID = R.id.ADD_NEW_ID;
	@Override public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
			case ADD_NEW_ID:
				addNewItem();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
}

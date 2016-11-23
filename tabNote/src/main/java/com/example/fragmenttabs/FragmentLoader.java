/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.fragmenttabs;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.AsyncTaskLoader;
import android.widget.ArrayAdapter;

public class FragmentLoader extends FragmentActivity 
{
	private static String mTableNumber;
	
	FragmentLoader()
	{}
	
	FragmentLoader(String str)
	{
        mTableNumber = str;
		DB.setTableNumber(mTableNumber);
	}

	// note unit
	public static class NoteEntry 
	{
		private String mNote;

		public String getNote() {
			return mNote;
		}

		public void setNote(String mNote) {
			this.mNote = mNote;
		}
	}

	// note list loader
	public static class NoteListLoader extends AsyncTaskLoader<List<NoteEntry>> 
	{
		List<NoteEntry> mApps;

		public NoteListLoader(Context context) {
			super(context);
		}

		@Override
		public List<NoteEntry> loadInBackground() {
			List<NoteEntry> entries = new ArrayList<NoteEntry>();
			return entries;
		}

		@Override
		protected void onStartLoading() {
			final Context context = getContext();
			System.out.println("_onStartLoading");
			new DB(context).open();
			forceLoad();
		}
	}

	// note list adapter
	public static class NoteListAdapter extends ArrayAdapter<NoteEntry> 
	{
		public NoteListAdapter(Context context) {
			super(context, android.R.layout.simple_list_item_1);
		}
		public void setData(List<NoteEntry> data) {
			clear();
			if (data != null) {
				addAll(data);
			}
		}
	}
}
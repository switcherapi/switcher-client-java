package com.switcher.client.domain;

import java.util.ArrayList;
import java.util.List;

import com.switcher.client.factory.SwitcherExecutor;

public class Switcher {
	
	public static final String KEY = "key";
	public static final String SHOW_REASON = "showReason";
	public static final String BYPASS_METRIC = "bypassMetric";
	
	private SwitcherExecutor context;
	private String key;
	private List<Entry> entry;
	
	public Switcher(final String key, final SwitcherExecutor context) {
		
		this.key = key;
		this.context = context;
	}
	
	public void prepareEntry(final List<Entry> entry) {
		
		this.entry = entry;
	}
	
	public void prepareEntry(final Entry entry, final boolean add) {
		
		if (this.entry == null) {
			this.entry = new ArrayList<>();
		}
		
		if (!add) this.entry.clear();
		
		if (!this.entry.contains(entry)) {
			this.entry.add(entry);
		}
	}
	
	public void prepareEntry(final Entry entry) {
		
		this.prepareEntry(entry, true);
	}
	
	public boolean isItOn(final String key, final Entry entry, final boolean add) throws Exception {
		
		this.prepareEntry(entry, add);
		return this.isItOn(key);
	}
	
	public boolean isItOn(final List<Entry> entry) throws Exception {
		
		this.entry = entry;
		return this.isItOn();
	}
	
	public boolean isItOn(final String key) throws Exception {
		
		this.key = key;
		return this.isItOn();
	}
	
	public boolean isItOn() throws Exception {
		
		return this.context.executeCriteria(this);
	}
	
	public GsonInputRequest getInputRequest() {
		
		return new GsonInputRequest(this.entry != null ? this.entry.toArray(new Entry[this.entry.size()]) : null);
	}

	public String getKey() {
		
		return this.key;
	}

	public List<Entry> getEntry() {
		
		return this.entry;
	}
	
	@Override
	public String toString() {
		
		return "Switcher [key=" + key + ", entry=" + entry + "]";
	}
	
	public class GsonInputRequest {
		
		private Entry[] entry;
		
		public GsonInputRequest(final Entry[] entry) {
			this.entry = entry;
		}

		public Entry[] getEntry() {
			return this.entry;
		}
	}

}

package com.switcher.client.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.switcher.client.SwitcherFactory;
import com.switcher.client.exception.SwitcherException;
import com.switcher.client.factory.SwitcherExecutor;

/**
 * Switchers are responsible for create calls between your application and Switcher API.
 * <br>To invoke the criteria, please use one of the available methods: {@link #isItOn()}.
 * 
 * @author rogerio
 * @since 2019-12-24
 * 
 * @see #isItOn()
 * @see #isItOn(List)
 * @see #isItOn(String)
 * @see #isItOn(String, Entry, boolean)
 *
 */
public class Switcher {
	
	public static final String KEY = "key";
	public static final String SHOW_REASON = "showReason";
	public static final String BYPASS_METRIC = "bypassMetric";
	
	private SwitcherExecutor context;
	private String key;
	private List<Entry> entry;
	private Map<String, Boolean> bypass;
	
	/**
	 * Use {@link SwitcherFactory#buildContext(Map, boolean)} to create this object.
	 * 
	 * @param key
	 * @param context
	 */
	public Switcher(final String key, final SwitcherExecutor context) {
		
		this.key = key;
		this.context = context;
		this.bypass = new HashMap<>();
	}
	
	/**
	 * Prepare the Switcher including a list of inputs necessary to run the criteria afterwards.
	 * 
	 * @param entry
	 */
	public void prepareEntry(final List<Entry> entry) {
		
		this.entry = entry;
	}
	
	/**
	 * Prepare the Switcher including a list of inputs necessary to run the criteria afterwards.
	 * 
	 * @param entry
	 * @param add if false, the list will be cleaned and the entry provided will be the only input for this Switcher.
	 */
	public void prepareEntry(final Entry entry, final boolean add) {
		
		if (this.entry == null) {
			this.entry = new ArrayList<>();
		}
		
		if (!add) this.entry.clear();
		
		if (!this.entry.contains(entry)) {
			this.entry.add(entry);
		}
	}
	
	/**
	 * It adds an input to the list of inputs.
	 * <br>Under the table it calls {@link #prepareEntry(Entry, boolean)} passing true to the second argument.
	 * 
	 * @param entry
	 */
	public void prepareEntry(final Entry entry) {
		
		this.prepareEntry(entry, true);
	}
	
	/**
	 * Convinient method to send all the information necessary to run the criteria with input.
	 * 
	 * @param key
	 * @param entry
	 * @param add
	 * @return
	 * @throws SwitcherException
	 */
	public boolean isItOn(final String key, final Entry entry, final boolean add) throws SwitcherException {
		
		this.prepareEntry(entry, add);
		return this.isItOn(key);
	}
	
	/**
	 * This method is going to invoke the criteria overwriting the existing input if it was added earlier.
	 * 
	 * @param entry
	 * @return
	 * @throws Exception
	 */
	public boolean isItOn(final List<Entry> entry) throws Exception {
		
		this.entry = entry;
		return this.isItOn();
	}
	
	/**
	 * This method will invoke the Switcher API according to the key provided.
	 * 
	 * @param key
	 * @return
	 * @throws SwitcherException
	 */
	public boolean isItOn(final String key) throws SwitcherException {
		
		this.key = key;
		return this.isItOn();
	}
	
	/**
	 * Invoke a given key provided via {@link SwitcherFactory#getSwitcher(String)}.
	 * <br>It's possible to chanage the key name for the Switcher even after its creation.
	 * <br>
	 * <br> For example:
	 * <br> You can create a Switcher invoking SwitcherFactory#getSwitcher("MY_KEY"), however, you can also change this key value by another using
	 * {@link #isItOn(String)}.
	 * 
	 * @return 
	 * @throws Exception
	 */
	public boolean isItOn() throws SwitcherException {
		
		if (this.bypass.containsKey(key)) {
			return this.bypass.get(key);
		}
		
		return this.context.executeCriteria(this);
	}
	
	/**
	 * It manipulates the result of a given key.
	 * 
	 * @param key
	 * @param expepectedResult
	 */
	public void assume(final String key, boolean expepectedResult) {
		
		this.bypass.put(key, expepectedResult);
	}
	
	/**
	 * It will clean up any result manipulation added before by invoking {@link Switcher#assume(String, boolean)}
	 * 
	 * @param key
	 */
	public void forget(final String key) {
		
		this.bypass.remove(key);
	}
	
	/**
	 * This method build up the request made by the client to reach the Switcher API.
	 * 
	 * @return
	 */
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

package com.github.petruki.switcher.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.petruki.switcher.client.SwitcherFactory;
import com.github.petruki.switcher.client.exception.SwitcherException;
import com.github.petruki.switcher.client.factory.SwitcherExecutor;

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
	private String switcherKey;
	private List<Entry> entry;
	private Map<String, Boolean> bypass;
	
	/**
	 * Use {@link SwitcherFactory#buildContext(Map, boolean)} to create this object.
	 * 
	 * @param switcherKey name of the key created
	 * @param context configuration object containing all information to start using switchers
	 */
	public Switcher(final String switcherKey, final SwitcherExecutor context) {
		
		this.switcherKey = switcherKey;
		this.context = context;
		this.bypass = new HashMap<>();
	}
	
	/**
	 * Prepare the Switcher including a list of inputs necessary to run the criteria afterwards.
	 * 
	 * @param entry input object
	 */
	public void prepareEntry(final List<Entry> entry) {
		
		this.entry = entry;
	}
	
	/**
	 * Prepare the Switcher including a list of inputs necessary to run the criteria afterwards.
	 * 
	 * @param entry input object
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
	 * @param entry input object
	 */
	public void prepareEntry(final Entry entry) {
		
		this.prepareEntry(entry, true);
	}
	
	/**
	 * Convinient method to send all the information necessary to run the criteria with input.
	 * 
	 * @param key name of the key created
	 * @param entry input object
	 * @param add if false, the list will be cleaned and the entry provided will be the only input for this Switcher.
	 * @return criteria result
	 * @throws SwitcherException connectivity or criteria errors regarding reading malformed snapshots
	 */
	public boolean isItOn(final String key, final Entry entry, final boolean add) throws SwitcherException {
		
		this.prepareEntry(entry, add);
		return this.isItOn(key);
	}
	
	/**
	 * This method is going to invoke the criteria overwriting the existing input if it was added earlier.
	 * 
	 * @param entry input object
	 * @return criteria result
	 * @throws SwitcherException connectivity or criteria errors regarding reading malformed snapshots
	 */
	public boolean isItOn(final List<Entry> entry) throws SwitcherException {
		
		this.entry = entry;
		return this.isItOn();
	}
	
	/**
	 * This method will invoke the Switcher API according to the key provided.
	 * 
	 * @param key name of the key created
	 * @return criteria result
	 * @throws SwitcherException connectivity or criteria errors regarding reading malformed snapshots
	 */
	public boolean isItOn(final String key) throws SwitcherException {
		
		this.switcherKey = key;
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
	 * @return criteria result
	 * @throws SwitcherException connectivity or criteria errors regarding reading malformed snapshots
	 */
	public boolean isItOn() throws SwitcherException {
		
		if (this.bypass.containsKey(switcherKey)) {
			return this.bypass.get(switcherKey);
		}
		
		return this.context.executeCriteria(this);
	}
	
	/**
	 * It manipulates the result of a given key.
	 * 
	 * @param key name of the key that you want to change the result
	 * @param expepectedResult result that will be returned when performing isItOn
	 */
	public void assume(final String key, boolean expepectedResult) {
		
		this.bypass.put(key, expepectedResult);
	}
	
	/**
	 * It will clean up any result manipulation added before by invoking {@link Switcher#assume(String, boolean)}
	 * 
	 * @param key name of the key you want to remove
	 */
	public void forget(final String key) {
		
		this.bypass.remove(key);
	}
	
	/**
	 * This method build up the request made by the client to reach the Switcher API.
	 * 
	 * @return json input request
	 */
	public GsonInputRequest getInputRequest() {
		
		return new GsonInputRequest(this.entry != null ? this.entry.toArray(new Entry[this.entry.size()]) : null);
	}

	public String getSwitcherKey() {
		
		return this.switcherKey;
	}

	public List<Entry> getEntry() {
		
		return this.entry;
	}
	
	@Override
	public String toString() {
		
		return "Switcher [switcherKey=" + switcherKey + ", entry=" + entry + "]";
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

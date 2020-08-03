package com.github.switcherapi.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.switcherapi.client.SwitcherFactory;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.factory.SwitcherExecutor;
import com.github.switcherapi.client.model.response.CriteriaResponse;

/**
 * Switchers are responsible for wrapping the input and output coming from the Switcher API.
 * <br>To execute a criteria evaluation, use one of the available methods: {@link #isItOn()}.
 * 
 * <p>To assign an input value for this Switcher, you can use one of the chained methods: prepareEntry
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
	
	private List<CriteriaResponse> historyExecution;
	
	private boolean bypassMetrics = Boolean.FALSE;
	
	private boolean showReason = Boolean.FALSE;
	
	/**
	 * Use {@link SwitcherFactory#buildContext(Map, boolean)} to create this object.
	 * 
	 * @param switcherKey name of the key created
	 * @param context configuration object containing all information to start using switchers
	 */
	public Switcher(final String switcherKey, final SwitcherExecutor context) {
		
		this.switcherKey = switcherKey;
		this.context = context;
		this.historyExecution = new ArrayList<>();
	}
	
	/**
	 * Prepare the Switcher including a list of inputs necessary to run the criteria afterward.
	 * 
	 * @param entry input object
	 */
	public Switcher prepareEntry(final List<Entry> entry) {
		
		this.entry = entry;
		return this;
	}
	
	/**
	 * Prepare the Switcher including a list of inputs necessary to run the criteria afterward.
	 * 
	 * @param entry input object
	 * @param add if false, the list will be cleaned and the entry provided will be the only input for this Switcher.
	 */
	public Switcher prepareEntry(final Entry entry, final boolean add) {
		
		if (this.entry == null) {
			this.entry = new ArrayList<>();
		}
		
		if (!add) this.entry.clear();
		
		if (!this.entry.contains(entry)) {
			this.entry.add(entry);
		}
		
		return this;
	}
	
	/**
	 * It adds an input to the list of inputs.
	 * <br>Under the table it calls {@link #prepareEntry(Entry, boolean)} passing true to the second argument.
	 * 
	 * @param entry input object
	 */
	public Switcher prepareEntry(final Entry entry) {
		
		return this.prepareEntry(entry, true);
	}
	
	/**
	 * Convenient method to send all the information necessary to run the criteria with input.
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
	 * Execute criteria based on a given switcher key provided via {@link SwitcherFactory#getSwitcher(String)}.
	 * <br>The detailed result is available in list of {@link CriteriaResponse}.
	 * <br>It's possible to change the switcher key even after instantiating a Switcher object.
	 * <br>
	 * <br> For example:
	 * <br> You can create a Switcher by invoking SwitcherFactory#getSwitcher("MY_KEY"), plus, you can also change this key value by another using
	 * {@link #isItOn(String)}.
	 * 
	 * @return criteria result
	 * @throws SwitcherException connectivity or criteria errors regarding reading malformed snapshots
	 */
	public boolean isItOn() throws SwitcherException {
		
		if (SwitcherExecutor.getBypass().containsKey(switcherKey)) {
			return SwitcherExecutor.getBypass().get(switcherKey);
		}
		
		final CriteriaResponse response = this.context.executeCriteria(this);
		this.historyExecution.add(response);
		return response.isItOn();
	}
	
	/**
	 * This method builds up the request made by the client to reach the Switcher API.
	 * 
	 * @return json input request
	 */
	public GsonInputRequest getInputRequest() {
		
		return new GsonInputRequest(this.entry != null ? this.entry.toArray(new Entry[this.entry.size()]) : null);
	}

	public boolean isBypassMetrics() {
		
		return bypassMetrics;
	}

	public void setBypassMetrics(boolean bypassMetrics) {
		
		this.bypassMetrics = bypassMetrics;
	}

	public boolean isShowReason() {
		
		return showReason;
	}

	public void setShowReason(boolean showReason) {
		
		this.showReason = showReason;
	}

	public String getSwitcherKey() {
		
		return this.switcherKey;
	}

	public List<Entry> getEntry() {
		
		return this.entry;
	}

	public List<CriteriaResponse> getHistoryExecution() {
		
		return this.historyExecution;
	}

	@Override
	public String toString() {
		
		return String.format("Switcher [switcherKey= %s, entry= %s, bypassMetrics= %s, showReason= %s]", 
				switcherKey, entry, bypassMetrics, showReason);
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

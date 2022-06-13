package com.github.switcherapi.client.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.switcherapi.client.SwitcherContext;
import com.github.switcherapi.client.SwitcherExecutor;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.model.response.CriteriaResponse;

/**
 * Switchers are responsible for wrapping the input and output coming from the Switcher API.
 * <br>To execute a criteria evaluation, use one of the available methods: {@link #isItOn()}.
 * 
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 * 
 * @see #isItOn()
 * @see #isItOn(List)
 * @see #isItOn(Entry, boolean)
 *
 */
public class Switcher extends SwitcherBuilder {
	
	public static final String KEY = "key";
	
	public static final String SHOW_REASON = "showReason";
	
	public static final String BYPASS_METRIC = "bypassMetric";
	
	private AsyncSwitcher asyncSwitcher;
	
	private SwitcherExecutor context;
	
	private String switcherKey;
	
	private Set<CriteriaResponse> historyExecution;
	
	private boolean bypassMetrics = Boolean.FALSE;
	
	private boolean showReason = Boolean.FALSE;
	
	/**
	 * Use {@link SwitcherContext#getSwitcher(String)} to create this object.
	 * 
	 * @param switcherKey name of the key created
	 * @param context configuration object containing all information to start using switchers
	 */
	public Switcher(final String switcherKey, final SwitcherExecutor context) {
		this.switcherKey = switcherKey;
		this.context = context;
		this.historyExecution = new HashSet<>();
	}
	
	private boolean canUseAsync() {
		return super.delay > 0 && !this.historyExecution.isEmpty();
	}
	
	private CriteriaResponse getFromHistory() {
		return this.historyExecution.stream()
				.filter(c -> c.getEntry().equals(getEntry()))
				.findFirst()
				.orElse(null);
	}
	
	@Override
	public Switcher prepareEntry(final List<Entry> entry) {
		this.entry = entry;
		return this;
	}
	
	@Override
	public Switcher prepareEntry(final Entry entry, final boolean add) {
		if (this.entry == null)
			this.entry = new ArrayList<>();
		
		if (!add) 
			this.entry.clear();
		
		if (!this.entry.contains(entry))
			this.entry.add(entry);
		
		return this;
	}
	
	@Override
	public Switcher prepareEntry(final Entry entry) {
		return this.prepareEntry(entry, false);
	}
	
	@Override
	public boolean isItOn(final Entry entry, final boolean add) 
			throws SwitcherException {
		this.prepareEntry(entry, add);
		return this.isItOn();
	}
	
	@Override
	public boolean isItOn(final List<Entry> entry) throws SwitcherException {
		this.entry = entry;
		return this.isItOn();
	}
	
	@Override
	public boolean isItOn() throws SwitcherException {
		if (SwitcherExecutor.getBypass().containsKey(switcherKey)) {
			return SwitcherExecutor.getBypass().get(switcherKey);
		}
		
		if (canUseAsync()) {
			if (asyncSwitcher == null)
				asyncSwitcher = new AsyncSwitcher();
			
			asyncSwitcher.execute(this);
			final CriteriaResponse response = getFromHistory();
			if (response != null)
				return response.isItOn();
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
		return new GsonInputRequest(
				this.entry != null ? 
						this.entry.toArray(new Entry[this.entry.size()]) : null);
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
	
	public void resetEntry() {
		this.entry = new ArrayList<Entry>();
	}

	public Set<CriteriaResponse> getHistoryExecution() {
		return this.historyExecution;
	}
	
	public SwitcherExecutor getContext() {
		return context;
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

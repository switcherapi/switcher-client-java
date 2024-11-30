package com.github.switcherapi.client.model;

import com.github.switcherapi.client.SwitcherContext;
import com.github.switcherapi.client.SwitcherProperties;
import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.service.SwitcherExecutionService;
import com.github.switcherapi.client.test.SwitcherBypass;

import java.util.*;

/**
 * SwitcherRequest are the entry point to evaluate criteria and return the result.
 * <br>To execute a criteria evaluation, use one of the available methods: {@link #isItOn()}.
 * 
 * @author Roger Floriano (petruki)
 * @since 2019-12-24
 * 
 * @see #isItOn()
 * @see #submit()
 */
public final class SwitcherRequest extends SwitcherBuilder {
	
	public static final String KEY = "key";
	public static final String SHOW_REASON = "showReason";
	public static final String BYPASS_METRIC = "bypassMetric";

	private final SwitcherExecutionService switcherExecutor;
	private final String switcherKey;
	private final Set<SwitcherResult> historyExecution;
	private AsyncSwitcher asyncSwitcher;
	
	/**
	 * Use {@link SwitcherContext#getSwitcher(String)} to create this object.
	 * 
	 * @param switcherKey name of the key created
	 * @param switcherExecutor client context in which the switcher will be executed (local/remote)
	 * @param switcherProperties properties to be used with executor operations
	 */
	public SwitcherRequest(final String switcherKey,
						   final SwitcherExecutionService switcherExecutor,
						   final SwitcherProperties switcherProperties) {
		super(switcherProperties);
		this.switcherExecutor = switcherExecutor;
		this.switcherKey = switcherKey;
		this.historyExecution = new HashSet<>();
		this.entry = new ArrayList<>();
	}

	@Override
	public SwitcherRequest build() {
		return this;
	}
	
	@Override
	public SwitcherRequest prepareEntry(final List<Entry> entry) {
		this.entry = Optional.ofNullable(entry).orElse(new ArrayList<>());
		return this;
	}
	
	@Override
	public SwitcherRequest prepareEntry(final Entry entry, final boolean add) {
		if (!add) {
			this.entry.clear();
		}

		if (!this.entry.contains(entry)) {
			this.entry.add(entry);
		}
		
		return this;
	}
	
	@Override
	public SwitcherRequest prepareEntry(final Entry entry) {
		return this.prepareEntry(entry, false);
	}
	
	@Override
	public boolean isItOn() throws SwitcherException {
		final SwitcherResult response = submit();
		return response.isItOn();
	}

	@Override
	public SwitcherResult submit() throws SwitcherException {
		if (SwitcherBypass.getBypass().containsKey(switcherKey)) {
			return SwitcherBypass.getBypass().get(switcherKey).buildFromSwitcher(switcherKey, entry);
		}

		if (canUseAsync()) {
			if (Objects.isNull(asyncSwitcher)) {
				asyncSwitcher = new AsyncSwitcher(this, super.delay);
			}

			asyncSwitcher.execute();
			final Optional<SwitcherResult> response = getFromHistory();
			if (response.isPresent()) {
				return response.get();
			}
		}

		final SwitcherResult response = this.switcherExecutor.executeCriteria(this);
		this.updateHistoryExecution(response);
		return response;
	}

	@Override
	public SwitcherResult executeCriteria() {
		return this.switcherExecutor.executeCriteria(this);
	}

	@Override
	public void updateHistoryExecution(final SwitcherResult response) {
		this.historyExecution.removeIf(item ->
				this.switcherKey.equals(item.getSwitcherKey()) && this.entry.equals(item.getEntry()));

		this.historyExecution.add(response);
	}

	@Override
	public String getSwitcherKey() {
		return this.switcherKey;
	}

	@Override
	public List<Entry> getEntry() {
		return this.entry;
	}

	public boolean isBypassMetrics() {
		return bypassMetrics;
	}
	
	public void resetEntry() {
		this.entry = new ArrayList<>();
	}

	private boolean canUseAsync() {
		return super.delay > 0 && !this.historyExecution.isEmpty();
	}

	private Optional<SwitcherResult> getFromHistory() {
		for (SwitcherResult switcherResult : historyExecution) {
			if (switcherResult.getEntry().equals(getEntry())) {
				return Optional.of(switcherResult);
			}
		}
		return Optional.empty();
	}

	@Override
	public String toString() {
		return String.format("SwitcherRequest [switcherKey= %s, entry= %s, bypassMetrics= %s]",
				switcherKey, entry, bypassMetrics);
	}

}

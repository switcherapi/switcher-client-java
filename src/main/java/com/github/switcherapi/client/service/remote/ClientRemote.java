package com.github.switcherapi.client.service.remote;

import com.github.switcherapi.client.exception.SwitcherException;
import com.github.switcherapi.client.model.Switcher;
import com.github.switcherapi.client.model.criteria.Snapshot;
import com.github.switcherapi.client.model.criteria.SwitchersCheck;
import com.github.switcherapi.client.model.response.CriteriaResponse;

import java.util.Set;

/**
 * @author Roger Floriano (petruki)
 * @since 2023-07-12
 */
public interface ClientRemote {

    CriteriaResponse executeCriteria(final Switcher switcher);

    Snapshot resolveSnapshot() throws SwitcherException;

    boolean checkSnapshotVersion(final long version);

    SwitchersCheck checkSwitchers(final Set<String> switchers);
}

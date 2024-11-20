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

    /**
     * Execute the criteria validation based on the configuration tree. It starts
     * validating from the top of the node (Domain) ascending to the lower level
     * (Strategy)
     *
     * @param switcher Configuration switcher to be validated
     * @return The criteria result
     * @throws SwitcherException If encountered either invalid input or misconfiguration
     */
    CriteriaResponse executeCriteria(final Switcher switcher);

    /**
     * Resolve the snapshot from the remote server
     *
     * @return The snapshot
     * @throws SwitcherException If encountered either invalid input or misconfiguration
     */
    Snapshot resolveSnapshot() throws SwitcherException;

    /**
     * Check if the snapshot version is the same as the one in the remote server
     *
     * @param version The version to be checked
     * @return True if the version is the same
     */
    boolean checkSnapshotVersion(final long version);

    /**
     * Check if the switchers are valid and if they are properly annotated with @SwitcherKey
     *
     * @param switchers List of switchers to be checked
     * @return List of invalid switchers
     */
    SwitchersCheck checkSwitchers(final Set<String> switchers);
}

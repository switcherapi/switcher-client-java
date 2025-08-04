package com.switcherapi.client.service.remote;

import com.switcherapi.client.exception.SwitcherException;
import com.switcherapi.client.model.criteria.Snapshot;
import com.switcherapi.client.remote.dto.SwitchersCheck;
import com.switcherapi.client.remote.dto.CriteriaRequest;
import com.switcherapi.client.remote.dto.CriteriaResponse;

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
     * @param criteriaRequest Criteria request
     * @return The criteria result
     * @throws SwitcherException If encountered either invalid input or misconfiguration
     */
    CriteriaResponse executeCriteria(final CriteriaRequest criteriaRequest);

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

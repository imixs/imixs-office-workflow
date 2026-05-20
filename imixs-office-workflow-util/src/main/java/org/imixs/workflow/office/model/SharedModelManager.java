/*******************************************************************************
 *  Imixs Workflow
 *  Copyright (C) 2001, 2011 Imixs Software Solutions GmbH,
 *  http://www.imixs.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  as published by the Free Software Foundation; either version 2
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *
 *  Project:
 *  	http://www.imixs.org
 *  	http://java.net/projects/imixs-workflow
 *
 *  Contributors:
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika - Software Developer
 *******************************************************************************/

package org.imixs.workflow.office.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.imixs.workflow.ModelManager;
import org.imixs.workflow.engine.WorkflowService;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.inject.Inject;

/**
 * The SharedModelManager is a application-scoped singleton EJB that holds a
 * shared instance of the {@link ModelManager} POJO for use in the UI layer
 * (e.g. ModelController). Used instead of creating a local ModelManager
 * instance.
 * <p>
 * In contrast to the ModelManager instances used by the WorkflowKernel during
 * processing - which are always session or request-local - this shared instance
 * is intentionally global and serves as a cache for model meta data in the
 * frontend.
 * <p>
 * The internal ModelManager can be reset at any time (e.g. after a model upload
 * or deletion) without affecting any ongoing workflow processing, since the
 * WorkflowKernel always holds its own local ModelManager instance during a
 * transaction.
 *
 * @see ModelController
 * @see ModelManager
 * @author rsoika
 */
@Singleton
public class SharedModelManager {

    private static final Logger logger = Logger.getLogger(SharedModelManager.class.getName());

    @Inject
    WorkflowService workflowService;

    // The shared ModelManager instance - volatile to ensure visibility across
    // threads
    private volatile ModelManager modelManager;

    // Collect model warnings for frontend display
    private final Set<String> modelWarnings = Collections.synchronizedSet(new LinkedHashSet<>());

    /**
     * Initializes the shared ModelManager instance on startup.
     */
    @PostConstruct
    public void init() {
        modelManager = new ModelManager(workflowService);
        logger.info("├── SharedModelManager initialized.");
    }

    /**
     * Returns the shared ModelManager instance.
     * <p>
     * Note: this instance is shared across all active user sessions. It is suitable
     * for read-only UI operations (e.g. resolving model groups, start tasks,
     * process descriptions). It must not be used inside workflow processing
     * transactions.
     *
     * @return the shared {@link ModelManager} instance
     */
    public ModelManager getModelManager() {
        return modelManager;
    }

    /**
     * Resets the shared ModelManager by creating a new instance.
     * <p>
     * This clears all internal caches (modelStore, bpmnEntityCache,
     * bpmnElementCache, groupCache) and forces a fresh reload of model data on the
     * next access.
     * <p>
     * This method should be called after a model upload or deletion to ensure all
     * active user sessions see the updated model data immediately.
     * <p>
     * Ongoing workflow processing is not affected since the WorkflowKernel holds
     * its own local ModelManager instance.
     */
    public synchronized void reset() {
        modelManager = new ModelManager(workflowService);
        modelWarnings.clear();
        logger.info("├── SharedModelManager reset - all model caches cleared.");
    }

    public void addModelWarning(String message) {
        modelWarnings.add(message);
    }

    public Set<String> getModelWarnings() {
        return modelWarnings;
    }
}
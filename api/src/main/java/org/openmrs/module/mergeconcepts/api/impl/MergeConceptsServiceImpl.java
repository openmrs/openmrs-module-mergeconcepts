/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.mergeconcepts.api.impl;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.mergeconcepts.api.MergeConceptsService;
import org.openmrs.module.mergeconcepts.api.db.MergeConceptsDAO;
import org.springframework.transaction.annotation.Transactional;

/**
 * It is a default implementation of {@link MergeConceptsService}.
 */
public class MergeConceptsServiceImpl extends BaseOpenmrsService implements MergeConceptsService {
	
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private MergeConceptsDAO dao;
	
	/**
     * @param dao the dao to set
     */
    public void setDao(MergeConceptsDAO dao) {
	    this.dao = dao;
    }
    
    /**
     * @return the dao
     */
    public MergeConceptsDAO getDao() {
	    return dao;
    }
    
    public int getObsCount(Integer conceptId){
    	int obsCount = dao.getObsCount(conceptId);
    	return obsCount;
    }
    
    public List<Integer> getObsIds(Integer conceptId){
    	List<Integer> obsIds = dao.getObsIds(conceptId);
    	return obsIds;
    	
    }
    
    @Transactional
    public void updateObs(Integer oldConceptId, Integer newConceptId){
    	dao.updateObs(oldConceptId, newConceptId);
    }

	@Override
	public List<Program> getProgramsByConcept(Concept concept) {
		List<Program> programs = dao.getProgramsByConcept(concept);
		return programs;
	}

	@Override
	public List<ProgramWorkflow> getProgramWorkflowsByConcept(Concept concept) {
		List<ProgramWorkflow> programWorkFlows = dao.getProgramWorkflowsByConcept(concept);
		return programWorkFlows;
	}

	@Override
	public List<ProgramWorkflowState> getProgramWorkflowStatesByConcept(
			Concept concept) {
		List<ProgramWorkflowState> programWorkflowStates = dao.getProgramWorkflowStatesByConcept(concept);
		return programWorkflowStates;
	}
    
}
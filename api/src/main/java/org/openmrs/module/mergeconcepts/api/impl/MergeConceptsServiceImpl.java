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
import java.util.Set;

import org.openmrs.*;
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
    	return dao.getObsCount(conceptId);
    }
    
    public List<Integer> getObsIds(Integer conceptId){
    	return dao.getObsIdsWithQuestionConcept(conceptId);
    }
    
    @Transactional
    public void updateObs(Integer oldConceptId, Integer newConceptId){
    	dao.updateObs(oldConceptId, newConceptId);
    }

    @Override
    public void updateFields(int oldConceptId, int newConceptId) {
        dao.updateFields(oldConceptId, newConceptId);
    }

    @Override
	public List<Drug> getDrugsByIngredient(Concept ingredient) {
        return dao.getDrugsByIngredient(ingredient);
	}

    @Override
    public void updateOrders(int oldConceptId, int newConceptId) {
        dao.updateOrders(oldConceptId,newConceptId);
    }

    @Override
    public List<Order> getMatchingOrders(Concept concept) {
        return dao.getMatchingOrders(concept);
    }

    @Override
    public void updatePrograms(int oldConceptId, int newConceptId) {
        dao.updatePrograms(oldConceptId, newConceptId);
    }

    @Override
    public List<Program> getMatchingPrograms(Concept concept) {
        return dao.getProgramsByConcept(concept);
    }

    @Override
    public Set<FormField> getMatchingFormFields(Concept concept) {
        return dao.getMatchingFormFields(concept);
    }

    @Override
    public Set<Form> getMatchingForms(Concept concept) {
        return dao.getMatchingForms(concept);
    }

    @Override
    public List<Drug> getDrugsByRouteConcept(Concept concept) {
        return dao.getDrugsByRouteConcept(concept);
    }

    @Override
    public List<Drug> getDrugsByDosageFormConcept(Concept concept) {
        return dao.getDrugsByDosageFormConcept(concept);
    }
}
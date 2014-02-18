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
package org.openmrs.module.mergeconcepts.api.db.hibernate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.openmrs.*;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mergeconcepts.api.db.MergeConceptsDAO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * It is a default implementation of  {@link MergeConceptsDAO}.
 */
public class HibernateMergeConceptsDAO implements MergeConceptsDAO {
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private SessionFactory sessionFactory;
	
	/**
     * @param sessionFactory the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
	    this.sessionFactory = sessionFactory;
    }
    
	/**
     * @return the sessionFactory
     */
    public SessionFactory getSessionFactory() {
	    return sessionFactory;
    }
    
    /**
     * 
     * @param conceptId
     * @return
     * @should return a count of the obs
     */
    @Transactional
    public Integer getObsCount(Integer conceptId){
    	Long obsCount = null;
    	Long obsCount2 = null;
    	
    	Query query = sessionFactory.getCurrentSession().createQuery("select count(*) from Obs where concept_id = :conceptId and voided = 0")
		        .setParameter("conceptId", conceptId);
    	obsCount = (Long)query.uniqueResult();
    	
    	Query query2 = sessionFactory.getCurrentSession().createQuery("select count(*) from Obs where value_coded = :conceptId and voided = 0")
		        .setParameter("conceptId", conceptId);
    	obsCount2 = (Long)query2.uniqueResult();
    	
    	return obsCount.intValue() + obsCount2.intValue();
    	
    	/**
    	 * example:
    	 * String select = "select count(*) from Patient";
    	 * Query query = sessionFactory.getCurrentSession().createQuery(select);
    	 * return ((Number) query.iterate().next()).intValue();
    	 */
    }
    
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getProgramsByConcept(org.openmrs.Concept)
	 */
	@Override
	public List<Program> getProgramsByConcept(Concept concept) {
		
		String pq = "select distinct p from Program p where p.concept = :concept";
		Query pquery = sessionFactory.getCurrentSession().createQuery(pq);
		pquery.setEntity("concept", concept);
		
		List<Program> matchingPrograms = pquery.list();
		
		return matchingPrograms;
		
	}
	
	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getProgramWorkflowsByConcept(org.openmrs.Concept)
	 */
	@Override
	public List<ProgramWorkflow> getProgramWorkflowsByConcept(Concept concept) {
		
		String wq = "select distinct w from ProgramWorkflow w where w.concept = :concept";
		Query wquery = sessionFactory.getCurrentSession().createQuery(wq);
		wquery.setEntity("concept", concept);
		
		return wquery.list();
	
	}

	/**
	 * @see org.openmrs.api.db.ProgramWorkflowDAO#getProgramWorkflowStatesByConcept(org.openmrs.Concept)
	 */
	@Override
	public List<ProgramWorkflowState> getProgramWorkflowStatesByConcept(Concept concept) {
	
		String sq = "select distinct s from ProgramWorkflowState s where s.concept = :concept";
		Query squery = sessionFactory.getCurrentSession().createQuery(sq);
		squery.setEntity("concept", concept);
		
		return squery.list();
	
	}

	/**
	 * @see org.openmrs.api.db.ConceptDAO#getDrugsByIngredient(org.openmrs.Concept)
	 */
	@SuppressWarnings("unchecked")
	public List<Drug> getDrugsByIngredient(Concept ingredient) {
		Criteria searchDrugCriteria = sessionFactory.getCurrentSession().createCriteria(Drug.class, "drug");
		Criterion rhs = Restrictions.eq("drug.concept", ingredient);
		searchDrugCriteria.createAlias("ingredients", "ingredients");
		Criterion lhs = Restrictions.eq("ingredients.ingredient", ingredient);
		searchDrugCriteria.add(Restrictions.or(lhs, rhs));

		return (List<Drug>) searchDrugCriteria.list();
	}

    /**
     * 
     * @param conceptId
     * @return
     * @should return a list of the obs
     */
    @Transactional
    public List<Integer> getObsIdsWithQuestionConcept(Integer conceptId) throws APIException {
    	List<Integer> obsIds = null;
    	
    	Query query = sessionFactory.getCurrentSession().createQuery("select obs.obsId from Obs obs where concept_id = :conceptId and voided = 0")
		        .setParameter("conceptId", conceptId);
    	
    	obsIds = (List<Integer>) query.list();
    	
    	return obsIds;
    }
    
    /**
     * for concepts used as an answer
     * @param conceptId
     * @return
     * @should return a list of the obs
     */
    @Transactional
    public List<Integer> getObsIdsWithAnswerConcept(Integer conceptId) throws APIException {
    	List<Integer> obsIds = null; //for obs in value_coded column
    	
    	Query query = sessionFactory.getCurrentSession().createQuery("select obs.obsId from Obs obs where value_coded = :conceptId and voided = 0")
		        .setParameter("conceptId", conceptId);
    	
    	obsIds = (List<Integer>) query.list();
    	
    	return obsIds;
    	
    }

    /**
     * 
     * @param conceptId
     * @return
     */
    @Transactional
    public void updateObs(Integer oldConceptId, Integer newConceptId){
    	
    	ConceptService conceptService = Context.getConceptService();
		
		Concept oldConcept = conceptService.getConcept(oldConceptId); 
		Concept newConcept = conceptService.getConcept(newConceptId);

		String msg = "Converted concept references from " + oldConcept + " to " + newConcept;

		List<Integer> QuestionObsToConvert = this.getObsIdsWithQuestionConcept(oldConceptId);
		List<Integer> AnswerObsToConvert = this.getObsIdsWithAnswerConcept(oldConceptId);
		
		ObsService obsService = Context.getObsService();
		
		for(Integer obsId: QuestionObsToConvert){
			Obs o = obsService.getObs(obsId);
			o.setConcept(newConcept);
			obsService.saveObs(o, msg);
		}
		
		for(Integer obsId: AnswerObsToConvert){
			Obs o = obsService.getObs(obsId);
			o.setValueCoded(newConcept);
			obsService.saveObs(o, msg);
		}
    }
}
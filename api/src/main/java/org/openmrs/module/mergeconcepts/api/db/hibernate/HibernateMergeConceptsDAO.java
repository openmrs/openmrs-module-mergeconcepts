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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.hibernate.JDBCException;
import org.hibernate.Query;
import org.hibernate.Session;
//import org.hibernate.jdbc.Work; cannot be resolved
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mergeconcepts.api.db.MergeConceptsDAO;
import org.springframework.transaction.annotation.Transactional;

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
     * 
     * @param conceptId
     * @return
     * @should return a count of the obs
     
    @Transactional
    public Integer getAnswerObsCount(Integer conceptId){
    	Long obsCount = null;

    	Query query = sessionFactory.getCurrentSession().createQuery("select count(*) from Obs where concept_id = :conceptId and voided = 0")
		        .setParameter("conceptId", conceptId);
    	obsCount = (Long) query.uniqueResult();
    	/**
    	 * String select = "select count(*) from Patient";
    	 * Query query = sessionFactory.getCurrentSession().createQuery(select);
    	 * return ((Number) query.iterate().next()).intValue();
    	 
    	return obsCount.intValue();
    }*/
    
    
    /**
     * 
     * @param conceptId
     * @return
     * @should return a list of the obs
     */
    @Transactional
    public List<Integer> getObsIds(Integer conceptId) throws APIException {
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
    public List<Integer> getObsIds2(Integer conceptId) throws APIException {   
    	List<Integer> obsIds2 = null; //for obs in value_coded column
    	
    	Query query2 = sessionFactory.getCurrentSession().createQuery("select obs.obsId from Obs obs where value_coded = :conceptId and voided = 0")
		        .setParameter("conceptId", conceptId);
    	
    	obsIds2 = (List<Integer>) query2.list();
    	
    	return obsIds2;
    	
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

		//Query query = sessionFactory.getCurrentSession().createQuery("select obs_id from Obs where concept_id = :conceptId")
		  //      .setParameter("conceptId", oldConceptId);
		
		List<Integer> QuestionObsToConvert = this.getObsIds(oldConceptId);
		List<Integer> AnswerObsToConvert = this.getObsIds2(oldConceptId);
		
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


/**
 * 
 * 
 * //sessionFactory.getCurrentSession().doWork( new Work() { void execute(Connection connection) { } } );
 * 
 * else{
    		List<Integer> obsIds = new ArrayList<Integer>();//mysql query to make List obsIds
    		
    		ObsService obsService = Context.getObsService();
    		//String msg = "Converted question concept from " + oldConcept + " to " + newConcept; <-- use SessionFactory
    		
    		int i = 0;
    		for(Integer obsId : obsIds){
    			//update obs
    			Obs o = obsService.getObs(obsId);
    			//o.setConcept(newConcept);
    			//obsService.saveObs(o, msg);
    			
    			i++;
    			if(i==200){
    				Context.flushSession();
    				Context.clearSession();
    				i = 0;
    			}
    		}
    	}
    	
    	
    	
    	
    	
    	
 */

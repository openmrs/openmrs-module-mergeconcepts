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
import org.openmrs.api.*;
import org.openmrs.api.context.Context;
import org.openmrs.module.mergeconcepts.api.MergeConceptsService;
import org.openmrs.module.mergeconcepts.api.db.MergeConceptsDAO;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    	Long obsWithQuestionConceptCount = null;
    	Long obsWithAnswerConceptCount = null;
    	
    	Query query = sessionFactory.getCurrentSession().createQuery("select count(*) from Obs where concept_id = :conceptId and voided = 0")
		        .setParameter("conceptId", conceptId);
    	obsWithQuestionConceptCount = (Long)query.uniqueResult();
    	
    	Query query2 = sessionFactory.getCurrentSession().createQuery("select count(*) from Obs where value_coded = :conceptId and voided = 0")
		        .setParameter("conceptId", conceptId);
    	obsWithAnswerConceptCount = (Long)query2.uniqueResult();
    	
    	return obsWithQuestionConceptCount.intValue() + obsWithAnswerConceptCount.intValue();
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

    public List<Drug> getDrugsByRouteConcept(Concept route) {
        Criteria searchDrugCriteria = sessionFactory.getCurrentSession().createCriteria(Drug.class, "drug");
        Criterion rhs = Restrictions.eq("drug.route", route);
        searchDrugCriteria.add(rhs);

        return (List<Drug>) searchDrugCriteria.list();
    }

    /**
     * @param concept
     * @return
     */
    public List<Order> getMatchingOrders(Concept concept) {
        List<Concept> conceptList = new ArrayList<Concept>();
        conceptList.add(concept);
        OrderService orderService = Context.getOrderService();
        List<Order> ordersToUpdate = orderService.getOrders(Order.class, null, conceptList, null, null, null, null);
        return ordersToUpdate;
    }

    @Override
    public void updateOrders(int oldConceptId, int newConceptId) {
        Concept oldConcept = Context.getConceptService().getConcept(oldConceptId);
        Concept newConcept = Context.getConceptService().getConcept(newConceptId);
        List<Order> ordersToUpdate = this.getMatchingOrders(oldConcept);
        for (Order o : ordersToUpdate) {
            o.setConcept(newConcept);
        }
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
     * @param oldConceptId
     * @param newConceptId
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


    /**
     * @param oldConceptId
     * @param newConceptId
     */
    public void updatePrograms(int oldConceptId, int newConceptId) {
        Concept oldConcept = Context.getConceptService().getConcept(oldConceptId);
        Concept newConcept = Context.getConceptService().getConcept(newConceptId);
        List<Program> programsToUpdate = this.getProgramsByConcept(oldConcept);
        List<ProgramWorkflow> programWorkflowsToUpdate = this.getProgramWorkflowsByConcept(oldConcept);
        List<ProgramWorkflowState> programWorkflowStatesToUpdate = this.getProgramWorkflowStatesByConcept(oldConcept);

        for (Program p : programsToUpdate) {
            p.setConcept(newConcept);
            p.setName(newConcept.getName().toString());
            p.setDescription(newConcept.getDescription().toString());
        }

        for (ProgramWorkflow pw : programWorkflowsToUpdate) {
            pw.setConcept(newConcept);
        }

        for (ProgramWorkflowState pws : programWorkflowStatesToUpdate) {
            pws.setConcept(newConcept);
        }
    }

    @Override
    public void updateFields(int oldConceptId, int newConceptId) {
        Concept oldConcept = Context.getConceptService().getConcept(oldConceptId);
        Concept newConcept = Context.getConceptService().getConcept(newConceptId);

        String newConceptName = newConcept.getName().toString();

        Set<FormField> formFieldsToUpdate = this.getMatchingFormFields(oldConcept);
        FormService formService = Context.getFormService();

        for (FormField formField : formFieldsToUpdate) {
            Field field = formField.getField();
            field.setConcept(newConcept);
            field.setName(newConceptName);

            ConceptDescription newDescription = newConcept.getDescription();

            if (newDescription != null) {
                field.setDescription(newDescription.toString());
            }

            formService.saveField(field);
            formField.setField(field);
            formService.saveFormField(formField);
            formService.saveForm(formField.getForm());
        }
    }

    @Override
    public Set<FormField> getMatchingFormFields(Concept concept) {
        Set<FormField> formFields = new HashSet<FormField>();
        List<Form> allForms = Context.getFormService().getFormsContainingConcept(concept); //instead of Context.getFormService().getAllForms();
        //FormFields with old concept (might be a better way to do this)
        for (Form f : allForms) {
            formFields.add(Context.getFormService().getFormField(f, concept));
        }
        return formFields;
    }

    @Override
    public Set<Form> getMatchingForms(Concept concept) {

        Set<FormField> formFields = Context.getService(MergeConceptsService.class).getMatchingFormFields(concept);
        Set<Form> conceptForms = new HashSet<Form>();

        for (FormField f : formFields) {

            //forms that ref oldConcept
            if (!f.getForm().equals(null)) {
                conceptForms.add(f.getForm());
            }

        }

        return conceptForms;
    }
}
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.mergeconcepts.api.MergeConceptsService;
import org.openmrs.module.mergeconcepts.api.db.MergeConceptsDAO;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * It is a default implementation of {@link MergeConceptsService}.
 */

public class MergeConceptsServiceImpl extends BaseOpenmrsService implements MergeConceptsService {

	protected final Log log = LogFactory.getLog(this.getClass());

	private MergeConceptsDAO dao;

    @Override
    public void update(Integer oldConceptId, Integer newConceptId, Concept oldConcept, Concept newConcept) {
        //OBS
        updateObs(oldConceptId, newConceptId);

        //FORMS
        updateFields(oldConceptId, newConceptId);

        //DRUGS
        updateDrugs(oldConcept, newConcept);

        //ORDERS
        updateOrders(oldConcept, newConcept);

        //PROGRAMS
        updatePrograms(oldConceptId, newConceptId);

        //CONCEPT SETS
        updateConceptSets(oldConcept, newConcept);

        //CONCEPT ANSWERS
        updateConceptAnswers(oldConcept, newConcept);

        //PERSON ATTRIBUTE TYPES
        updatePersonAttributeTypes(oldConcept, newConcept);
    }

    @Transactional
    public void updateObs(Integer oldConceptId, Integer newConceptId){
        dao.updateObs(oldConceptId, newConceptId);
    }

    @Override
    public void updateFields(int oldConceptId, int newConceptId) {
        dao.updateFields(oldConceptId, newConceptId);
    }

    public void updateDrugs(Concept oldConcept, Concept newConcept) {
        List<Drug> drugsToUpdate = getMatchingDrugsByConcept(oldConcept);
        List<Drug> drugsByRouteConcept = getDrugsByRouteConcept(oldConcept);

        setConceptsAndRoutes(newConcept, drugsToUpdate, drugsByRouteConcept);

    }

    public List<Drug> getMatchingDrugsByConcept(Concept concept) {
        ConceptService conceptService = Context.getConceptService();
        return conceptService.getDrugsByConcept(concept);
    }


    @Override
    public List<Drug> getDrugsByRouteConcept(Concept concept) {
        return dao.getDrugsByRouteConcept(concept);
    }

    @Override
    public void setConceptsAndRoutes(Concept newConcept, List<Drug> drugsToUpdate, List<Drug> drugsByRouteConcept) {
        if (drugsToUpdate != null) {
            for (Drug d : drugsToUpdate) {
                d.setConcept(newConcept);
            }
        }


        if (drugsByRouteConcept != null) {
            for (Drug d : drugsByRouteConcept) {
                d.setRoute(newConcept);
            }
        }
    }

    @Override
    public void updatePrograms(int oldConceptId, int newConceptId) {
        dao.updatePrograms(oldConceptId, newConceptId);
    }

    public void updateConceptSets(Concept oldConcept, Concept newConcept) {
        //update concept_id
        List<ConceptSet> conceptSetConceptsToUpdate = getMatchingConceptSetConcepts(oldConcept);
        if (conceptSetConceptsToUpdate != null) {
            for (ConceptSet csc : conceptSetConceptsToUpdate) {
                csc.setConcept(newConcept);
            }
        }

        //concept_set
        List<ConceptSet> conceptSetsToUpdate = getMatchingConceptSets(oldConcept);
        if (conceptSetConceptsToUpdate != null) {
            for (ConceptSet cs : conceptSetsToUpdate) {
                cs.setConceptSet(newConcept);
            }
        }
    }

    public List<ConceptSet> getMatchingConceptSetConcepts(Concept concept) {
        ConceptService conceptService = Context.getConceptService();
        return conceptService.getSetsContainingConcept(concept);
    }

    public List<ConceptSet> getMatchingConceptSets(Concept concept) {
        ConceptService conceptService = Context.getConceptService();
        return conceptService.getConceptSetsByConcept(concept);
    }

    /**
     * ConceptAnswers contain references to concepts
     *  @param oldConcept
     * @param newConcept
     */
    public void updateConceptAnswers(Concept oldConcept, Concept newConcept) {
        List<ConceptAnswer> conceptAnswerQuestionsToUpdate = getMatchingConceptAnswerQuestions(oldConcept);

        //update concept_id
        for (ConceptAnswer caq : conceptAnswerQuestionsToUpdate) {
            caq.setConcept(newConcept);
        }

        List<ConceptAnswer> conceptAnswerAnswersToUpdate = getMatchingConceptAnswerAnswers(oldConcept);

        //update answer_concepts
        for (ConceptAnswer caa : conceptAnswerAnswersToUpdate) {
            caa.setAnswerConcept(newConcept);
        }
    }

    public List<ConceptAnswer> getMatchingConceptAnswerQuestions(Concept concept) {
        List<ConceptAnswer> matchingConceptAnswers = new ArrayList<ConceptAnswer>();
        for (ConceptAnswer ca : concept.getAnswers()) {
            matchingConceptAnswers.add(ca);
        }
        return matchingConceptAnswers;
    }

    public List<ConceptAnswer> getMatchingConceptAnswerAnswers(Concept concept) {
        ConceptService conceptService = Context.getConceptService();
        List<ConceptAnswer> matchingConceptAnswers = new ArrayList<ConceptAnswer>();

        //Concepts that are questions answered by this concept, and possibly others
        for (Concept c : conceptService.getConceptsByAnswer(concept)) {
            //ConceptAnswers of all possible answers to question concept above
            for (ConceptAnswer a : c.getAnswers()) {

                //only add ConceptAnswers with an answer matching this concept
                if (a.getAnswerConcept().equals(concept)) {
                    matchingConceptAnswers.add(a);
                }
            }
        }

        return matchingConceptAnswers;
    }

    public void updatePersonAttributeTypes(Concept oldConcept, Concept newConcept) {
        List<PersonAttributeType> matchingPersonAttributeTypes = getMatchingPersonAttributeTypes(oldConcept);

        for (PersonAttributeType m : matchingPersonAttributeTypes) {
            m.setForeignKey(newConcept.getConceptId());
        }
    }

    public List<PersonAttributeType> getMatchingPersonAttributeTypes(Concept concept) {
        PersonService personService = Context.getPersonService();
        List<PersonAttributeType> allPersonAttributeTypes = personService.getAllPersonAttributeTypes();
        List<PersonAttributeType> matchingPersonAttributeTypes = new ArrayList<PersonAttributeType>();

        for (PersonAttributeType p : allPersonAttributeTypes) {
            if (p.getFormat().toLowerCase().contains("concept")) {
                if (p.getForeignKey() != null && p.getForeignKey().equals(concept.getConceptId())) {
                    matchingPersonAttributeTypes.add(p);
                }
            }
        }

        return matchingPersonAttributeTypes;
    }

    public List<ConceptAnswer> getMatchingConceptAnswers(Concept concept) {
        List<ConceptAnswer> conceptAnswers = getMatchingConceptAnswerAnswers(concept);
        for (ConceptAnswer c : getMatchingConceptAnswerQuestions(concept)) {
            conceptAnswers.add(c);
        }
        return conceptAnswers;
    }

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

    @Override
	public List<Drug> getDrugsByIngredient(Concept ingredient) {
        return dao.getDrugsByIngredient(ingredient);
	}

    @Override
    public void updateOrders(Concept oldConcept, Concept newConcept) {
        dao.updateOrders(oldConcept, newConcept);
    }

    @Override
    public List<Order> getMatchingOrders(Concept concept) {
        return dao.getMatchingOrders(concept);
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
}
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

import java.util.*;

/**
 * It is a default implementation of {@link MergeConceptsService}.
 */

public class MergeConceptsServiceImpl extends BaseOpenmrsService implements MergeConceptsService {

	protected final Log log = LogFactory.getLog(this.getClass());

	private MergeConceptsDAO dao;

    @Override
    public void update(Concept oldConcept, Concept newConcept) {
        //OBS
        updateObs(oldConcept, newConcept);

        //FORMS
        updateFields(oldConcept,newConcept);

        //DRUGS
        updateDrugs(oldConcept, newConcept);

        //ORDERS
        updateOrders(oldConcept, newConcept);

        //PROGRAMS
        updatePrograms(oldConcept, newConcept);

        //CONCEPT SETS
        updateConceptSets(oldConcept, newConcept);

        //CONCEPT ANSWERS
        updateConceptAnswers(oldConcept, newConcept);

        //PERSON ATTRIBUTE TYPES
        updatePersonAttributeTypes(oldConcept, newConcept);
    }

    @Transactional
    public void updateObs(Concept oldConcept, Concept newConcept){
        dao.updateObs(oldConcept, newConcept);
    }

    @Override
    public void updateFields(Concept oldConcept, Concept newConcept) {
        dao.updateFields(oldConcept, newConcept);
    }

    public void updateDrugs(Concept oldConcept, Concept newConcept) {
        List<Drug> drugsToUpdate = getMatchingDrugsByConcept(oldConcept);
        List<Drug> drugsByRouteConcept = getDrugsByRouteConcept(oldConcept);
        List<Drug> drugsByDosageFormConcept = getDrugsByDosageFormConcept(oldConcept);

        setRelatedConceptsForDrugs(newConcept, drugsToUpdate, drugsByRouteConcept, drugsByDosageFormConcept);

    }

    public List<Drug> getMatchingDrugsByConcept(Concept concept) {
        ConceptService conceptService = Context.getConceptService();
        return conceptService.getDrugsByConcept(concept);
    }

    @Override
    public void setRelatedConceptsForDrugs(Concept newConcept, List<Drug> drugsToUpdate, List<Drug> drugsByRouteConcept, List<Drug> drugsByDosageFormConcept) {
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

        if (drugsByDosageFormConcept != null) {
            for (Drug d : drugsByDosageFormConcept) {
                d.setDosageForm(newConcept);
            }
        }
    }

    @Override
    public void updatePrograms(Concept oldConcept, Concept newConcept) {
        dao.updatePrograms(oldConcept, newConcept);
    }

    public void updateConceptSets(Concept oldConcept, Concept newConcept) {
        updateConceptSetsByPuttingNewConceptWhereOldConceptWasChild(oldConcept, newConcept);

        updateConceptSetsByChangingTheChildrenOfOldConceptToHaveNewConceptAsParent(oldConcept, newConcept);
    }

    private void updateConceptSetsByPuttingNewConceptWhereOldConceptWasChild(Concept oldConcept, Concept newConcept) {
        ConceptService conceptService = Context.getConceptService();

        List<ConceptSet> conceptSetConceptsToUpdate = conceptService.getSetsContainingConcept(oldConcept);
        if (conceptSetConceptsToUpdate != null) {
            for (ConceptSet conceptSet : conceptSetConceptsToUpdate) {
                conceptSet.setConcept(newConcept);
            }
        }
    }

    private void updateConceptSetsByChangingTheChildrenOfOldConceptToHaveNewConceptAsParent(Concept oldConcept, Concept newConcept) {
        ConceptService conceptService = Context.getConceptService();

        List<ConceptSet> conceptSetsToUpdate = conceptService.getConceptSetsByConcept(oldConcept);
        if (conceptSetsToUpdate != null) {
            for (ConceptSet conceptSet : conceptSetsToUpdate) {
                conceptSet.setConceptSet(newConcept);
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

    @Override
    public List<Drug> getDrugsByRouteConcept(Concept concept) {
        return dao.getDrugsByRouteConcept(concept);
    }

    @Override
    public List<Drug> getDrugsByDosageFormConcept(Concept concept) {
        return dao.getDrugsByDosageFormConcept(concept);
    }

    @Override
    public Map<String, Object> getAttributes(String conceptType, Concept concept) {
        Map<String, Object> attributes = new HashMap<String, Object>();

        attributes.put(conceptType + "ConceptId", concept.getId());

        List<String> drugNames = new ArrayList<String>();
        addDrugNames(concept, drugNames);
        attributes.put(conceptType + "Drugs", drugNames);

        //preview concept answers by id
        List<Integer> conceptAnswerIds = new ArrayList<Integer>();

        int obsCount = getObsCount(concept.getId());
        attributes.put(conceptType + "ObsCount", obsCount);

        if (getMatchingConceptAnswers(concept) != null) {
            for (ConceptAnswer a : getMatchingConceptAnswers(concept)) {
                conceptAnswerIds.add(a.getConceptAnswerId());
            }
        }

        List<Integer> conceptSetIds = new ArrayList<Integer>();
        if (getMatchingConceptSets(concept) != null) {
            for (ConceptSet c : getMatchingConceptSets(concept)) {
                conceptSetIds.add(c.getConceptSetId());
            }
            if (getMatchingConceptSetConcepts(concept) != null) {
                for (ConceptSet cs : getMatchingConceptSetConcepts(concept)) {
                    conceptSetIds.add(cs.getConceptSetId());
                }
            }
        }

        attributes.put(conceptType + "ConceptSets", conceptSetIds);
        attributes.put(conceptType + "ConceptAnswers", conceptAnswerIds);

        if (getMatchingForms(concept) != null) {
            attributes.put(conceptType + "Forms", getMatchingForms(concept));
        }

        if (getMatchingOrders(concept) != null){
            attributes.put(conceptType + "Orders", getMatchingOrders(concept));
        }

        if (getMatchingPrograms(concept) != null){
            attributes.put(conceptType + "Programs", getMatchingPrograms(concept));
        }

        if (getMatchingPersonAttributeTypes(concept) != null) {
            attributes.put(conceptType + "PersonAttributeTypes", getMatchingPersonAttributeTypes(concept));
        }


        return attributes;
    }

    @Override
    public void addDrugNames(Concept concept, List<String> drugNames) {
        if (getMatchingDrugsByConcept(concept) != null) {
            for (Drug od : getMatchingDrugsByConcept(concept)) {
                drugNames.add(od.getFullName(null));
            }
        }
    }
}
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
    @Override
    public void updateObs(Concept oldConcept, Concept newConcept){
        dao.updateObs(oldConcept, newConcept);
    }

    @Override
    public void updateFields(Concept oldConcept, Concept newConcept) {
        dao.updateFields(oldConcept, newConcept);
    }

    @Override
    public void updateDrugs(Concept oldConcept, Concept newConcept) {
        List<Drug> drugsToUpdate = getMatchingDrugsByConcept(oldConcept);
        List<Drug> drugsByRouteConcept = getDrugsByRouteConcept(oldConcept);
        List<Drug> drugsByDosageFormConcept = getDrugsByDosageFormConcept(oldConcept);

        setRelatedConceptsForDrugs(newConcept, drugsToUpdate, drugsByRouteConcept, drugsByDosageFormConcept);
    }

    @Override
    public void updateOrders(Concept oldConcept, Concept newConcept) {
        dao.updateOrders(oldConcept, newConcept);
    }

    @Override
    public void updatePrograms(Concept oldConcept, Concept newConcept) {
        dao.updatePrograms(oldConcept, newConcept);
    }

    @Override
    public void updateConceptSets(Concept oldConcept, Concept newConcept) {
        updateConceptSetsByPuttingNewConceptWhereOldConceptWasChild(oldConcept, newConcept);

        updateConceptSetsByChangingTheChildrenOfOldConceptToHaveNewConceptAsParent(oldConcept, newConcept);
    }

    @Override
    public void updateConceptAnswers(Concept oldConcept, Concept newConcept) {
        for (ConceptAnswer caq : getMatchingConceptAnswerQuestions(oldConcept)) {
            caq.setConcept(newConcept);
        }

        for (ConceptAnswer caa : getMatchingConceptAnswerAnswers(oldConcept)) {
            caa.setAnswerConcept(newConcept);
        }
    }

    @Override
    public void updatePersonAttributeTypes(Concept oldConcept, Concept newConcept) {
        for (PersonAttributeType m : getMatchingPersonAttributeTypes(oldConcept)) {
            m.setForeignKey(newConcept.getConceptId());
        }
    }

    @Override
    public void setRelatedConceptsForDrugs(Concept newConcept, List<Drug> drugsToUpdate, List<Drug> drugsByRouteConcept, List<Drug> drugsByDosageFormConcept) {
        for (Drug d : drugsToUpdate) {
            d.setConcept(newConcept);
        }
        for (Drug d : drugsByRouteConcept) {
            d.setRoute(newConcept);
        }
        for (Drug d : drugsByDosageFormConcept) {
            d.setDosageForm(newConcept);
        }
    }

    private void updateConceptSetsByPuttingNewConceptWhereOldConceptWasChild(Concept oldConcept, Concept newConcept) {
        for (ConceptSet conceptSet : getMatchingConceptSetConcepts(oldConcept)) {
            conceptSet.setConcept(newConcept);
        }
    }

    private void updateConceptSetsByChangingTheChildrenOfOldConceptToHaveNewConceptAsParent(Concept oldConcept, Concept newConcept) {
        for (ConceptSet conceptSet : getMatchingConceptSets(oldConcept)) {
            conceptSet.setConceptSet(newConcept);
        }
    }

    public void setDao(MergeConceptsDAO dao) {
	    this.dao = dao;
    }

    public MergeConceptsDAO getDao() {
	    return dao;
    }

    @Override
    public Map<String, Object> getAttributes(String conceptType, Concept concept) {
        Map<String, Object> attributes = new HashMap<String, Object>();

        attributes.put(conceptType + "ConceptId", concept.getId());
        attributes.put(conceptType + "ObsCount", getObsCount(concept.getId()));
        attributes.put(conceptType + "Forms", getMatchingForms(concept));
        attributes.put(conceptType + "Orders", getMatchingOrders(concept));
        attributes.put(conceptType + "Programs", getMatchingPrograms(concept));
        attributes.put(conceptType + "PersonAttributeTypes", getMatchingPersonAttributeTypes(concept));

        List<String> drugNames = new ArrayList<String>();
        addDrugNames(concept, drugNames);
        attributes.put(conceptType + "Drugs", drugNames);

        List<Integer> conceptAnswerIds = new ArrayList<Integer>();
        getConceptAnswersIds(concept, conceptAnswerIds);
        attributes.put(conceptType + "ConceptAnswers", conceptAnswerIds);

        List<Integer> conceptSetIds = new ArrayList<Integer>();
        getConceptSetIds(concept, conceptSetIds);
        attributes.put(conceptType + "ConceptSets", conceptSetIds);

        return attributes;
    }

    @Override
    public int getObsCount(Integer conceptId){
    	return dao.getObsCount(conceptId);
    }

    @Override
    public List<Integer> getObsIds(Integer conceptId){
    	return dao.getObsIdsWithQuestionConcept(conceptId);
    }

    @Override
    public List<Drug> getMatchingDrugsByConcept(Concept concept) {
        ConceptService conceptService = Context.getConceptService();
        return conceptService.getDrugsByConcept(concept);
    }

    @Override
	public List<Drug> getDrugsByIngredient(Concept ingredient) {
        return dao.getDrugsByIngredient(ingredient);
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
    public void addDrugNames(Concept concept, List<String> drugNames) {
        for (Drug od : getMatchingDrugsByConcept(concept)) {
            drugNames.add(od.getFullName(null));
        }
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
    public List<ConceptSet> getMatchingConceptSetConcepts(Concept concept) {
        ConceptService conceptService = Context.getConceptService();
        return conceptService.getSetsContainingConcept(concept);
    }

    @Override
    public List<ConceptSet> getMatchingConceptSets(Concept concept) {
        ConceptService conceptService = Context.getConceptService();
        return conceptService.getConceptSetsByConcept(concept);
    }

    @Override
    public List<PersonAttributeType> getMatchingPersonAttributeTypes(Concept concept) {
        List<PersonAttributeType> matchingPersonAttributeTypes = new ArrayList<PersonAttributeType>();
        for (PersonAttributeType p : getPersonAttributeTypes()) {
            if (p.getFormat().toLowerCase().contains("concept")) {
                if (p.getForeignKey() != null && p.getForeignKey().equals(concept.getConceptId())) {
                    matchingPersonAttributeTypes.add(p);
                }
            }
        }
        return matchingPersonAttributeTypes;
    }

    @Override
    public List<ConceptAnswer> getMatchingConceptAnswers(Concept concept) {
        List<ConceptAnswer> conceptAnswers = getMatchingConceptAnswerAnswers(concept);
        for (ConceptAnswer c : getMatchingConceptAnswerQuestions(concept)) {
            conceptAnswers.add(c);
        }
        return conceptAnswers;
    }

    public List<ConceptAnswer> getMatchingConceptAnswerQuestions(Concept concept) {
        List<ConceptAnswer> matchingConceptAnswers = new ArrayList<ConceptAnswer>();
        for (ConceptAnswer ca : concept.getAnswers()) {
            matchingConceptAnswers.add(ca);
        }
        return matchingConceptAnswers;
    }

    public List<ConceptAnswer> getMatchingConceptAnswerAnswers(Concept concept) {
        List<ConceptAnswer> matchingConceptAnswers = new ArrayList<ConceptAnswer>();
        //Concepts that are questions answered by this concept, and possibly others
        for (Concept c : getConceptsByAnswerForAConcept(concept)) {
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

    private List<Concept> getConceptsByAnswerForAConcept(Concept concept) {
        ConceptService conceptService = Context.getConceptService();
        return conceptService.getConceptsByAnswer(concept);
    }

    private List<PersonAttributeType> getPersonAttributeTypes() {
        PersonService personService = Context.getPersonService();
        return personService.getAllPersonAttributeTypes();
    }

    private void getConceptSetIds(Concept concept, List<Integer> conceptSetIds) {
        for (ConceptSet c : getMatchingConceptSets(concept)) {
            conceptSetIds.add(c.getConceptSetId());
        }
        for (ConceptSet cs : getMatchingConceptSetConcepts(concept)) {
            conceptSetIds.add(cs.getConceptSetId());
        }
    }

    private void getConceptAnswersIds(Concept concept, List<Integer> conceptAnswerIds) {
        for (ConceptAnswer a : getMatchingConceptAnswers(concept)) {
            conceptAnswerIds.add(a.getConceptAnswerId());
        }
    }
}
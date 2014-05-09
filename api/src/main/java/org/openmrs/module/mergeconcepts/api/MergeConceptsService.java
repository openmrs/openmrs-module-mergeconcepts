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
package org.openmrs.module.mergeconcepts.api;

import org.openmrs.*;
import org.openmrs.api.OpenmrsService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * This service exposes module's core functionality. It is a Spring managed bean which is configured in moduleApplicationContext.xml.
 * <p>
 * It can be accessed only via Context:<br>
 * <code>
 * Context.getService(MergeConceptsService.class).someMethod();
 * </code>
 * 
 * @see org.openmrs.api.context.Context
 */
@Transactional
public interface MergeConceptsService extends OpenmrsService {

    public void setConceptsAndRoutes(Concept newConcept, List<Drug> drugsToUpdate, List<Drug> drugsByRouteConcept);

    public int getObsCount(Integer conceptId);
	
	public List<Integer> getObsIds(Integer conceptId);

	public void updateObs(Concept oldConcept, Concept newConcept);

    public void updateFields(Concept oldConcept, Concept newConcept);

	public List<Drug> getDrugsByIngredient(Concept ingredient);

    void updateOrders(Concept oldConceptId, Concept newConceptId);

    public List<Order> getMatchingOrders(Concept concept);

    public void updatePrograms(int oldConceptId, int newConceptId);

    public List<Program> getMatchingPrograms(Concept concept);

    public Set<FormField> getMatchingFormFields(Concept concept);

    public Set<Form> getMatchingForms(Concept concept);

    public List<Drug> getDrugsByRouteConcept(Concept concept);

    public void updateDrugs(Concept oldConcept, Concept newConcept);

    public List<Drug> getMatchingDrugsByConcept(Concept concept);

    public void updateConceptSets(Concept oldConcept, Concept newConcept);

    public void updateConceptAnswers(Concept oldConcept, Concept newConcept);

    public List<ConceptSet> getMatchingConceptSetConcepts(Concept concept);

    public List<PersonAttributeType> getMatchingPersonAttributeTypes(Concept concept);

    public List<ConceptSet> getMatchingConceptSets(Concept concept);

    public List<ConceptAnswer> getMatchingConceptAnswers(Concept concept);

    public void updatePersonAttributeTypes(Concept oldConcept, Concept newConcept);

    void update(Integer oldConceptId, Integer newConceptId, Concept oldConcept, Concept newConcept);
}
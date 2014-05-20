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
package org.openmrs.module.mergeconcepts.api.db;

import org.openmrs.*;
import org.openmrs.module.mergeconcepts.api.MergeConceptsService;

import java.util.List;
import java.util.Set;


/**
 *  Database methods for {@link MergeConceptsService}.
 */

public interface MergeConceptsDAO {

	public Integer getObsCount(Integer conceptId);
	
	public void updateObs(Concept oldConcept, Concept newConcept);

    public List<Integer> getObsIdsWithQuestionConcept(Integer conceptId);

    public List<Integer> getObsIdsWithAnswerConcept(Integer conceptId);

	public List<Program> getProgramsByConcept(Concept concept);

	public List<ProgramWorkflow> getProgramWorkflowsByConcept(Concept concept);

	public List<ProgramWorkflowState> getProgramWorkflowStatesByConcept(Concept concept);
	
	public List<Drug> getDrugsByIngredient(Concept ingredient);

    public void updateOrders(Concept oldConcept, Concept newConcept);

    public List<Order> getMatchingOrders(Concept concept);

    public void updatePrograms(int oldConceptId, int newConceptId);

    void updateFields(Concept oldConcept, Concept newConcept);

    public Set<FormField> getMatchingFormFields(Concept concept);

    public Set<Form> getMatchingForms(Concept concept);

    public List<Drug> getDrugsByRouteConcept(Concept concept);

    public List<Drug> getDrugsByDosageFormConcept(Concept concept);
}


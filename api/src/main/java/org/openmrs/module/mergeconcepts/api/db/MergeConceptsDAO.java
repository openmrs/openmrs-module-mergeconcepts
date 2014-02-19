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

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.module.mergeconcepts.api.MergeConceptsService;
import org.openmrs.ProgramWorkflowState;


/**
 *  Database methods for {@link MergeConceptsService}.
 */

public interface MergeConceptsDAO {

	public Integer getObsCount(Integer conceptId);
	
	public void updateObs(Integer oldConceptId, Integer newConceptId);

    public List<Integer> getObsIdsWithQuestionConcept(Integer conceptId);

    public List<Integer> getObsIdsWithAnswerConcept(Integer conceptId);

	public List<Program> getProgramsByConcept(Concept concept);

	public List<ProgramWorkflow> getProgramWorkflowsByConcept(Concept concept);

	public List<ProgramWorkflowState> getProgramWorkflowStatesByConcept(Concept concept);
	
	public List<Drug> getDrugsByIngredient(Concept ingredient);
}
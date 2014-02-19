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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * Tests {@link ${MergeConceptsService}}.
 */
public class  MergeConceptsServiceTest extends BaseModuleContextSensitiveTest {
	
	int knownConceptId = 18; // name="FOOD ASSISTANCE"

	MergeConceptsService mergeConceptsService = null;

	@Before
	public void setUp()  {
        mergeConceptsService = Context.getService(MergeConceptsService.class);
	}

	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(MergeConceptsService.class));
	}

	/**
	 * @see MergeConceptsService#getQuestionConceptObsCount(Integer)
	 * @verifies return a count of question concept obs
	 */
	@Test
	public void getObsCount_shouldReturnACountOfQuestionAndAnswerConceptObs()
			throws Exception {
		List<Concept> conceptList = new ArrayList<Concept>();
        conceptList.add(Context.getConceptService().getConcept(knownConceptId));
        int knownObsId = 7;
        createObsWithConceptAnswer(knownObsId, knownConceptId );

        Integer actualServiceObsCount = mergeConceptsService.getObsCount(knownConceptId);

        Integer expectedServiceObsCount = 2;
        assertEquals(expectedServiceObsCount, actualServiceObsCount);
	}

    @Test
    public void updateObs_shouldUpdateObsWithNewConceptInQuestionsAndAnswerConcepts() {
        Obs savedObsWithId10 = createObsWithConceptAnswer(10, knownConceptId);
        Obs savedObsWithId12 = createObsWithConceptQuestion(12, knownConceptId);

        int newConceptId = 22;
        mergeConceptsService.updateObs(knownConceptId, newConceptId);

        assertEquals(new Integer(newConceptId), savedObsWithId10.getValueCoded().getId());
        assertEquals(new Integer(newConceptId), savedObsWithId12.getConcept().getId());
    }

    private Obs createObsWithConceptAnswer(int obsId, int conceptId) {
        ObsService obsService = Context.getObsService();
        Obs knownAnswerObs = obsService.getObs(obsId);
        knownAnswerObs.setValueCoded(Context.getConceptService().getConcept(conceptId));
        return obsService.saveObs(knownAnswerObs,"");
    }

    private Obs createObsWithConceptQuestion(int obsId, int conceptId) {
        ObsService obsService = Context.getObsService();
        Obs knownAnswerObs = obsService.getObs(obsId);
        knownAnswerObs.setConcept(Context.getConceptService().getConcept(conceptId));
        return obsService.saveObs(knownAnswerObs,"");
    }
}

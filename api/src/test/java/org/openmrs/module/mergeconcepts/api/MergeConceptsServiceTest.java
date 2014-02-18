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
	int knownAnswerConceptId = 5089;
	
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
        createObsWithConceptAnswer();
        Integer expectedServiceObsCount = 2;

        Integer actualServiceObsCount = mergeConceptsService.getObsCount(knownConceptId);

		Assert.assertEquals(expectedServiceObsCount, actualServiceObsCount);
	}

    private void createObsWithConceptAnswer() {
        ObsService obsService = Context.getObsService();
        int knownObsId = 7;
        Obs knownAnswerObs = obsService.getObs(knownObsId);
        knownAnswerObs.setValueCoded(Context.getConceptService().getConcept(knownConceptId));
        obsService.saveObs(knownAnswerObs,"");
    }
}

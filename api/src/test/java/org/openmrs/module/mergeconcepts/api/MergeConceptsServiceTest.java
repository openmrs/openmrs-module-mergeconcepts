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
import org.junit.Ignore;

import org.openmrs.Concept;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;

/**
 * Tests {@link ${MergeConceptsService}}.
 */
public class  MergeConceptsServiceTest extends BaseModuleContextSensitiveTest {
	
	int knownQuestionConceptId = 18;
	int knownAnswerConceptId = 5089;
	
	MergeConceptsService service = null;
	
	@Before
	public void setUp(){
		service = Context.getService(MergeConceptsService.class);
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
	public void getQuestionConceptObsCount_shouldReturnACountOfQuestionConceptObs()
			throws Exception {
		
		ObsService obsService = Context.getObsService();
		List<Concept> conceptList = new ArrayList<Concept>();
		conceptList.add(Context.getConceptService().getConcept(knownQuestionConceptId));
		Integer obsServiceCount = obsService.getObservations(null, null,  conceptList, null, null, null, null, null, null, null, null,
				false).size();
		
		Integer myServiceObsCount = service.getObsCount(knownQuestionConceptId);
		
		Assert.assertEquals(obsServiceCount, myServiceObsCount);
		//TODO auto-generated
		//Assert.fail("Not yet implemented");
	}
}

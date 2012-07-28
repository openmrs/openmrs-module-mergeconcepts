package org.openmrs.module.mergeconcepts.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.api.ObsService;
import org.openmrs.api.context.*;
import org.openmrs.*;
import junit.framework.*; //assertions
import org.junit.*; //annotations
import org.openmrs.test.*;
import org.junit.Test;
import junit.framework.Assert;

public class OpenMRSApiTest extends BaseModuleContextSensitiveTest {
	
	@Test
	public void sanityCheck() throws Exception{
		Assert.assertTrue(Boolean.TRUE);
		
	}
	
	@Test
	public void obsService_shouldGetObsService(){
		ObsService obsService = Context.getObsService();
		Assert.assertNotNull(obsService);
		int knownConceptId = 19;
		java.util.List<Concept> conceptList = new ArrayList<Concept>();
		conceptList.add(new Concept(knownConceptId));
	//	Integer testObsCount = obsService.getObservationCount(null, null, conceptList, conceptList, null, null, null, null, null, false);
	//	Assert.assertTrue(obsService.getObservationCount(null, null, conceptList, conceptList, null, null, null, null, null, false) > 0);
	
	/*
	 * Question:  does 
	 * getObservationsByPersonAndConcept(Person who, Concept question) 
          Get all nonvoided observations for the given patient with the given concept as the question concept (conceptId)
	 * 
	 * return the same as 
	 * 
	 * getObservations(List<Person> whom, List<Encounter> encounters, List<Concept> questions, List<Concept> answers, 
	 * List<OpenmrsConstants.PERSON_TYPE> personTypes, List<Location> locations, List<String> sort, 
	 * Integer mostRecentN, Integer obsGroupId, Date fromDate, Date toDate, boolean includeVoidedObs) 
          This method fetches observations according to the criteria in the given arguments.
     * 
     * Answer: no.
	 */
		List<Obs> matches = Context.getObsService().getObservations( null, null, null, conceptList, null, null, null, null, null, null, null,
				true);
		//Assert.assertTrue("confirm answers list not null",matches.size() > 0);
		List<Obs> qMatches = Context.getObsService().getObservations( null, null, conceptList, null, null, null, null, null, null, null, null,
				true);
		Assert.assertTrue("confirm question list not null",qMatches.size() > 0 || matches.size() > 0);
	
	}

	
}
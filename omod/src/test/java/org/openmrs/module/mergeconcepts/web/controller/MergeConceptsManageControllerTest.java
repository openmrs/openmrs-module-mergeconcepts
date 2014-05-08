package org.openmrs.module.mergeconcepts.web.controller;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mergeconcepts.api.MergeConceptsService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.ui.ModelMap;

import static org.junit.Assert.*;


public class MergeConceptsManageControllerTest extends BaseModuleContextSensitiveTest{
	MergeConceptsManageController controller = null;

	Concept knownQuestionConcept = null;
	Concept knownAnswerConcept = null;

	Integer knownConceptId = 3;// 3 = COUGH SYRUP in the standard test dataset (standardTestDataset.xml) in openmrs-core

	@Before
	public void setUp(){
		controller = new MergeConceptsManageController();

		int knownQuestionConceptId=18;
		knownQuestionConcept = Context.getConceptService().getConcept(knownQuestionConceptId);

		int knownAnswerConceptId=5089;
		knownAnswerConcept = Context.getConceptService().getConcept(knownAnswerConceptId);

		/**
		 * int knownObsId = 13;
		knownAnswerObs1 = Context.getObsService().getObs(knownObsId);
		knownQuestionObs = new Obs();


		knownQuestionObs.setEncounter( knownAnswerObs.getEncounter());
		knownQuestionObs.setConcept(knownQuestionConcept);
		knownQuestionObs.setPerson(knownAnswerObs.getPerson());
		knownQuestionObs.setLocation(knownAnswerObs.getLocation());
		knownQuestionObs.setObsDatetime(knownAnswerObs.getObsDatetime());
		//Obs(Person person, Concept question, Date obsDatetime, Location location)
		knownQuestionObs.setValueText("This obs has a value");
		Context.getObsService().saveObs(knownQuestionObs, "");
		Assert.assertTrue(knownQuestionObs.getObsId() > 0);*/
	}

	@Test
	public void getOldConcept_shouldSetModelAttributeOldConcept_ToConceptUserWantsToRetire()
			throws Exception {
	    Concept oldConcept = controller.getOldConcept(knownConceptId.toString());
	    Assert.assertNotNull(oldConcept);
	    assertEquals("it instantiated the concept we requested", knownConceptId, oldConcept.getConceptId());
	}

    @Test
    public void getNewConcept_ShouldSetModelAttributNewConcept_ToConceptUserWantsToKeep(){
        Concept newConcept = controller.getNewConcept(knownConceptId.toString());
        Assert.assertNotNull(newConcept);
        assertEquals(knownConceptId, newConcept.getConceptId());
    }

    @Test
    public void updateDrugs_ShouldUpdateConceptIdInDrug(){
        int newConceptId = 88;
        int oldConceptId = 792;
        int drugId = 2;

        ConceptService conceptService = Context.getConceptService();
        Concept oldConcept = conceptService.getConcept(oldConceptId);
        Concept newConcept = conceptService.getConcept(newConceptId);
        Drug drug = conceptService.getDrug(drugId);

        assertFalse(newConceptId == drug.getConcept().getConceptId());
        MergeConceptsService service = Context.getService(MergeConceptsService.class);
        service.updateDrugs(oldConcept, newConcept);

        Drug updatedDrug = conceptService.getDrug(drugId);

        assertTrue(newConceptId == updatedDrug.getConcept().getConceptId());
    }

    @Test
    public void updateDrugs_ShouldUpdateRouteConceptIdInDrug(){
        int oldRouteConceptId = 22;
        int newRouteConceptId = 23;
        int drugId = 11;

        Concept newRoute = Context.getConceptService().getConcept(newRouteConceptId);
        Concept oldRoute = Context.getConceptService().getConcept(oldRouteConceptId);

        Drug drugToWhichWeAddRoute = Context.getConceptService().getDrug(drugId);
        drugToWhichWeAddRoute.setRoute(oldRoute);

        Context.getConceptService().saveDrug(drugToWhichWeAddRoute);

        assertTrue(oldRouteConceptId == drugToWhichWeAddRoute.getRoute().getConceptId());


        MergeConceptsService service = Context.getService(MergeConceptsService.class);
        service.updateDrugs(oldRoute, newRoute);

        Drug updatedDrug = Context.getConceptService().getDrug(drugId);

        assertEquals(new Integer(newRouteConceptId),  updatedDrug.getRoute().getConceptId() );
    }

	@Test
	public void results_shouldDisplayUpdatedReferencesToOldConceptAndNewConcept()
			throws Exception {

		ModelMap modelMap = new ModelMap();
		//Check the counts before making the change
		//TODO: Hardcoded counts are bad. Call MergeConceptsService method that returns a count for both counters
		int numObsWithOldConceptId = 1; // 1 because the code told me it returned 1
		int numObsWithNewConceptId = 0;

		controller.results(modelMap, knownQuestionConcept.getConceptId(),
			 knownConceptId);//showPage(request, response);

		assertTrue("The controller set the attribute", modelMap.containsKey("newObsCount"));
		Assert.assertNotNull("It set the attribute equal to something",modelMap.get("newObsCount"));
		//results doesn't call the method that updates the obs
		//so it will still match the original count
		assertEquals("New count value matches count",
                ((Integer) modelMap.get("newObsCount")).intValue(), numObsWithNewConceptId);
		assertTrue(modelMap.containsKey("oldObsCount"));
		Assert.assertNotNull("It set the attribute equal to something",modelMap.get("oldObsCount"));

		//results doesn't call the method that updates the obs
		//so it will still match the original count
		assertEquals("Old obs value matches count", ((Integer) modelMap.get("oldObsCount")).intValue(),
                numObsWithOldConceptId);

	}

}
package org.openmrs.module.mergeconcepts.web.controller;


import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Drug;

import org.openmrs.api.context.Context;
import org.openmrs.module.mergeconcepts.api.MergeConceptsService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.ui.ModelMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;


public class MergeConceptsManageControllerTest extends BaseModuleContextSensitiveTest {
    MergeConceptsManageController controller = new MergeConceptsManageController();

    Concept knownQuestionConcept;
    Concept knownAnswerConcept;

    Integer knownConceptId = 3;// 3 = COUGH SYRUP in the standard test dataset (standardTestDataset.xml) in openmrs-core

    @Before
    public void setUp() {
        int knownQuestionConceptId = 18;
        knownQuestionConcept = getConceptFromConceptId(knownQuestionConceptId);

        int knownAnswerConceptId = 5089;
        knownAnswerConcept = getConceptFromConceptId(knownAnswerConceptId);

    }

    @Test
    public void shouldSetModelAttributeOldConceptToConceptUserWantsToRetire() {
        Concept oldConcept = controller.getOldConcept(knownConceptId.toString());
        assertThat("it instantiated the concept we requested", knownConceptId, is(oldConcept.getConceptId()));
    }

    @Test
    public void shouldSetNewConceptModelAttributeToConceptUserWantsToKeep() {
        Concept newConcept = controller.getNewConcept(knownConceptId.toString());
        assertEquals(knownConceptId, newConcept.getConceptId());
    }


    @Test
    public void shouldDisplayUpdatedReferencesToOldConceptAndNewConcept() {
		ModelMap modelMap = new ModelMap();
		//Check the counts before making the change
		//TODO: Hardcoded counts are bad. Call MergeConceptsService method that returns a count for both counters
		int numObsWithOldConceptId = 1; // 1 because the code told me it returned 1
		int numObsWithNewConceptId = 0;

		controller.results(modelMap, knownQuestionConcept.getConceptId(),
			 knownConceptId);//showPage(request, response);

		assertTrue("The controller set the attribute", modelMap.containsKey("newObsCount"));
		assertNotNull("It set the attribute equal to something", modelMap.get("newObsCount"));
		//results doesn't call the method that updates the obs
		//so it will still match the original count
		assertEquals("New count value matches count",
                ((Integer) modelMap.get("newObsCount")).intValue(), numObsWithNewConceptId);
		assertTrue(modelMap.containsKey("oldObsCount"));
		assertNotNull("It set the attribute equal to something", modelMap.get("oldObsCount"));

		//results doesn't call the method that updates the obs
		//so it will still match the original count
		assertEquals("Old obs value matches count", ((Integer) modelMap.get("oldObsCount")).intValue(),
                numObsWithOldConceptId);

	}


    private Concept getConceptFromConceptId(int conceptId) {
        return Context.getConceptService().getConcept(conceptId);
    }
}
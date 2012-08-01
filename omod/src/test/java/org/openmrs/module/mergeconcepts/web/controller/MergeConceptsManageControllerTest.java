package org.openmrs.module.mergeconcepts.web.controller;


import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.api.context.Context;

import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.ui.ModelMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class MergeConceptsManageControllerTest extends BaseModuleContextSensitiveTest{
	MergeConceptsManageController controller = null;

	Concept knownQuestionConcept = null;
	Concept knownAnswerConcept = null;

	Obs knownQuestionObs = null;
	Obs knownAnswerObs = null;

	Integer knownConceptId = 3;// a concept we can change things to. 3 = COUGH SYRUP in the standard test dataset

	@Before
	public void setUp(){
		controller = new MergeConceptsManageController();

		int knownQuestionConceptId=18;
		knownQuestionConcept = Context.getConceptService().getConcept(knownQuestionConceptId);

		int knownAnswerConceptId=5089;
		knownAnswerConcept = Context.getConceptService().getConcept(knownAnswerConceptId);

		/**
		 * int knownObsId = 13;
		knownAnswerObs = Context.getObsService().getObs(knownObsId);
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

	/**
	 * @see MergeConceptsManageController#chooseConcepts()
	 * @verifies (eventually) prepopulate concept widgets
	 */
	@Ignore
	@Test
	public void chooseConcepts_shouldEventuallyPrepopulateConceptWidgets()
			throws Exception {
		//TODO auto-generated
		Assert.fail("Not yet implemented");
	}

	/**
	 * @see MergeConceptsManageController#executeMerge(ModelMap)
	 * @verifies merge concepts
	 */
	@Ignore
	@Test
	public void executeMerge_shouldMergeConcepts() throws Exception {
		//TODO auto-generated
		Assert.fail("Not yet implemented");

	}

	/**
	 * @see MergeConceptsManageController#generateNewReferenceLists(String)
	 * @verifies generate fresh lists of references to new concept
	 */
	@Ignore
	@Test
	public void generateNewReferenceLists_shouldGenerateFreshListsOfReferencesToNewConcept()
			throws Exception {
		//TODO auto-generated
		Assert.fail("Not yet implemented");
	}

	/**
	 * @see MergeConceptsManageController#generateReferenceLists(String)
	 * @verifies generate fresh lists of references to old concept
	 */
	@Ignore
	@Test
	public void generateReferenceLists_shouldGenerateFreshListsOfReferencesToOldConcept()
			throws Exception {
		//TODO auto-generated
		Assert.fail("Not yet implemented");
	}

	/**
	 * @see MergeConceptsManageController#getNewConcept(String)
	 * @verifies set model attribute "newConcept" to concept user wants to keep
	 */
	@Ignore
	@Test
	public void getNewConcept_shouldSetModelAttributeNewConceptToConceptUserWantsToKeep()
			throws Exception {
		//TODO auto-generated
		Assert.fail("Not yet implemented");
	}

	/**
	 * @see MergeConceptsManageController#getOldConcept(String)
	 * @verifies set model attribute "oldConcept" to concept user wants to retire
	 */

	@Test
	public void getOldConcept_shouldSetModelAttributeOldConceptToConceptUserWantsToRetire()
			throws Exception {
	    Concept c = controller.getOldConcept(knownConceptId.toString());
	    Assert.assertNotNull(c);
	    Assert.assertEquals("it instantiated the concept we requested",knownConceptId, c.getConceptId());
	}

	/**
	 * @see MergeConceptsManageController#preview(ModelMap,String,String)
	 * @verifies display references to oldConcept and newConcept
	 */
	@Ignore
	@Test
	public void preview_shouldDisplayReferencesToOldConceptAndNewConcept()
			throws Exception {
		//TODO auto-generated
		Assert.fail("Not yet implemented");
	}

	/**
	 * @see MergeConceptsManageController#results(ModelMap)
	 * @verifies display updated references to oldConcept and newConcept
	 */
	@Test
	public void results_shouldDisplayUpdatedReferencesToOldConceptAndNewConcept()
			throws Exception {
		ModelMap modelMap = new ModelMap();
		//Check the counts before making the change
		//TODO: Hardcoded counts are bad. Call MergeConceptsService method that returns a count for both counters
		int numObsWithOldConceptId = 1; // 1 because the code told me it returned 1
		int numObsWithNewConceptId = 0;

		controller.results(modelMap, knownQuestionConcept.getConceptId().toString(),
			 knownConceptId.toString());//showPage(request, response);

		Assert.assertTrue("The controller set the attribute",modelMap.containsKey("newObsCount"));
		Assert.assertNotNull("It set the attribute equal to something",modelMap.get("newObsCount"));
		//results doesn't call the method that updates the obs
		//so it will still match the original count
		Assert.assertEquals("New count value matches count",
			((Integer)modelMap.get("newObsCount")).intValue(),numObsWithNewConceptId);
		Assert.assertTrue(modelMap.containsKey("oldObsCount"));
		Assert.assertNotNull("It set the attribute equal to something",modelMap.get("oldObsCount"));

		//results doesn't call the method that updates the obs
		//so it will still match the original count
		Assert.assertEquals("Old obs value matches count", ((Integer)modelMap.get("oldObsCount")).intValue(),
			numObsWithOldConceptId);
	}

	/**
	 * @see MergeConceptsManageController#showPage(ModelMap)
	 * @verifies do nothing
	 */
	@Ignore
	@Test
	public void showPage_shouldDoNothing() throws Exception {
		//TODO auto-generated
		Assert.fail("Not yet implemented");
	}

	/**
	 * @see MergeConceptsManageController#getMatchingObs(Concept)
	 * @verifies return a list of Obs that use the concept as a question or answer
	 */
	@Test
	public void getMatchingObs_shouldReturnAListOfObsThatUseTheConceptAsAQuestionOrAnswer()
			throws Exception {
		List<Obs> questionResults = controller.getMatchingObs(knownQuestionConcept);
		List<Obs> answerResults = controller.getMatchingObs(knownAnswerConcept);

		Assert.assertTrue(questionResults.size() > 0);
		Assert.assertTrue(answerResults.size() > 0);
	}

	/**
	 * @see MergeConceptsManageController#getMatchingObs(Concept)
	 * @verifies return an empty List if no matches
	 */
	@Test
	public void getMatchingObs_shouldReturnAnEmptyListIfNoMatches()
			throws Exception {
		int noMatches = controller.getMatchingObs(new Concept(-1)).size();
		Assert.assertEquals(noMatches, 0);
	}

	/**
	 * @see MergeConceptsManageController#getMatchingObs(Concept)
	 * @verifies return an empty list if Concept is null
	 */
	@Test
	public void getMatchingObs_shouldReturnAnEmptyListIfConceptIsNull()
			throws Exception {
		List<Obs> test = controller.getMatchingObs(null);
		Assert.assertEquals(test.size(), 0);
	}



}
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
    public void shouldUpdateConceptIdInDrug() {
        int newConceptId = 88;
        int oldConceptId = 792;

        assertThatCurrentDrugConceptIdEqualsOldConceptId(2, oldConceptId);

        MergeConceptsService service = Context.getService(MergeConceptsService.class);
        service.updateDrugs(getConceptFromConceptId(oldConceptId), getConceptFromConceptId(newConceptId));

        assertThatUpdatedDrugConceptIdEqualsNewConceptId(2, newConceptId);
    }


    private void assertThatCurrentDrugConceptIdEqualsOldConceptId(int drugId, int oldConceptId) {
        Drug drug = getDrugById(drugId);
        Integer drugConceptId = getConceptIdFromDrug(drug);
        assertThat(drugConceptId, is(oldConceptId));
    }

    private void assertThatUpdatedDrugConceptIdEqualsNewConceptId(int drugId, int newConceptId) {
        Drug updatedDrug = getDrugById(drugId);
        Integer updatedDrugConceptId = getConceptIdFromDrug(updatedDrug);
        assertThat(updatedDrugConceptId, is(newConceptId));
    }

    private Integer getConceptIdFromDrug(Drug drug) {
        return drug.getConcept().getConceptId();
    }

    private Concept getConceptFromConceptId(int conceptId) {
        return Context.getConceptService().getConcept(conceptId);
    }

    @Test
    public void shouldUpdateRouteConceptIdInDrug() {
        Concept oldConceptWithRoute = getConceptFromConceptId(22);
        Concept newConceptWithRoute = getConceptFromConceptId(23);
        Drug drug = getDrugById(11);
        drug.setRoute(oldConceptWithRoute);
        saveDrugToConceptService(drug);

        Integer oldRouteConceptIdInDrug = drug.getRoute().getConceptId();
        assertThat(oldRouteConceptIdInDrug, is(22));

        MergeConceptsService service = Context.getService(MergeConceptsService.class);
        service.updateDrugs(oldConceptWithRoute, newConceptWithRoute);

        Integer updatedRouteConceptIdInDrug = drug.getRoute().getConceptId();
        assertThat(updatedRouteConceptIdInDrug, is(23));
    }


    @Test
    public void shouldUpdateDoseFormConceptIdInDrug() {
        Concept oldConceptWithDosageForm = getConceptFromConceptId(7);
        Concept newConceptWithDosageForm = getConceptFromConceptId(8);
        Drug drug = getDrugById(11);
        drug.setDosageForm(oldConceptWithDosageForm);
        saveDrugToConceptService(drug);

        Integer oldDosageFormConceptIdInDrug = drug.getDosageForm().getConceptId();
        assertThat(oldDosageFormConceptIdInDrug, is(7));

        MergeConceptsService service = Context.getService(MergeConceptsService.class);
        service.updateDrugs(oldConceptWithDosageForm, newConceptWithDosageForm);

        Integer updatedDosageFormConceptIdInDrug = drug.getDosageForm().getConceptId();
        assertThat(updatedDosageFormConceptIdInDrug, is(8));
    }

    private Drug saveDrugToConceptService(Drug drug) {
        return Context.getConceptService().saveDrug(drug);
    }

    private Drug getDrugById(int drugId) {
        return Context.getConceptService().getDrug(drugId);
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

}
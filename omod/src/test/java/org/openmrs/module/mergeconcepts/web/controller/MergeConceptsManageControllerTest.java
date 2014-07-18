package org.openmrs.module.mergeconcepts.web.controller;


import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Form;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.ui.ModelMap;

import java.util.HashSet;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


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
    public void results_shouldGetObsCountsForBothConceptsAndAttachThemToTheModelForDisplay() {
        int oldConceptSetId = 1;
        ModelMap modelMap = new ModelMap();

        controller.results(modelMap, knownQuestionConcept.getConceptId(), knownConceptId);

        assertEquals(((List) modelMap.get("newPersonAttributeTypes")).size(), 0);
        assertEquals(((List) modelMap.get("newConceptAnswers")).size(), 0);
        assertEquals(((List) modelMap.get("newOrders")).size(), 0);
        // Should be returning 1 drug name "NYQUIL"
//        assertEquals(((List) modelMap.get("newDrugs")).size(), 0);
        assertEquals(((List) modelMap.get("newPrograms")).size(), 0);
        assertEquals((int) ((Form) ((HashSet)modelMap.get("newForms")).iterator().next()).getFormId(), 1);
        assertEquals(((List) modelMap.get("newConceptSets")).size(), 0);
        assertEquals(((Integer) modelMap.get("newConceptId")).intValue(), 3);
        assertEquals(((Integer) modelMap.get("newObsCount")).intValue(), 0);

        assertEquals(((Integer) modelMap.get("oldObsCount")).intValue(), 1);
        assertEquals(((Integer) modelMap.get("oldConceptId")).intValue(), 18);
        assertEquals(((List) modelMap.get("oldConceptAnswers")).size(), 0);
        assertEquals(((List) modelMap.get("oldPrograms")).size(), 0);
        assertEquals(((List) modelMap.get("oldPersonAttributeTypes")).size(), 0);
        assertTrue(((List) modelMap.get("oldConceptSets")).get(0).equals(oldConceptSetId));
        assertEquals(((List) modelMap.get("oldOrders")).size(), 0);
        assertEquals(((HashSet) modelMap.get("oldForms")).size(), 0);
        assertEquals(((List) modelMap.get("oldDrugs")).size(), 0);
	}

    private Concept getConceptFromConceptId(int conceptId) {
        return Context.getConceptService().getConcept(conceptId);
    }
}
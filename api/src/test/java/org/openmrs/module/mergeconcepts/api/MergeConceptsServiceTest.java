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

import org.junit.Before;
import org.junit.Test;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.api.context.Context;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class  MergeConceptsServiceTest extends BaseModuleContextSensitiveTest {
	
	int knownConceptId = 18; // name="FOOD ASSISTANCE"

	MergeConceptsService mergeConceptsService = null;
    private ConceptService conceptService;

    @Before
	public void setUp() throws Exception {
        mergeConceptsService = Context.getService(MergeConceptsService.class);
        executeDataSet(EXAMPLE_XML_DATASET_PACKAGE_PATH );
        conceptService = Context.getConceptService();
    }

	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(MergeConceptsService.class));
	}

	@Test
	public void getObsCount_shouldReturnACountOfQuestionAndAnswerConceptObs()
			throws Exception {
		List<Concept> conceptList = new ArrayList<Concept>();
        conceptList.add(Context.getConceptService().getConcept(knownConceptId));
        int knownObsId = 7;
        updateObsWithConceptAnswer(knownObsId, knownConceptId);

        Integer actualServiceObsCount = mergeConceptsService.getObsCount(knownConceptId);

        Integer expectedServiceObsCount = 2;
        assertEquals(expectedServiceObsCount, actualServiceObsCount);
	}

    @Test
    public void updateObs_shouldUpdateObsWithNewConceptInQuestionsAndAnswerConcepts() {
        Obs savedObsWithId10 = updateObsWithConceptAnswer(10, knownConceptId);
        Obs savedObsWithId12 = updateObsWithConceptQuestion(12, knownConceptId);
        int newConceptId = 22;

        Concept knownConcept = conceptService.getConcept(knownConceptId);
        Concept newConcept = conceptService.getConcept(newConceptId);

        mergeConceptsService.updateObs(knownConcept, newConcept);

        assertEquals(new Integer(newConceptId), savedObsWithId10.getValueCoded().getId());
        assertEquals(new Integer(newConceptId), savedObsWithId12.getConcept().getId());
    }

    @Test
    public void updateOrders_shouldUpdateOrdersWithNewConcept() throws Exception {
        int oldConceptId = 23;
        Order orderWithId4 = updateOrderWithConcept(4, 23);
        Order orderWithId5 = updateOrderWithConcept(5, 23);
        int newConceptId = 18;

        Concept oldConcept = conceptService.getConcept(oldConceptId);
        Concept newConcept = conceptService.getConcept(newConceptId);

        mergeConceptsService.updateOrders(oldConcept,newConcept);

        assertEquals(new Integer(newConceptId), orderWithId4.getConcept().getId());
        assertEquals(new Integer(newConceptId), orderWithId5.getConcept().getId());
    }

    @Test
    public void updateFormFields_shouldUpdateFormFieldAttributes() throws Exception {
        int newConceptId = 88;
        int oldConceptId = 3;

        String newName = conceptService.getConcept(newConceptId).getName().toString();
        String newDescription = conceptService.getConcept(newConceptId).getDescription().toString();

        Concept oldConcept = conceptService.getConcept(oldConceptId);
        Concept newConcept = conceptService.getConcept(newConceptId);

        mergeConceptsService.updateFields(oldConcept, newConcept);

        int fieldId = 1;
        FormService formService = Context.getFormService();
        Field field = formService.getField(fieldId);

        String updatedName = field.getName();
        String updatedDescription = field.getDescription();
        int updatedConceptId = field.getConcept().getId();

        assertEquals(newConceptId, updatedConceptId);
        assertEquals(newName, updatedName);
        assertEquals(newDescription, updatedDescription);
    }

    @Test
    public void updateProgram_shouldUpdateProgramNameDescriptionAndId() throws Exception {
        //program 2 references concept 10
        //retire concept 10 in favor of concept 9

        int newConceptId = 9;
        int oldConceptId = 10;

        ConceptService conceptService = Context.getConceptService();
        String newName = conceptService.getConcept(newConceptId).getName().toString();
        String newDescription = conceptService.getConcept(newConceptId).getDescription().toString();

        mergeConceptsService.updatePrograms(oldConceptId, newConceptId);

        int programId = 2;
        ProgramWorkflowService programService = Context.getProgramWorkflowService();
        Program program = programService.getProgram(programId);

        String updatedName = program.getName();
        String updatedDescription = program.getDescription();
        int updatedConceptId = program.getConcept().getId();

        assertEquals(newConceptId, updatedConceptId);
        assertEquals(newName, updatedName);
        assertEquals(newDescription, updatedDescription);
    }


    @Test
    public void getDrugsByRouteConcept() throws Exception {

        Concept route = Context.getConceptService().getConcept(22);
        Drug drugToWhichWeAddRoute = Context.getConceptService().getDrug(11);
        drugToWhichWeAddRoute.setRoute(route);

        Context.getConceptService().saveDrug(drugToWhichWeAddRoute);

        List<Drug> drugs = mergeConceptsService.getDrugsByRouteConcept(route);

        assertEquals(1, drugs.size());
        assertEquals( route.getConceptId(), drugs.get(0).getRoute().getConceptId());
    }


    private Order updateOrderWithConcept(int orderId, int conceptId) {
        OrderService orderService = Context.getOrderService();
        Order knownOrder = orderService.getOrder(orderId);
        knownOrder.setConcept(Context.getConceptService().getConcept(conceptId));
        return orderService.saveOrder(knownOrder);
    }

    private Obs updateObsWithConceptAnswer(int obsId, int conceptId) {
        ObsService obsService = Context.getObsService();
        Obs knownAnswerObs = obsService.getObs(obsId);
        knownAnswerObs.setValueCoded(Context.getConceptService().getConcept(conceptId));
        return obsService.saveObs(knownAnswerObs,"");
    }

    private Obs updateObsWithConceptQuestion(int obsId, int conceptId) {
        ObsService obsService = Context.getObsService();
        Obs knownAnswerObs = obsService.getObs(obsId);
        knownAnswerObs.setConcept(Context.getConceptService().getConcept(conceptId));
        return obsService.saveObs(knownAnswerObs,"");
    }
}

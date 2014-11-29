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
package org.openmrs.module.mergeconcepts.api.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.*;
import org.openmrs.api.*;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mergeconcepts.api.MergeConceptsService;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class MergeConceptsServiceImplTest extends BaseModuleContextSensitiveTest {
    int FAVORITE_FOOD_NON_CODED = 19; // name="FAVORITE FOOD, NON-CODED"

    private ConceptService conceptService;
    private MergeConceptsService mergeConceptsService;

    @Before
    public void setUp() throws Exception {
        mergeConceptsService = Context.getService(MergeConceptsService.class);
        conceptService = Context.getConceptService();
        executeDataSet(EXAMPLE_XML_DATASET_PACKAGE_PATH );
    }

    @Test
    public void shouldUpdateChildrenToHaveNewParent() {
        setUpConceptSetTest();

        Concept oldConcept = conceptService.getConcept(23);
        Concept newConcept = conceptService.getConcept(3);

        mergeConceptsService.updateConceptSets(oldConcept, newConcept);

        List<Concept> oldConceptSet = conceptService.getConceptsByConceptSet(oldConcept);
        assertThat(oldConceptSet.size(), is(0));
        List<Concept> newConceptSet = conceptService.getConceptsByConceptSet(newConcept);
        assertThat(newConceptSet.size(), is(4));
    }

    @Test
    public void shouldReplaceOldConceptAsChildToNewConcept() {
        setUpConceptSetTest();

        Concept oldConcept = conceptService.getConcept(20);
        Concept newConcept = conceptService.getConcept(4);

        mergeConceptsService.updateConceptSets(oldConcept,  newConcept);

        List<ConceptSet> conceptSets = conceptService.getSetsContainingConcept(newConcept);
        ConceptSet conceptSet1 = conceptSets.get(0);
        assertThat(conceptSet1.getConceptSet().getId(), is(23));

        ConceptSet conceptSet2 = conceptSets.get(1);
        assertThat(conceptSet2.getConceptSet().getId(), is(3));


    }

    @Test
    public void shouldUpdatePersonAttributeTypeWithForeignKey() {
        PersonService personService = Context.getPersonService();

        PersonAttributeType personAttributeType = personService.getPersonAttributeType(8);

        int oldConceptId = 6;
        int newConceptId = 7;
        personAttributeType.setForeignKey(oldConceptId);
        Concept oldConcept = conceptService.getConcept(oldConceptId);
        Concept newConcept = conceptService.getConcept(newConceptId);

        mergeConceptsService.updatePersonAttributeTypes(oldConcept, newConcept);

        assertThat(personAttributeType.getForeignKey(), is(newConceptId));
    }

    private void setUpConceptSetTest() {
        Concept parentConcept = conceptService.getConcept(3);
        Concept childConcept = conceptService.getConcept(4);

        parentConcept.addSetMember(childConcept);

        conceptService.saveConcept(parentConcept);
    }


    @Test
    public void shouldSetupContext() {
        assertNotNull(Context.getService(MergeConceptsService.class));
    }

    @Test
    public void getObsCount_shouldReturnACountOfQuestionAndAnswerConceptObs()
            throws Exception {
        int knownObsId = 7;
        updateObsWithConceptAnswer(knownObsId, FAVORITE_FOOD_NON_CODED);

        Integer actualServiceObsCount = mergeConceptsService.getObsCount(FAVORITE_FOOD_NON_CODED);

        Integer expectedServiceObsCount = 2;
        assertEquals(expectedServiceObsCount, actualServiceObsCount);
    }

    @Test
    public void updateObs_shouldUpdateObsWithNewConceptInQuestionsAndAnswerConcepts() {
        Obs savedObsWithId10 = updateObsWithConceptAnswer(10, FAVORITE_FOOD_NON_CODED);
        Obs savedObsWithId12 = updateObsWithConceptQuestion(12, FAVORITE_FOOD_NON_CODED);
        int newConceptId = 22;

        Concept knownConcept = conceptService.getConcept(FAVORITE_FOOD_NON_CODED);
        Concept newConcept = conceptService.getConcept(newConceptId);

        mergeConceptsService.updateObs(knownConcept, newConcept);

        assertEquals(new Integer(newConceptId), savedObsWithId10.getValueCoded().getId());
        assertEquals(new Integer(newConceptId), savedObsWithId12.getConcept().getId());
    }

    @Test
    public void updateOrders_shouldUpdateOrdersWithNewConcept() throws Exception {
        int oldConceptId = 88;
        int newConceptId = 792;

        Concept oldConcept = conceptService.getConcept(oldConceptId);
        Concept newConcept = conceptService.getConcept(newConceptId);

        mergeConceptsService.updateOrders(oldConcept,newConcept);

        Order orderWithId4 = Context.getOrderService().getOrder(4);
        Order orderWithId5 = Context.getOrderService().getOrder(4);

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
        String programName = "MDR program";
        String newDescription = conceptService.getConcept(newConceptId).getDescription().toString();

        mergeConceptsService.updatePrograms(Context.getConceptService().getConcept(oldConceptId), Context.getConceptService().getConcept(newConceptId));

        int programId = 2;
        ProgramWorkflowService programService = Context.getProgramWorkflowService();
        Program program = programService.getProgram(programId);

        String updatedName = program.getName();
        String updatedDescription = program.getDescription();
        int updatedConceptId = program.getConcept().getId();

        assertEquals(newConceptId, updatedConceptId);
        assertEquals(programName, updatedName);
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


    @Test
    public void shouldUpdateRouteConceptIdInDrug() {
        Concept oldConceptWithRoute = getConceptFromConceptId(22);
        Concept newConceptWithRoute = getConceptFromConceptId(23);
        Drug drug = getDrugById(11);
        drug.setRoute(oldConceptWithRoute);
        saveDrugToConceptService(drug);

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


    private Concept getConceptFromConceptId(int conceptId) {
        return Context.getConceptService().getConcept(conceptId);
    }


    private Drug saveDrugToConceptService(Drug drug) {
        return Context.getConceptService().saveDrug(drug);
    }


    private Drug getDrugById(int drugId) {
        return Context.getConceptService().getDrug(drugId);
    }

    private Obs updateObsWithConceptAnswer(int obsId, int conceptId) {
        ObsService obsService = Context.getObsService();
        Obs knownAnswerObs = obsService.getObs(obsId);
        knownAnswerObs.setValueCoded(Context.getConceptService().getConcept(conceptId));
        return obsService.saveObs(knownAnswerObs,"Updating observation with new concept answer");
    }


    private Obs updateObsWithConceptQuestion(int obsId, int conceptId) {
        ObsService obsService = Context.getObsService();
        Obs knownAnswerObs = obsService.getObs(obsId);
        knownAnswerObs.setConcept(Context.getConceptService().getConcept(conceptId));
        return obsService.saveObs(knownAnswerObs,"Updating observation with new concept question");
    }
}

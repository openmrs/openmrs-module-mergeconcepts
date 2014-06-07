package org.openmrs.module.mergeconcepts.api.impl;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptSet;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mergeconcepts.api.MergeConceptsService;
import org.openmrs.test.BaseModuleContextSensitiveTest;

import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MergeConceptsServiceImplTest extends BaseModuleContextSensitiveTest {

    private Concept parentConcept;
    private ConceptSet conceptSet;
    private Set<ConceptSet> conceptSets;
    private ConceptService conceptService;
    private MergeConceptsService mergeConceptsService;

    @Before
    public void setUp() throws Exception {
        mergeConceptsService = Context.getService(MergeConceptsService.class);
    }

    @Test
    public void shouldCopyOldConceptSetToNewConceptSet() {
        setUpConceptSetTest();

        Concept oldConcept = conceptService.getConcept(23);
        Concept newConcept = conceptService.getConcept(3);

        mergeConceptsService.updateConceptSets(oldConcept, newConcept);

        List<Concept> oldConceptSet = conceptService.getConceptsByConceptSet(oldConcept);
        assertThat(oldConceptSet.size(), is(0));
        List<Concept> newConceptSet = conceptService.getConceptsByConceptSet(newConcept);
        assertThat(newConceptSet.size(), is(4));
    }

    private void setUpConceptSetTest() {


        conceptService = Context.getConceptService();

        parentConcept = conceptService.getConcept(3);
        parentConcept.setSet(true);

        Concept childConcept = conceptService.getConcept(4);
        parentConcept.addSetMember(childConcept);

        conceptService.saveConcept(parentConcept);
    }

}
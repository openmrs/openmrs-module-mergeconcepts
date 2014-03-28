package org.openmrs.module.mergeconcepts.web.controller;



import javax.servlet.http.HttpSession;

import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptSet;
import org.openmrs.Drug;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Order;
import org.openmrs.PersonAttributeType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.FormService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mergeconcepts.api.MergeConceptsService;
import org.openmrs.util.PrivilegeConstants;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class MergeConceptsManageController extends BaseOpenmrsObject {

    /**
     * Default page from admin link or results page
     *
     * @should do nothing
     */
    @RequestMapping(value = "/module/mergeconcepts/chooseConcepts", method = RequestMethod.GET)
    public void showPage(ModelMap model) {}

    /**
     * Method is called when going back to choose concepts page after an error
     * or from preview page "no, I'm not sure"
     *
     * @should prepopulate concept widgets
     */
    @Authorized(PrivilegeConstants.VIEW_CONCEPTS)
    @RequestMapping(value = "/module/mergeconcepts/chooseConcepts", method = RequestMethod.POST)
    public void chooseConcepts(ModelMap model) {}


    /**
     * Method is called on submitting chooseConcepts form
     *
     * @should display references to oldConcept and newConcept
     */
    @Authorized({PrivilegeConstants.VIEW_CONCEPTS, PrivilegeConstants.VIEW_FORMS, PrivilegeConstants.VIEW_ORDERS,
            PrivilegeConstants.VIEW_PERSON_ATTRIBUTE_TYPES, PrivilegeConstants.VIEW_PROGRAMS})
    @RequestMapping("/module/mergeconcepts/preview")
    public String preview(ModelMap model, @RequestParam(required = false, value = "oldConceptId") Integer oldConceptId,
                          @RequestParam(required = false, value = "newConceptId") Integer newConceptId,
                          HttpSession httpSession) {

        httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);

        //handle less than two concepts
        if (oldConceptId == null || newConceptId == null) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Please choose two concepts and try again");
            return "redirect:chooseConcepts.form";
        }

        ConceptService conceptService = getConceptService();

        Concept oldConcept = conceptService.getConcept(oldConceptId);
        Concept newConcept = conceptService.getConcept(newConceptId);

        //handle conceptIds are the same
        if (oldConceptId.equals(newConceptId)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "The same concept was chosen twice - please try again");
            return "redirect:chooseConcepts.form";
        }

        //handle concepts with different datatypes
        //TODO - unless oldConcept is N/A (what if it's the other way around?) <-- is that right?
        if (!this.hasMatchingDatatypes(oldConcept, newConcept)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Please choose concepts with same datatypes and try again");
            return "redirect:chooseConcepts.form";
        }

        //if both concepts' types are coded, make sure both answer sets are the same
        if (oldConcept.getDatatype().isCoded() && !this.hasCodedAnswers(oldConcept, newConcept)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                    "Concept chosen to be retired has answers that the concept to keep does not have - please try again");
            return "redirect:chooseConcepts.form";
        }

        //if both concepts' types are numeric, make sure absolute high for concept to keep includes absolute high for concept to retire
        if (oldConcept.getDatatype().isNumeric() && !this.hasCorrectAbsoluteHi(oldConceptId, newConceptId)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                    "Absolute high for concept to be retired is greater than absolute high for concept to keep - please try again");
            return "redirect:chooseConcepts.form";
        }

        //if both concepts' types are numeric, make sure absolute low for concept to keep includes absolute low for concept to retire
        if (oldConcept.getDatatype().isNumeric() && !this.hasCorrectAbsoluteLow(oldConceptId, newConceptId)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                    "Absolute low for concept to be retired is less than absolute low for concept to keep - please try again");
            return "redirect:chooseConcepts.form";
        }

        //if both concepts' types are numeric, make sure units are the same
        if (oldConcept.getDatatype().isNumeric() && !this.hasMatchingUnits(oldConceptId, newConceptId)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                    "Concepts you chose have different units - please try again");
            return "redirect:chooseConcepts.form";
        }

        //if both concepts' types are numeric, make sure both precision (y/n)s are the same
        if (oldConcept.getDatatype().isNumeric() && !this.hasMatchingPrecise(oldConceptId, newConceptId)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                    "Concepts do not agree on precise (y/n) - please try again");
            return "redirect:chooseConcepts.form";
        }

        //if both concepts' types are complex, make sure handlers are the same
        if (oldConcept.getDatatype().isComplex() && !this.hasMatchingComplexHandler(oldConceptId, newConceptId)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                    "Complex concepts do not have the same handler - please try again");
            return "redirect:chooseConcepts.form";
        }

        // TODO: Make conceptType Enum
        addConceptDetails(model, oldConceptId, "old");
        addConceptDetails(model, newConceptId, "new");

        return "/module/mergeconcepts/preview";
    }

    /**
     * Method is called after user confirms preview page
     *
     * @should merge concepts
     */
    @Authorized({PrivilegeConstants.EDIT_OBS, PrivilegeConstants.MANAGE_CONCEPTS, PrivilegeConstants.MANAGE_FORMS})
    @RequestMapping("/module/mergeconcepts/executeMerge")
    public String executeMerge(ModelMap model, @RequestParam("oldConceptId") Integer oldConceptId,
                               @RequestParam("newConceptId") Integer newConceptId,
                               HttpSession httpSession) throws APIException {
        MergeConceptsService service = Context.getService(MergeConceptsService.class);
        ConceptService conceptService = getConceptService();

        Concept oldConcept = conceptService.getConcept(oldConceptId);
        Concept newConcept = conceptService.getConcept(newConceptId);

        model.addAttribute("oldConceptId", oldConceptId);
        model.addAttribute("newConceptId", newConceptId);
        model.addAttribute("oldForms", service.getMatchingForms(oldConcept));
        model.addAttribute("newForms", service.getMatchingForms(newConcept));

        try {
            //OBS
            service.updateObs(oldConceptId, newConceptId);

            //FORMS
            service.updateFields(oldConceptId, newConceptId);

            //DRUGS
            this.updateDrugs(oldConcept, newConcept);

            //ORDERS
            service.updateOrders(oldConceptId, newConceptId);

            //PROGRAMS
            service.updatePrograms(oldConceptId, newConceptId);

            //CONCEPT SETS
            this.updateConceptSets(oldConcept, newConcept);

            //CONCEPT ANSWERS
            this.updateConceptAnswers(oldConcept, newConcept);

            //PERSON ATTRIBUTE TYPES
            this.updatePersonAttributeTypes(oldConcept, newConcept);
        } catch (Exception e) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Something went wrong. Exception:" + e);
            return "redirect: chooseConcepts.form";
        }

        String msg = "Converted concept references from " + oldConcept + " to " + newConcept;
        conceptService.retireConcept(oldConcept, msg);

        return "redirect:results.form";
    }

    /**
     * Method is called after executeMerge() is finished
     *
     * @param oldConceptId
     * @should display updated references to oldConcept and newConcept
     */
    @Authorized({PrivilegeConstants.VIEW_CONCEPTS, PrivilegeConstants.VIEW_FORMS})
    @RequestMapping("/module/mergeconcepts/results")
    public void results(ModelMap model, @RequestParam("oldConceptId") Integer oldConceptId,
                        @RequestParam("newConceptId") Integer newConceptId) {
        addConceptDetails(model, newConceptId, "new");
        addConceptDetails(model, oldConceptId, "old");
    }

    /**
     * Called when any page is requested, does not respond to concept search widgets
     *
     * @param newConceptId
     * @return
     * @should set model attribute "newConcept" to concept user wants to keep
     */
    @ModelAttribute("newConcept")
    public Concept getNewConcept(@RequestParam(required = false, value = "newConceptId") String newConceptId) {
        //going to make this use ConceptEditor instead
        return getConceptService().getConcept(newConceptId);
    }

    /**
     * Called when any page is requested, does not respond to concept search widgets
     *
     * @param oldConceptId
     * @return
     * @should set model attribute "oldConcept" to concept user wants to retire
     */
    @ModelAttribute("oldConcept")
    public Concept getOldConcept(@RequestParam(required = false, value = "oldConceptId") String oldConceptId) {
        //going to make this use ConceptEditor instead
        return getConceptService().getConcept(oldConceptId);
    }

    protected void addConceptDetails(ModelMap model, Integer conceptId, String conceptType) {
        ConceptService conceptService = getConceptService();

        Concept concept = conceptService.getConcept(conceptId);

        MergeConceptsService service = Context.getService(MergeConceptsService.class);

        List<String> drugNames = new ArrayList<String>();
        if (this.getMatchingDrugs(concept) != null) {
            for (Drug od : this.getMatchingDrugs(concept)) {
                drugNames.add(od.getFullName(null));
            }
        }
        model.addAttribute(conceptType + "Drugs", drugNames);
        model.addAttribute(conceptType + "ConceptId", conceptId);

        if (service.getMatchingForms(concept) != null)
            model.addAttribute(conceptType + "Forms", service.getMatchingForms(concept));

        if (service.getMatchingOrders(concept) != null)
            model.addAttribute(conceptType + "Orders", service.getMatchingOrders(concept));

        if (service.getMatchingPrograms(concept) != null)
            model.addAttribute(conceptType + "Programs", service.getMatchingPrograms(concept));

        //preview concept answers by id
        List<Integer> conceptAnswerIds = new ArrayList<Integer>();
        if (this.getMatchingConceptAnswers(concept) != null) {
            for (ConceptAnswer a : this.getMatchingConceptAnswers(concept)) {
                conceptAnswerIds.add(a.getConceptAnswerId());
            }
        }
        model.addAttribute(conceptType + "ConceptAnswers", conceptAnswerIds);

        List<Integer> conceptSetIds = new ArrayList<Integer>();
        if (this.getMatchingConceptSets(concept) != null) {
            for (ConceptSet c : this.getMatchingConceptSets(concept)) {
                conceptSetIds.add(c.getConceptSetId());
            }
            if (this.getMatchingConceptSetConcepts(concept) != null) {
                for (ConceptSet cs : this.getMatchingConceptSetConcepts(concept)) {
                    conceptSetIds.add(cs.getConceptSetId());
                }
            }
        }
        model.addAttribute(conceptType + "ConceptSets", conceptSetIds);

        if (this.getMatchingPersonAttributeTypes(concept) != null)
            model.addAttribute(conceptType + "PersonAttributeTypes", this.getMatchingPersonAttributeTypes(concept));

        int obsCount = service.getObsCount(conceptId);
        model.addAttribute(conceptType + "ObsCount", obsCount);
    }


    public void updateDrugs(Concept oldConcept, Concept newConcept) {

        List<Drug> drugsToUpdate = this.getMatchingDrugs(oldConcept);

        if (drugsToUpdate != null) {
            for (Drug d : drugsToUpdate) {
                d.setConcept(newConcept);
            }
        }

        //service.updateDrugRoutes(oldConcept.getConceptId(), newConcept.getConceptId());
        /**
         * TODO need to fix hql in service before this will work
         List<Drug> dosageFormsToUpdate = new ArrayList<Drug>();

         if(service.getDosageForms(oldConcept.getConceptId())!=null){
         for (Integer d : service.getDosageForms(oldConcept.getConceptId())){
         dosageFormsToUpdate.addAll(conceptService.getDrugsByConcept(conceptService.getConcept(d)));
         }
         }*/

    }

    /**
     * @param oldConcept
     * @param newConcept
     */

    public void updateConceptSets(Concept oldConcept, Concept newConcept) {
        //update concept_id
        List<ConceptSet> conceptSetConceptsToUpdate = this.getMatchingConceptSetConcepts(oldConcept);
        if (this.getMatchingConceptSetConcepts(oldConcept) != null) {
            for (ConceptSet csc : conceptSetConceptsToUpdate) {
                csc.setConcept(newConcept);
            }
        }

        //concept_set
        List<ConceptSet> conceptSetsToUpdate = this.getMatchingConceptSets(oldConcept);
        if (this.getMatchingConceptSets(oldConcept) != null) {
            for (ConceptSet cs : conceptSetsToUpdate) {
                cs.setConceptSet(newConcept);
            }
        }
    }

    /**
     * ConceptAnswers contain references to concepts
     *
     * @param oldConcept
     * @param newConcept
     */
    public void updateConceptAnswers(Concept oldConcept, Concept newConcept) {
        List<ConceptAnswer> conceptAnswerQuestionsToUpdate = this.getMatchingConceptAnswerQuestions(oldConcept);

        //update concept_id
        for (ConceptAnswer caq : conceptAnswerQuestionsToUpdate) {
            caq.setConcept(newConcept);
        }

        List<ConceptAnswer> conceptAnswerAnswersToUpdate = this.getMatchingConceptAnswerAnswers(oldConcept);

        //update answer_concepts
        for (ConceptAnswer caa : conceptAnswerAnswersToUpdate) {
            caa.setAnswerConcept(newConcept);
        }
    }


    public void updatePersonAttributeTypes(Concept oldConcept, Concept newConcept) {
        List<PersonAttributeType> matchingPersonAttributeTypes = this.getMatchingPersonAttributeTypes(oldConcept);

        for (PersonAttributeType m : matchingPersonAttributeTypes) {
            m.setForeignKey(newConcept.getConceptId());
        }
    }

    /**
     * getMatchingForms
     *
     * @param concept - the concept to look up
     * @return a list of Forms using the concept as a question or answer, an empty List if none found
     * @should return a list of Forms that use the concept as a question or answer
     * @should return an empty List if no matches
     * @should return an empty list if Concept is null
     */




    protected List<Drug> getMatchingDrugs(Concept concept) {
        return getConceptService().getDrugsByConcept(concept);
    }

    protected List<Drug> getMatchingDrugIngredientDrugs(Concept concept) {
        return Context.getService(MergeConceptsService.class).getDrugsByIngredient(concept);
    }

    protected List<ConceptSet> getMatchingConceptSetConcepts(Concept concept) {
        ConceptService conceptService = getConceptService();
        return conceptService.getSetsContainingConcept(concept);
    }

    protected List<ConceptSet> getMatchingConceptSets(Concept concept) {
        ConceptService conceptService = getConceptService();
        return conceptService.getConceptSetsByConcept(concept);

    }

    protected List<ConceptAnswer> getMatchingConceptAnswerQuestions(Concept concept) {
        List<ConceptAnswer> matchingConceptAnswers = new ArrayList<ConceptAnswer>();
        for (ConceptAnswer ca : concept.getAnswers()) {
            matchingConceptAnswers.add(ca);
        }
        return matchingConceptAnswers;
    }

    protected List<ConceptAnswer> getMatchingConceptAnswerAnswers(Concept concept) {
        ConceptService conceptService = getConceptService();
        List<ConceptAnswer> matchingConceptAnswers = new ArrayList<ConceptAnswer>();

        //Concepts that are questions answered by this concept, and possibly others
        for (Concept c : conceptService.getConceptsByAnswer(concept)) {
            //ConceptAnswers of all possible answers to question concept above
            for (ConceptAnswer a : c.getAnswers()) {

                //only add ConceptAnswers with an answer matching this concept
                if (a.getAnswerConcept().equals(concept)) {
                    matchingConceptAnswers.add(a);
                }
            }
        }

        return matchingConceptAnswers;
    }

    protected List<ConceptAnswer> getMatchingConceptAnswers(Concept concept) {
        List<ConceptAnswer> conceptAnswers = this.getMatchingConceptAnswerAnswers(concept);
        for (ConceptAnswer c : this.getMatchingConceptAnswerQuestions(concept)) {
            conceptAnswers.add(c);
        }
        return conceptAnswers;
    }

    protected List<PersonAttributeType> getMatchingPersonAttributeTypes(Concept concept) {
        PersonService personService = Context.getPersonService();
        List<PersonAttributeType> allPersonAttributeTypes = personService.getAllPersonAttributeTypes();
        List<PersonAttributeType> matchingPersonAttributeTypes = new ArrayList<PersonAttributeType>();

        for (PersonAttributeType p : allPersonAttributeTypes) {
            if (p.getFormat().toLowerCase().contains("concept")) {
                if (p.getForeignKey() != null && p.getForeignKey().equals(concept.getConceptId())) {
                    matchingPersonAttributeTypes.add(p);
                }
            }
        }

        return matchingPersonAttributeTypes;
    }


    /**
     * check if concepts have matching datatypes
     * TO DO - unless oldConcept is N/A (what if it's the other way around?)
     */
    private boolean hasMatchingDatatypes(Concept oldConcept, Concept newConcept) {
        return (oldConcept.getDatatype().equals(newConcept.getDatatype()));
    }

    /**
     * newConcept should have all answers of oldConcept
     */
    private boolean hasCodedAnswers(Concept oldConcept, Concept newConcept) {
        if (newConcept.getAnswers(false) == null) {
            if (oldConcept.getAnswers(false) == null) {
                return true;
            } else return false;
        } else if (oldConcept.getAnswers(false) == null) return true;

        return newConcept.getAnswers(false).containsAll(oldConcept.getAnswers(false));
    }

    private boolean hasCorrectAbsoluteHi(Integer oldConceptId, Integer newConceptId) {
        ConceptService conceptService = getConceptService();

        if (conceptService.getConceptNumeric(newConceptId).getHiAbsolute() == null) {
            return true;
        } else if (conceptService.getConceptNumeric(oldConceptId).getHiAbsolute() == null) {
            return false;
        }

        return (conceptService.getConceptNumeric(oldConceptId).getHiAbsolute() <= conceptService.getConceptNumeric(newConceptId).getHiAbsolute());
    }

    private boolean hasCorrectAbsoluteLow(Integer oldConceptId, Integer newConceptId) {
        ConceptService conceptService = getConceptService();
        if (conceptService.getConceptNumeric(newConceptId).getLowAbsolute() == null) {
            return true;
        } else if (conceptService.getConceptNumeric(oldConceptId).getLowAbsolute() == null) {
            return false;
        }

        return (conceptService.getConceptNumeric(oldConceptId).getLowAbsolute() >= conceptService.getConceptNumeric(newConceptId).getLowAbsolute());

    }

    //if both concepts' types are numeric, make sure units are the same
    private boolean hasMatchingUnits(Integer oldConceptId, Integer newConceptId) {
        ConceptService conceptService = getConceptService();
        if (conceptService.getConceptNumeric(oldConceptId).getUnits() == null)
            return true;
        return conceptService.getConceptNumeric(oldConceptId).getUnits().equals(conceptService.getConceptNumeric(newConceptId).getUnits());
    }


    //if both concepts' types are numeric, make sure both precision (y/n)s are the same
    private boolean hasMatchingPrecise(Integer oldConceptId, Integer newConceptId) {
        ConceptService conceptService = getConceptService();
        return conceptService.getConceptNumeric(oldConceptId).getPrecise().equals(conceptService.getConceptNumeric(newConceptId).getPrecise());
    }

    //if both concepts' types are complex, make sure handlers are the same
    private boolean hasMatchingComplexHandler(Integer oldConceptId, Integer newConceptId) {
        ConceptService conceptService = getConceptService();
        return conceptService.getConceptComplex(oldConceptId).getHandler().equals(conceptService.getConceptComplex(newConceptId).getHandler());
    }


    @Override
    public Integer getId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setId(Integer arg0) {
        // TODO Auto-generated method stub

    }

    protected ConceptService getConceptService() {
        return Context.getConceptService();
    }

}
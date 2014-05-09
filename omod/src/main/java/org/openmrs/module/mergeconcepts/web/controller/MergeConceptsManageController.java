package org.openmrs.module.mergeconcepts.web.controller;



import org.openmrs.*;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
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

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Controller
public class MergeConceptsManageController extends BaseOpenmrsObject {

    private ConceptService conceptService;

    public MergeConceptsManageController() {
        conceptService = Context.getConceptService();
    }

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
        if (isNumeric(oldConcept) && !hasCorrectAbsoluteHi(oldConceptId, newConceptId)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                    "Absolute high for concept to be retired is greater than absolute high for concept to keep - please try again");
            return "redirect:chooseConcepts.form";
        }

        //if both concepts' types are numeric, make sure absolute low for concept to keep includes absolute low for concept to retire
        if (isNumeric(oldConcept) && !hasCorrectAbsoluteLow(oldConceptId, newConceptId)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                    "Absolute low for concept to be retired is less than absolute low for concept to keep - please try again");
            return "redirect:chooseConcepts.form";
        }

        //if both concepts' types are numeric, make sure units are the same
        if (isNumeric(oldConcept) && !hasMatchingUnits(oldConceptId, newConceptId)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                    "Concepts you chose have different units - please try again");
            return "redirect:chooseConcepts.form";
        }

        //if both concepts' types are numeric, make sure both precision (y/n)s are the same
        if (isNumeric(oldConcept) && !hasMatchingPrecise(oldConceptId, newConceptId)) {
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

    private boolean isNumeric(Concept oldConcept) {
        return oldConcept.getDatatype().isNumeric();
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
        MergeConceptsService mergeConceptsService = Context.getService(MergeConceptsService.class);

        Concept oldConcept = conceptService.getConcept(oldConceptId);
        Concept newConcept = conceptService.getConcept(newConceptId);

        model.addAttribute("oldConceptId", oldConceptId);
        model.addAttribute("newConceptId", newConceptId);
        model.addAttribute("oldForms", mergeConceptsService.getMatchingForms(oldConcept));
        model.addAttribute("newForms", mergeConceptsService.getMatchingForms(newConcept));

        try {
            mergeConceptsService.update(oldConceptId, newConceptId, oldConcept, newConcept);

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
        return conceptService.getConcept(newConceptId);
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
        return conceptService.getConcept(oldConceptId);
    }

    protected void addConceptDetails(ModelMap model, Integer conceptId, String conceptType) {
        Concept concept = conceptService.getConcept(conceptId);

        MergeConceptsService service = Context.getService(MergeConceptsService.class);

        List<String> drugNames = new ArrayList<String>();
        if (service.getMatchingDrugsByConcept(concept) != null) {
            for (Drug od : service.getMatchingDrugsByConcept(concept)) {
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
        if (service.getMatchingConceptAnswers(concept) != null) {
            for (ConceptAnswer a : service.getMatchingConceptAnswers(concept)) {
                conceptAnswerIds.add(a.getConceptAnswerId());
            }
        }
        model.addAttribute(conceptType + "ConceptAnswers", conceptAnswerIds);

        List<Integer> conceptSetIds = new ArrayList<Integer>();
        if (service.getMatchingConceptSets(concept) != null) {
            for (ConceptSet c : service.getMatchingConceptSets(concept)) {
                conceptSetIds.add(c.getConceptSetId());
            }
            if (service.getMatchingConceptSetConcepts(concept) != null) {
                for (ConceptSet cs : service.getMatchingConceptSetConcepts(concept)) {
                    conceptSetIds.add(cs.getConceptSetId());
                }
            }
        }
        model.addAttribute(conceptType + "ConceptSets", conceptSetIds);

        if (service.getMatchingPersonAttributeTypes(concept) != null)
            model.addAttribute(conceptType + "PersonAttributeTypes", service.getMatchingPersonAttributeTypes(concept));

        int obsCount = service.getObsCount(conceptId);
        model.addAttribute(conceptType + "ObsCount", obsCount);
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
        if (conceptService.getConceptNumeric(newConceptId).getHiAbsolute() == null) {
            return true;
        } else if (conceptService.getConceptNumeric(oldConceptId).getHiAbsolute() == null) {
            return false;
        }

        return (conceptService.getConceptNumeric(oldConceptId).getHiAbsolute() <= conceptService.getConceptNumeric(newConceptId).getHiAbsolute());
    }

    private boolean hasCorrectAbsoluteLow(Integer oldConceptId, Integer newConceptId) {
        if (conceptService.getConceptNumeric(newConceptId).getLowAbsolute() == null) {
            return true;
        } else if (conceptService.getConceptNumeric(oldConceptId).getLowAbsolute() == null) {
            return false;
        }

        return (conceptService.getConceptNumeric(oldConceptId).getLowAbsolute() >= conceptService.getConceptNumeric(newConceptId).getLowAbsolute());

    }

    //if both concepts' types are numeric, make sure units are the same
    private boolean hasMatchingUnits(Integer oldConceptId, Integer newConceptId) {
        if (conceptService.getConceptNumeric(oldConceptId).getUnits() == null)
            return true;
        return conceptService.getConceptNumeric(oldConceptId).getUnits().equals(conceptService.getConceptNumeric(newConceptId).getUnits());
    }


    //if both concepts' types are numeric, make sure both precision (y/n)s are the same
    private boolean hasMatchingPrecise(Integer oldConceptId, Integer newConceptId) {
        return conceptService.getConceptNumeric(oldConceptId).getPrecise().equals(conceptService.getConceptNumeric(newConceptId).getPrecise());
    }

    //if both concepts' types are complex, make sure handlers are the same
    private boolean hasMatchingComplexHandler(Integer oldConceptId, Integer newConceptId) {
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

}
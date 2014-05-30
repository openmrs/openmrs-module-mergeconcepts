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
import java.util.*;

@Controller
public class MergeConceptsManageController extends BaseOpenmrsObject {

    public ConceptService getConceptService() {
        return Context.getConceptService();
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

        if (hasErrors(oldConceptId, newConceptId, httpSession)) {
            return "redirect:chooseConcepts.form";
        }

        // TODO: Make conceptType Enum
        addConceptDetailsToModel(model, oldConceptId, "old");
        addConceptDetailsToModel(model, newConceptId, "new");

        return "/module/mergeconcepts/preview";
    }

    private boolean hasErrors(Integer oldConceptId, Integer newConceptId, HttpSession httpSession) {

        if (oldConceptId == null || newConceptId == null) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Please choose two concepts and try again");
            return true;
        }

        Concept oldConcept = getConceptService().getConcept(oldConceptId);
        Concept newConcept = getConceptService().getConcept(newConceptId);

        //handle conceptIds are the same
        if (oldConceptId.equals(newConceptId)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "The same concept was chosen twice - please try again");
            return true;
        }

        //handle concepts with different datatypes
        //TODO - unless oldConcept is N/A (what if it's the other way around?) <-- is that right?
        if (!this.hasMatchingDatatypes(oldConcept, newConcept)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Please choose concepts with same datatypes and try again");
            return true;
        }

        //if both concepts' types are coded, make sure both answer sets are the same
        if (oldConcept.getDatatype().isCoded() && !this.hasCodedAnswers(oldConcept, newConcept)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                    "Concept chosen to be retired has answers that the concept to keep does not have - please try again");
            return true;
        }

        //if both concepts' types are numeric, make sure absolute high for concept to keep includes absolute high for concept to retire
        if (isNumeric(oldConcept) && !hasCorrectAbsoluteHi(oldConceptId, newConceptId)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                    "Absolute high for concept to be retired is greater than absolute high for concept to keep - please try again");
            return true;
        }

        //if both concepts' types are numeric, make sure absolute low for concept to keep includes absolute low for concept to retire
        if (isNumeric(oldConcept) && !hasCorrectAbsoluteLow(oldConceptId, newConceptId)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                    "Absolute low for concept to be retired is less than absolute low for concept to keep - please try again");
            return true;
        }

        //if both concepts' types are numeric, make sure units are the same
        if (isNumeric(oldConcept) && !hasMatchingUnits(oldConceptId, newConceptId)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                    "Concepts you chose have different units - please try again");
            return true;
        }

        //if both concepts' types are numeric, make sure both precision (y/n)s are the same
        if (isNumeric(oldConcept) && !hasMatchingPrecise(oldConceptId, newConceptId)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                    "Concepts do not agree on precise (y/n) - please try again");
            return true;
        }

        //if both concepts' types are complex, make sure handlers are the same
        if (oldConcept.getDatatype().isComplex() && !this.hasMatchingComplexHandler(oldConceptId, newConceptId)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                    "Complex concepts do not have the same handler - please try again");
            return true;
        }
        return false;
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

        Concept oldConcept = getConceptService().getConcept(oldConceptId);
        Concept newConcept = getConceptService().getConcept(newConceptId);

        model.addAttribute("oldConceptId", oldConceptId);
        model.addAttribute("newConceptId", newConceptId);
        model.addAttribute("oldForms", mergeConceptsService.getMatchingForms(oldConcept));
        model.addAttribute("newForms", mergeConceptsService.getMatchingForms(newConcept));

        return mergeConcepts(httpSession, mergeConceptsService, oldConcept, newConcept);
    }

    private String mergeConcepts(HttpSession httpSession, MergeConceptsService mergeConceptsService, Concept oldConcept, Concept newConcept) {
        String view;
        try {
            mergeConceptsService.update(oldConcept, newConcept);
            String msg = "Converted concept references from " + oldConcept + " to " + newConcept;
            getConceptService().retireConcept(oldConcept, msg);

            view = "redirect:results.form";

        } catch (Exception e) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Something went wrong. Exception:" + e);
            view = "redirect: chooseConcepts.form";
        }
        return view;
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
        addConceptDetailsToModel(model, newConceptId, "new");
        addConceptDetailsToModel(model, oldConceptId, "old");
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

    protected void addConceptDetailsToModel(ModelMap model, Integer conceptId, String conceptType) {
        Concept concept = getConceptService().getConcept(conceptId);

        MergeConceptsService service = Context.getService(MergeConceptsService.class);

        Map<String, Object> attributes = getAttributes(conceptType, concept, service);

        for (String s : attributes.keySet()) {
            model.addAttribute(s,attributes.get(s));
        }
    }

    private Map<String, Object> getAttributes(String conceptType, Concept concept, MergeConceptsService service) {
        Map<String, Object> attributes = new HashMap<String, Object>();

        attributes.put(conceptType + "ConceptId", concept.getId());

        List<String> drugNames = new ArrayList<String>();
        addDrugNames(concept, service, drugNames);
        attributes.put(conceptType + "Drugs", drugNames);

        //preview concept answers by id
        List<Integer> conceptAnswerIds = new ArrayList<Integer>();

        int obsCount = service.getObsCount(concept.getId());
        attributes.put(conceptType + "ObsCount", obsCount);

        if (service.getMatchingConceptAnswers(concept) != null) {
            for (ConceptAnswer a : service.getMatchingConceptAnswers(concept)) {
                conceptAnswerIds.add(a.getConceptAnswerId());
            }
        }

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

        attributes.put(conceptType + "ConceptSets", conceptSetIds);
        attributes.put(conceptType + "ConceptAnswers", conceptAnswerIds);

        if (service.getMatchingForms(concept) != null) {
            attributes.put(conceptType + "Forms", service.getMatchingForms(concept));
        }

        if (service.getMatchingOrders(concept) != null){
            attributes.put(conceptType + "Orders", service.getMatchingOrders(concept));
        }

        if (service.getMatchingPrograms(concept) != null){
            attributes.put(conceptType + "Programs", service.getMatchingPrograms(concept));
        }

        if (service.getMatchingPersonAttributeTypes(concept) != null) {
            attributes.put(conceptType + "PersonAttributeTypes", service.getMatchingPersonAttributeTypes(concept));
        }


        return attributes;
    }

    private void addDrugNames(Concept concept, MergeConceptsService service, List<String> drugNames) {
        if (service.getMatchingDrugsByConcept(concept) != null) {
            for (Drug od : service.getMatchingDrugsByConcept(concept)) {
                drugNames.add(od.getFullName(null));
            }
        }
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
        if (getConceptService().getConceptNumeric(newConceptId).getHiAbsolute() == null) {
            return true;
        } else if (getConceptService().getConceptNumeric(oldConceptId).getHiAbsolute() == null) {
            return false;
        }

        return (getConceptService().getConceptNumeric(oldConceptId).getHiAbsolute() <= getConceptService().getConceptNumeric(newConceptId).getHiAbsolute());
    }

    private boolean hasCorrectAbsoluteLow(Integer oldConceptId, Integer newConceptId) {
        if (getConceptService().getConceptNumeric(newConceptId).getLowAbsolute() == null) {
            return true;
        } else if (getConceptService().getConceptNumeric(oldConceptId).getLowAbsolute() == null) {
            return false;
        }

        return (getConceptService().getConceptNumeric(oldConceptId).getLowAbsolute() >= getConceptService().getConceptNumeric(newConceptId).getLowAbsolute());

    }

    //if both concepts' types are numeric, make sure units are the same
    private boolean hasMatchingUnits(Integer oldConceptId, Integer newConceptId) {
        if (getConceptService().getConceptNumeric(oldConceptId).getUnits() == null)
            return true;
        return getConceptService().getConceptNumeric(oldConceptId).getUnits().equals(getConceptService().getConceptNumeric(newConceptId).getUnits());
    }


    //if both concepts' types are numeric, make sure both precision (y/n)s are the same
    private boolean hasMatchingPrecise(Integer oldConceptId, Integer newConceptId) {
        return getConceptService().getConceptNumeric(oldConceptId).getPrecise().equals(getConceptService().getConceptNumeric(newConceptId).getPrecise());
    }

    //if both concepts' types are complex, make sure handlers are the same
    private boolean hasMatchingComplexHandler(Integer oldConceptId, Integer newConceptId) {
        return getConceptService().getConceptComplex(oldConceptId).getHandler().equals(getConceptService().getConceptComplex(newConceptId).getHandler());
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
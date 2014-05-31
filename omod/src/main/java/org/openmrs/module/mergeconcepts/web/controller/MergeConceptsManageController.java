package org.openmrs.module.mergeconcepts.web.controller;

import org.openmrs.*;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mergeconcepts.api.MergeConceptsService;
import org.openmrs.module.mergeconcepts.api.impl.PreviewErrorValidation;
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

        PreviewErrorValidation previewErrorValidation = Context.getService(PreviewErrorValidation.class);
        httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);

        if (previewErrorValidation.hasErrors(oldConceptId, newConceptId, httpSession)) {
            return "redirect:chooseConcepts.form";
        }

        // TODO: Make conceptType Enum
        addConceptDetailsToModel(model, oldConceptId, "old");
        addConceptDetailsToModel(model, newConceptId, "new");

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

        Map<String, Object> attributes = service.getAttributes(conceptType, concept);

        for (String s : attributes.keySet()) {
            model.addAttribute(s,attributes.get(s));
        }
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
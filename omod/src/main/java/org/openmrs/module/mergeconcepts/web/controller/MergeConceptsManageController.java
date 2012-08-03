package org.openmrs.module.mergeconcepts.web.controller;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.openmrs.Concept;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mergeconcepts.api.MergeConceptsService;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The controller.
 */
@Controller
public class MergeConceptsManageController {
	
	private Logger log = Logger.getLogger(MergeConceptsManageController.class);
	
	/**
	 * Called when any page is requested, does not respond to concept search widgets
	 * @should set model attribute "newConcept" to concept user wants to keep
	 * @param newConceptId
	 * @return
	 */
	@ModelAttribute("newConcept")
	public Concept getNewConcept(@RequestParam(required=false, value="newConceptId") String newConceptId){
		//going to make this use ConceptEditor instead
		Concept newConcept = Context.getConceptService().getConcept(newConceptId);
		return newConcept;
	}
	
	/**
	 * Called when any page is requested, does not respond to concept search widgets
	 * @should set model attribute "oldConcept" to concept user wants to retire
	 * @param oldConceptId
	 * @return
	 */
	@ModelAttribute("oldConcept")
	public Concept getOldConcept(@RequestParam(required=false, value="oldConceptId") String oldConceptId){
		//going to make this use ConceptEditor instead
		Concept oldConcept = Context.getConceptService().getConcept(oldConceptId);
		return oldConcept;
	}
	
	/**
	 * getMatchingObs
	 * @param concept - the concept to look up
	 * @return a list of Obs using the concept as a question or answer, an empty List if none found
	 * @should return a list of Obs that use the concept as a question or answer
	 * @should return an empty List if no matches
	 * @should return an empty list if Concept is null
	 */
	protected List<Obs> getMatchingObs(Concept concept){
		ObsService obsService = Context.getObsService();

		List<Concept> conceptList = new ArrayList<Concept>();
		conceptList.add(concept);
		
		List<Obs> result = new ArrayList<Obs>();
		
		if( concept == null )
			return result; //TODO recondisder error handling strategy here
		
		//answer concept
		List<Obs> obsFound = obsService.getObservations(null, null, null, conceptList, null, null, null, null, null, null, null,
				false);
		if(obsFound!=null){
			result.addAll(obsFound);
			log.info("Found " + obsFound.size() + " obs with answers concept Id " + concept.getConceptId());
		}
		
		//question concept
		obsFound = obsService.getObservations(null, null,  conceptList, null, null, null, null, null, null, null, null,
				false);
		if(obsFound!=null){
			result.addAll(obsFound);
			log.info("Found " + obsFound.size() + " obs with questions concept Id " + concept.getConceptId());

		}
		
		return result;
	}

	/**
	 * getMatchingForms
	 * @param concept - the concept to look up
	 * @return a list of Forms using the concept as a question or answer, an empty List if none found
	 * @should return a list of Forms that use the concept as a question or answer
	 * @should return an empty List if no matches
	 * @should return an empty list if Concept is null
	 */
	protected Set<Form> getMatchingForms(Concept concept){
		
		Set<FormField> formFields = this.getMatchingFormFields(concept);
		Set<Form> conceptForms = new HashSet<Form>();

		for(FormField f : formFields){
			
			//forms that ref oldConcept
			if(!f.getForm().equals(null)){	
				conceptForms.add(f.getForm());
			}

		}
		
		return conceptForms;
	}
	
	protected Set<FormField> getMatchingFormFields(Concept concept){
		Set<FormField> formFields = new HashSet<FormField>();
		
		List<Form> allForms = Context.getFormService().getFormsContainingConcept(concept); //instead of Context.getFormService().getAllForms();
		
		//FormFields with old concept (might be a better way to do this)
		for(Form f : allForms){
			formFields.add(Context.getFormService().getFormField(f, concept));
		}
		
		return formFields;
	}
	
	/**
	 * TO DO
	 */
	public void updateFormFields(Set<FormField> formFields, Concept concept){
		for(FormField f : formFields){
			Field field = f.getField();
			field.setConcept(concept);
			Context.getFormService().saveField(field);			
		}
	}
	
	/**
	 * @should generate fresh lists of references to new concept
	 */
	public Map<String, List> generateNewReferenceLists(Integer newConceptId){

		Map<String, List> newConceptRefs = new HashMap<String, List>();
		Concept concept = Context.getConceptService().getConcept(newConceptId);
		
		
		//OBS
		//ObsService obsService = Context.getObsService(); //ObsEditor?
		
		List<Obs> newConceptObs;
		//newConceptObs = obsService.getObservationsByPersonAndConcept(null, getNewConcept(newConceptId));
		newConceptObs = this.getMatchingObs(concept);
		newConceptRefs.put("obs", newConceptObs);
		log.info("newObsCount = "+newConceptRefs.get("obs").size());
		
		//FORMS
		//Everything else
		
		return newConceptRefs;		
	}
	
	/**
	 * @should generate fresh lists of references to old concept
	 */
	public Map<String, List> generateOldReferenceLists(Integer oldConceptId){
		
		Map<String, List> oldConceptRefs = new HashMap<String, List>();
		Concept concept = Context.getConceptService().getConcept(oldConceptId);
		
		//OBS
		//ObsService obsService = Context.getObsService(); //ObsEditor?
		
		List<Obs> oldConceptObs;
		//oldConceptObs = obsService.getObservationsByPersonAndConcept(null, concept);
		oldConceptObs = this.getMatchingObs(concept);
		oldConceptRefs.put("obs", oldConceptObs);
		log.info("newObsCount = "+oldConceptRefs.get("obs").size());
		
		//FORMS
		//Everything else
		
		return oldConceptRefs;
		
		//FORMS
	}
	
	/**
	 * Default page from admin link or results page
	 * @should do nothing
	 * @param map
	 */
	@RequestMapping(value="/module/mergeconcepts/chooseConcepts",
			method=RequestMethod.GET)
	public void showPage(ModelMap model) {
		//return "chooseConcepts";
			
	}
	
	/**
	 * Method is called when going back to choose concepts page after an error
	 * or from preview page "no, I'm not sure"
	 * @should prepopulate concept widgets
	 */
	@RequestMapping(value="/module/mergeconcepts/chooseConcepts", 
			method=RequestMethod.POST)
	public void chooseConcepts(ModelMap model){
		
	}
	
	/**
	 * Method is called on submitting chooseConcepts form
	 * @should display references to oldConcept and newConcept
	 * @param map
	 */
	@RequestMapping("/module/mergeconcepts/preview")
	public String preview(ModelMap model, @RequestParam(required=false, value= "oldConceptId") Integer oldConceptId,
										  @RequestParam(required=false, value= "newConceptId") Integer newConceptId,
										  HttpSession httpSession) {
		
		httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);
		
		ConceptService conceptService = Context.getConceptService();
		
		Concept oldConcept = conceptService.getConcept(oldConceptId); 
		Concept newConcept = conceptService.getConcept(newConceptId);
		
		model.addAttribute("oldConceptId", oldConceptId);
		model.addAttribute("newConceptId", newConceptId);
		model.addAttribute("oldForms", this.getMatchingForms(oldConcept));
		model.addAttribute("newForms", this.getMatchingForms(newConcept));
		
		
		//handle less than two concepts
		if(oldConceptId.equals(null) || newConceptId.equals(null)){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Please choose both concept ids and try again");
			return "redirect:chooseConcepts.form";
		}
		
		//handle conceptIds are the same
		if(oldConceptId.equals(newConceptId)){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Please do not choose the same concept id twice and try again");
			return "redirect:chooseConcepts.form";
		}
		
		//handle concepts with different datatypes
		if(!(oldConcept.getDatatype().equals(newConcept.getDatatype()))){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Please choose concepts with similar datatypes and try again");
			return "redirect:chooseConcepts.form";
		}
		
		MergeConceptsService service = Context.getService(MergeConceptsService.class);
		
		int newObsCount = service.getObsCount(newConceptId);
		int oldObsCount = service.getObsCount(oldConceptId);
		
		Map<String, List> newConceptRefs= generateNewReferenceLists(newConceptId);
		Map<String, List> oldConceptRefs= generateOldReferenceLists(oldConceptId);
		
		model.addAttribute("newObsCount", newObsCount);
		model.addAttribute("oldObsCount", oldObsCount);
		
		return "";
		
	}
	
	/**
	 * Method is called after user confirms preview page
	 * @should merge concepts
	 * @param map
	 */
	//@Authorized(value = {"Add Observations","Edit Observations"})
	@RequestMapping("/module/mergeconcepts/executeMerge")
	public String executeMerge(ModelMap model, @RequestParam("oldConceptId") Integer oldConceptId,
											   @RequestParam("newConceptId") Integer newConceptId, 
											   HttpSession httpSession) throws APIException {
		
		MergeConceptsService service = Context.getService(MergeConceptsService.class);
		
		model.addAttribute("oldConceptId", oldConceptId);
		model.addAttribute("newConceptId", newConceptId);
		
		try {
			//OBS
			service.updateObs(oldConceptId, newConceptId);
		
			//FORMS
		
		}
		
		catch (Exception e){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Something went wrong. Exception:" + e);
			return "redirect: chooseConcepts.form";
		}
		
		//conceptService.retireConcept(oldConcept, msg);
			
		return "redirect:results.form";
	}
	
	/**
	 * Method is called after executeMerge() is finished
	 * @should display updated references to oldConcept and newConcept
	 * @param map
	 */
	@RequestMapping("/module/mergeconcepts/results")
	public void results(ModelMap model, @RequestParam("oldConceptId") Integer oldConceptId,
									    @RequestParam("newConceptId") Integer newConceptId) {
		
		//redirect if something went wrong
		
		Map<String, List> newConceptRefs= generateNewReferenceLists(newConceptId);
		Map<String, List> oldConceptRefs= generateOldReferenceLists(oldConceptId);
		
		model.addAttribute("newObsCount", newConceptRefs.get("obs").size());
		model.addAttribute("oldObsCount", oldConceptRefs.get("obs").size());
	}
	
}
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
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.FormService;
import org.openmrs.api.ObsService;
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
	 * TO DO - test logic here
	 */
	public void updateFormFields(Concept oldConcept, Concept newConcept){
		
		Set<FormField> formFieldsToUpdate = this.getMatchingFormFields(oldConcept);
		
		FormService formService = Context.getFormService();
		
		for(FormField f : formFieldsToUpdate){
			
			//update
			Field field = f.getField();
			field.setConcept(newConcept);
			
			//save
			formService.saveField(field);
			f.setField(field);
			formService.saveFormField(f);
			formService.saveForm(f.getForm());
			
		}
	}
	
	/**
	 * TO DO - might not need this method
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
	 * TO DO - might not need this method
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
	 * Default page from admin link or results page
	 * @should do nothing
	 * @param map
	 
	@RequestMapping(value="/module/mergeconcepts/experiment",
			method=RequestMethod.GET)
	public String showPreview(ModelMap model) {
		return "preview";
			
	}*/
	
	
	/**
	 * Method is called when going back to choose concepts page after an error
	 * or from preview page "no, I'm not sure"
	 * @should prepopulate concept widgets
	 */
	@Authorized(PrivilegeConstants.VIEW_CONCEPTS) 
	@RequestMapping(value="/module/mergeconcepts/chooseConcepts", 
			method=RequestMethod.POST)
	public void chooseConcepts(ModelMap model){
		
	}
	
	/**
	 * Method is called on submitting chooseConcepts form
	 * @should display references to oldConcept and newConcept
	 * @param map
	 */
	@Authorized( {PrivilegeConstants.VIEW_CONCEPTS, PrivilegeConstants.VIEW_FORMS})
	@RequestMapping("/module/mergeconcepts/preview")
	public String preview(ModelMap model, @RequestParam(required=false, value= "oldConceptId") Integer oldConceptId,
										  @RequestParam(required=false, value= "newConceptId") Integer newConceptId,
										  HttpSession httpSession) {
		
		httpSession.removeAttribute(WebConstants.OPENMRS_ERROR_ATTR);
		
		//handle less than two concepts
		if(oldConceptId==null || newConceptId==null){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Please choose two concepts and try again");
			return "redirect:chooseConcepts.form";
		}
		
		ConceptService conceptService = Context.getConceptService();

		Concept oldConcept = conceptService.getConcept(oldConceptId); 
		Concept newConcept = conceptService.getConcept(newConceptId);

		//handle conceptIds are the same
		if(oldConceptId.equals(newConceptId)){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Please do not choose the same concept id twice and try again");
			return "redirect:chooseConcepts.form";
		}

		//handle concepts with different datatypes
		//TO DO - unless oldConcept is N/A (what if it's the other way around?)
		if(!(oldConcept.getDatatype().equals(newConcept.getDatatype()))){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Please choose concepts with same datatypes and try again");
			return "redirect:chooseConcepts.form";
		}

		//if both concepts' types are coded, make sure both answer sets are the same
		if(oldConcept.getDatatype().isCoded() && !newConcept.getAnswers(false).containsAll(oldConcept.getAnswers(false))){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, 
					"Concept chosen to be retired has answers that the concept to keep does not have - please try again");
			return "redirect:chooseConcepts.form";
		}

		//if both concepts' types are numeric, make sure absolute high for concept to keep includes absolute high for concept to retire
		if(oldConcept.getDatatype().isNumeric() && conceptService.getConceptNumeric(oldConceptId).getHiAbsolute() > conceptService.getConceptNumeric(newConceptId).getHiAbsolute()){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, 
					"Absolute high for concept to be retired is greater than absolute high for concept to keep - please try again");
			return "redirect:chooseConcepts.form";
		}
		
		//if both concepts' types are numeric, make sure absolute low for concept to keep includes absolute low for concept to retire
		if(oldConcept.getDatatype().isNumeric() && conceptService.getConceptNumeric(oldConceptId).getLowAbsolute() < conceptService.getConceptNumeric(newConceptId).getLowAbsolute()){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, 
					"Absolute low for concept to be retired is less than absolute low for concept to keep - please try again");
			return "redirect:chooseConcepts.form";
		}
		
		//if both concepts' types are numeric, make sure units are the same
		if(oldConcept.getDatatype().isNumeric() && conceptService.getConceptNumeric(oldConceptId).getUnits().equals(conceptService.getConceptNumeric(newConceptId).getUnits())){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, 
					"The concepts you chose have different units - please try again");
			return "redirect:chooseConcepts.form";
		}
		
		//if both concepts' types are numeric, make sure both ranges, units, and precision (y/n)s are handled

		
		//if both concepts' types are complex, make sure handlers are the same
		if(oldConcept.getDatatype().isComplex() && !conceptService.getConceptComplex(oldConceptId).getHandler().equals(conceptService.getConceptComplex(newConceptId).getHandler())){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, 
					"Complex concepts do not have the same handler - please try again");
			return "redirect:chooseConcepts.form";
		}
		
		model.addAttribute("oldConceptId", oldConceptId);
		model.addAttribute("newConceptId", newConceptId);
		model.addAttribute("oldForms", this.getMatchingForms(oldConcept));
		model.addAttribute("newForms", this.getMatchingForms(newConcept));
		
		MergeConceptsService service = Context.getService(MergeConceptsService.class);
		
		int newObsCount = service.getObsCount(newConceptId);
		int oldObsCount = service.getObsCount(oldConceptId);
		
		//Map<String, List> newConceptRefs= generateNewReferenceLists(newConceptId);
		//Map<String, List> oldConceptRefs= generateOldReferenceLists(oldConceptId);
		
		model.addAttribute("newObsCount", newObsCount);
		model.addAttribute("oldObsCount", oldObsCount);
		
		return "/module/mergeconcepts/preview";
		
		
	}
	
	/**
	 * Method is called after user confirms preview page
	 * @should merge concepts
	 * @param map
	 */
	@Authorized( {PrivilegeConstants.EDIT_OBS, PrivilegeConstants.MANAGE_CONCEPTS, PrivilegeConstants.MANAGE_FORMS})
	@RequestMapping("/module/mergeconcepts/executeMerge")
	public String executeMerge(ModelMap model, @RequestParam("oldConceptId") Integer oldConceptId,
											   @RequestParam("newConceptId") Integer newConceptId, 
											   HttpSession httpSession) throws APIException {
		
		MergeConceptsService service = Context.getService(MergeConceptsService.class);
		
		ConceptService conceptService = Context.getConceptService();
		
		Concept oldConcept = conceptService.getConcept(oldConceptId); 
		Concept newConcept = conceptService.getConcept(newConceptId);
		
		model.addAttribute("oldConceptId", oldConceptId);
		model.addAttribute("newConceptId", newConceptId);
		model.addAttribute("oldForms", this.getMatchingForms(oldConcept));
		model.addAttribute("newForms", this.getMatchingForms(newConcept));
		
		try {
			//OBS
			service.updateObs(oldConceptId, newConceptId);
		
			//FORMS
			this.updateFormFields(oldConcept, newConcept);
		
		}
		
		catch (Exception e){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Something went wrong. Exception:" + e);
			return "redirect: chooseConcepts.form";
		}
		
		String msg = "Converted concept references from " + oldConcept + " to " + newConcept;
		conceptService.retireConcept(oldConcept, msg);
			
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
		
		ConceptService conceptService = Context.getConceptService();
		
		Concept oldConcept = conceptService.getConcept(oldConceptId); 
		Concept newConcept = conceptService.getConcept(newConceptId);
		
		Map<String, List> newConceptRefs= generateNewReferenceLists(newConceptId);
		Map<String, List> oldConceptRefs= generateOldReferenceLists(oldConceptId);
		
		model.addAttribute("newObsCount", newConceptRefs.get("obs").size());
		model.addAttribute("oldObsCount", oldConceptRefs.get("obs").size());
		model.addAttribute("oldForms", this.getMatchingForms(oldConcept));
		model.addAttribute("newForms", this.getMatchingForms(newConcept));
	}
	
}
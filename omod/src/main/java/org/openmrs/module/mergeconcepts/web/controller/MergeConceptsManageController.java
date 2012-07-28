package org.openmrs.module.mergeconcepts.web.controller;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.APIException;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
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
				true);
		if(obsFound!=null){
			result.addAll(obsFound);
			log.info("Found " + obsFound.size() + " obs with answers concept Id " + concept.getConceptId());
		}
		
		//question concept
		obsFound = obsService.getObservations(null, null,  conceptList, null, null, null, null, null, null, null, null,
				true);
		if(obsFound!=null){
			result.addAll(obsFound);
			log.info("Found " + obsFound.size() + " obs with questions concept Id " + concept.getConceptId());

		}
		
		return result;
	}

	/**
	 * @should generate fresh lists of references to new concept
	 */
	public Map<String, List> generateNewReferenceLists(String newConceptId){

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
	public Map<String, List> generateOldReferenceLists(String oldConceptId){
		
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
			
	}
	
	/**
	 * Method is called when going back to choose concepts page after an error
	 * or from preview page "no, I'm not sure"
	 * @should (eventually) prepopulate concept widgets
	 */
	@RequestMapping(value="/module/mergeconcepts/chooseConcepts", 
			method=RequestMethod.POST)
	public void chooseConcepts(){
		//were concepts submitted? is the concept being kept non-retired? etc
		
		
	}
	
	/**
	 * Method is called on submitting chooseConcepts form
	 * @should display references to oldConcept and newConcept
	 * @param map
	 */
	@RequestMapping("/module/mergeconcepts/preview")
	public void preview(ModelMap model, @RequestParam("oldConceptId") String oldConceptId,
										@RequestParam("newConceptId") String newConceptId) {
		
		//were concepts submitted? is the concept being kept non-retired? etc. if not, redirect
		
		Map<String, List> newConceptRefs= generateNewReferenceLists(newConceptId);
		Map<String, List> oldConceptRefs= generateOldReferenceLists(oldConceptId);
		
		model.addAttribute("newObsCount", newConceptRefs.get("obs").size());
		model.addAttribute("oldObsCount", oldConceptRefs.get("obs").size());
		
	}
	
	/**
	 * Method is called after user confirms preview page
	 * @should merge concepts
	 * @param map
	 */
	@RequestMapping("/module/mergeconcepts/executeMerge")
	public String executeMerge(ModelMap model, @RequestParam("oldConceptId") String oldConceptId,
											   @RequestParam("newConceptId") String newConceptId) throws APIException {
		
		Concept oldConcept = Context.getConceptService().getConcept(oldConceptId); 
		Concept newConcept = Context.getConceptService().getConcept(newConceptId);

		String msg = "Converted concept references from " + oldConcept + " to " + newConcept;

		List<Obs> obsToConvert = this.getMatchingObs(oldConcept);
		ObsService obsService = Context.getObsService();
		for(Obs o: obsToConvert){
			o.setConcept(newConcept);
			obsService.saveObs(o, msg); //problème ici
		}
		
		model.addAttribute("oldConceptId", oldConceptId);
		model.addAttribute("newConceptId", newConceptId);
			
		//retire oldConcept
			
		return "redirect:results.form";
	}
	
	/**
	 * Method is called after executeMerge() is finished
	 * @should display updated references to oldConcept and newConcept
	 * @param map
	 */
	@RequestMapping("/module/mergeconcepts/results")
	public void results(ModelMap model, @RequestParam("oldConceptId") String oldConceptId,
									    @RequestParam("newConceptId") String newConceptId) {
		
		//redirect if something went wrong
		
		Map<String, List> newConceptRefs= generateNewReferenceLists(newConceptId);
		Map<String, List> oldConceptRefs= generateOldReferenceLists(oldConceptId);
		
		model.addAttribute("newObsCount", newConceptRefs.get("obs").size());
		model.addAttribute("oldObsCount", oldConceptRefs.get("obs").size());
	}
	
}

/**
 * Pieces of code I might want later...
 * 
 * @param map
 * @param oldConceptId
 * @param newConceptId
 * @param httpSession

@RequestMapping(value="/module/mergeconcepts/chooseConcepts", 
				method=RequestMethod.POST)
public void afterPageSubmission(ModelMap map, 
		@RequestParam("oldConceptId") Integer oldConceptId,
		@RequestParam("newConceptId") Integer newConceptId,
		@RequestParam(required=false, value="back") Boolean back,
		HttpSession httpSession) {
	
	//??
	@ModelAttribute
	public Concept getNewConcept(@RequestParam(required=false, value="newConceptId") Concept newConcept){
		return newConcept;
	}

	@ModelAttribute
	public Concept getOldConcept(@RequestParam(required=false, value="oldConceptId") Concept oldConcept){
		return oldConcept;
	}
	
	model.addAttribute("oldConcept", Context.getConceptService().getConcept(oldConceptId));
	model.addAttribute("newConcept", Context.getConceptService().getConcept(newConceptId));
	
	
	Concept oldConcept = Context.getConceptService().getConcept(oldConceptId); 
	Concept newConcept = Context.getConceptService().getConcept(newConceptId);
	
	
	
	
	List<Obs> obsToConvert;
	ObsService obsService = Context.getObsService();
	obsToConvert = obsService.getObservationsByPersonAndConcept(null, oldConcept);
	
	String msg = "Converted question concept from " + oldConcept + " to " + newConcept;
	
	Integer count = 0;
	for (Obs o : obsToConvert) {
		count = count + 1;
		o.setConcept(newConcept);
		obsService.saveObs(o, msg);
	}
	
	/*
	Set<FormField> formFields1 = new HashSet<FormField>();
	
	List<Form> allForms = Context.getFormService().getAllForms();
	Set<Form> oldConceptForms = new HashSet<Form>();
	
	//FormFields with old concept (might be a better way to do this)
	for(Form f : allForms){
		formFields1.add(Context.getFormService().getFormField(f, oldConcept));
	}		
	
	//update fields
	for(FormField f : formFields1){
		//forms that ref oldConcept
		oldConceptForms.add(f.getForm());
		
		//update fields
		f.getField().setConcept(newConcept);
		Context.getFormService().saveField(f.getField());
	}
	
	//change message
	httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Obs converted successfully. total converted: " + count); //+ " Rebuild all forms that used this concept: " + oldConceptForms);
	
}*/

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
 
package org.openmrs.module.mergeconcepts.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The main controller.

@Controller
public class  MergeConceptsManageController {
	
	protected final Log log = LogFactory.getLog(getClass());
	

	
	
	@RequestMapping (value="/module/mergeconcepts/chooseConceptsToMerge", method=RequestMethod.GET)
	public void showForm(){
		
	}
	
	@RequestMapping(value = "/module/mergeconcepts/manage", method = RequestMethod.GET)
	public void manage(ModelMap model) {
		model.addAttribute("user", Context.getAuthenticatedUser());
	}
	
			//method getForms() is in FormFields.java
		for (Field f : fields) {
			f.setConcept(newConcept);
			if (f.getForms() != null)
				formsNeedingRebuilding.addAll(f.getForms());
			Context.getFormService().saveField(f);
		}
}*/

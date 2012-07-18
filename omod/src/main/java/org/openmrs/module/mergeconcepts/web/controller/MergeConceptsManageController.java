package org.openmrs.module.mergeconcepts.web.controller;


import org.apache.log4j.Logger;
import org.openmrs.Concept;
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
	 * @should 
	 * @param map
	 */
	@RequestMapping(value="/module/mergeconcepts/preview", method=RequestMethod.POST)
	public void preview(ModelMap model, @RequestParam("oldConceptId") Integer oldConceptId,
			@RequestParam("newConceptId") Integer newConceptId) {
		model.addAttribute("oldConcept", Context.getConceptService().getConcept(oldConceptId));
		model.addAttribute("newConcept", Context.getConceptService().getConcept(newConceptId));
		
		//were concepts submitted? is the concept being kept non-retired? etc. if not, redirect
		//add attributes?
	}
	
	/**
	 * Method is called after user confirms preview page
	 * @should merge concepts
	 * @param map
	 */
	@RequestMapping("/module/mergeconcepts/executeMerge")
	public String executeMerge(ModelMap map) {
			//ask for conceptIds
			//merge!
			//retire oldConcept
			return "redirect:results.form";
	}
	
	/**
	 * Method is called after executeMerge() is finished
	 * @param map
	 */
	@RequestMapping("/module/mergeconcepts/results")
	public void results(ModelMap map) {
		
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

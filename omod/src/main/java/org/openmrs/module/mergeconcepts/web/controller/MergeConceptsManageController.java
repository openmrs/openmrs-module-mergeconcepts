package org.openmrs.module.mergeconcepts.web.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.openmrs.Concept;
import org.openmrs.Field;
import org.openmrs.FormField;
import org.openmrs.Form;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * The main controller.
 */
@Controller
public class MergeConceptsManageController {
	
	@RequestMapping(value="/module/mergeconcepts/chooseConceptsToMerge", 
			method=RequestMethod.GET)
	public void showThePage(ModelMap map) {
			
	}


	@RequestMapping(value="/module/mergeconcepts/chooseConceptsToMerge", 
					method=RequestMethod.POST)
	public void afterPageSubmission(ModelMap map, 
			@RequestParam("oldConceptId") Integer oldConceptId,
			@RequestParam("newConceptId") Integer newConceptId,
			HttpSession httpSession) {
		
		Concept oldConcept = Context.getConceptService().getConcept(oldConceptId); 
		Concept newConcept = Context.getConceptService().getConcept(newConceptId);
		
		/*Jordan playing
		String oldConceptUuid = ${ oldConcep }
		String newConceptUuid = ${ newConcept.uuid }
		*/
		
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
		
		Set<FormField> formFields1 = new HashSet();
		
		List<Form> allForms = Context.getFormService().getAllForms();
		Set<Form> oldConceptForms = new HashSet();
		
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
		httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Obs converted successfully. total converted: " + count + " Rebuild all forms that used this concept: " + oldConceptForms);
		
	}
	
}
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
	
	@ModelAttribute
	public Concept getConcept(@RequestParam(required=false, value="conceptId") Concept concept){
		return concept;
	}
	
	
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

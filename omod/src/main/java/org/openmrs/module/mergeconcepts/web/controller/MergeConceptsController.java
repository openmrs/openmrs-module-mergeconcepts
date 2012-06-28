package org.openmrs.module.mergeconcepts.web.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.openmrs.Concept;
import org.openmrs.Field;
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

@Controller
public class MergeConceptsController {
	
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
		Integer oldConceptUuid = ${ oldConcept.uuid }
		Integer newConceptUuid = ${ newConcept.uuid }
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
		
		List<Field> fields = Context.getFormService().getFieldsByConcept(oldConcept);
		
		Set<Form> formsNeedingRebuilding = new HashSet<Form>();
		
		for (Field f : fields) {
			f.setConcept(newConcept);
			if (f.getForms() != null)
				formsNeedingRebuilding.addAll(f.getForms());
			Context.getFormService().saveField(f);
		}
		
		//change message
		httpSession.setAttribute(WebConstants.OPENMRS_MSG_ATTR, "Obs converted successfully. total converted: " + count + " Rebuild all forms that used this concept: " + formsNeedingRebuilding);
		
	}
	
}
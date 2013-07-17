package org.openmrs.module.mergeconcepts.web.controller;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptSet;
import org.openmrs.Drug;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.PersonAttributeType;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.ProgramWorkflowState;
import org.openmrs.annotation.Authorized;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.FormService;
import org.openmrs.api.ObsService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProgramWorkflowService;
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
	 * getMatchingObs - not used...
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
		List<Obs> obsFound = obsService.getObservations(null, null, conceptList, conceptList, null, null, null, null, null, null, null,
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
	 * getMatchingDrugs
	 *
	 */
	protected List<Drug> getMatchingDrugs(Concept concept){
		
		ConceptService conceptService = Context.getConceptService();
		
		List<Drug> drugsToUpdate = conceptService.getDrugsByConcept(concept);
		
		return drugsToUpdate;
		
	}
	
	/**
	 * update drugs
	 */
	public void updateDrugs(Concept oldConcept, Concept newConcept){
		List<Drug> drugsToUpdate = this.getMatchingDrugs(oldConcept);
		
		for (Drug d : drugsToUpdate){
			d.setConcept(newConcept);
		}
	}
	
	/**
	 * get matching orders
	 * 
	 */
	protected List<Order> getMatchingOrders(Concept concept){
		
		List<Concept> conceptList = new ArrayList<Concept>();
		conceptList.add(concept);
		
		OrderService orderService = Context.getOrderService();
		
		List<Order> ordersToUpdate = orderService.getOrders(null, null, conceptList, null, null, null, null);
		
		return ordersToUpdate;
		
	}
	
	/**
	 * update orders
	 */
	public void updateOrders(Concept oldConcept, Concept newConcept){
		List<Order> ordersToUpdate = this.getMatchingOrders(oldConcept);
		
		for (Order o : ordersToUpdate){
			o.setConcept(newConcept);
		}
	}
	
	/**
	 * get matching programs
	 */
	protected List<Program> getMatchingPrograms(Concept concept){
		
		MergeConceptsService service = Context.getService(MergeConceptsService.class);
		
		List<Program> programsToUpdate = service.getProgramsByConcept(concept);
		
		return programsToUpdate;
	}
	
	/**
	 * getMatchingProgramWorkflows
	 */
	protected List<ProgramWorkflow> getMatchingProgramWorkflows(Concept concept){
		
		MergeConceptsService service = Context.getService(MergeConceptsService.class);
		
		List<ProgramWorkflow> programWorkflowsToUpdate = service.getProgramWorkflowsByConcept(concept);
		
		return programWorkflowsToUpdate;
	}
	
	
	
	/**
	 * getMatchingProgramWorkflowStates
	 */
	protected List<ProgramWorkflowState> getMatchingProgramWorkflowStates(Concept concept){
		
		MergeConceptsService service = Context.getService(MergeConceptsService.class);
		
		List<ProgramWorkflowState> programWorkflowStatesToUpdate = service.getProgramWorkflowStatesByConcept(concept);
		
		return programWorkflowStatesToUpdate;
		
	}
	
	
	/**
	 * updatePrograms
	 */
	public void updatePrograms(Concept oldConcept, Concept newConcept){
		List<Program> programsToUpdate = this.getMatchingPrograms(oldConcept);
		List<ProgramWorkflow> programWorkflowsToUpdate = this.getMatchingProgramWorkflows(oldConcept);
		List<ProgramWorkflowState> programWorkflowStatesToUpdate = this.getMatchingProgramWorkflowStates(oldConcept);
		
		for (Program p : programsToUpdate){
			p.setConcept(newConcept);
		}
		
		for (ProgramWorkflow pw : programWorkflowsToUpdate){
			pw.setConcept(newConcept);
		}
		
		for (ProgramWorkflowState pws : programWorkflowStatesToUpdate){
			pws.setConcept(newConcept);
		}
	}
	
	/**
	 * getMatchingConceptSets
	 */
	protected List<ConceptSet> getMatchingConceptSets(Concept concept){
		
		ConceptService conceptService = Context.getConceptService();
		
		List<ConceptSet> conceptSetsToUpdate = conceptService.getConceptSetsByConcept(concept);
		
		return conceptSetsToUpdate;
		
	}
	
	/**
	 * update concept sets
	 */
	public void updateConceptSets(Concept oldConcept, Concept newConcept){
		
		List<ConceptSet> conceptSetsToUpdate = this.getMatchingConceptSets(oldConcept);
		
		for (ConceptSet cs : conceptSetsToUpdate){
			cs.setConcept(newConcept);
		}
		
	}
	
	/**
	 * 
	 * @param concept
	 * @return
	 */
	protected List<ConceptAnswer> getMatchingConceptAnswers(Concept concept){
		List<ConceptAnswer> matchingConceptAnswers = new ArrayList<ConceptAnswer>();
		
		for ( ConceptAnswer ca : concept.getAnswers()){
			matchingConceptAnswers.add(ca);
		}
		
		return matchingConceptAnswers;
	}
	
	/**
	 * 
	 * @param oldConcept
	 * @param newConcept
	 */
	public void updateConceptAnswers(Concept oldConcept, Concept newConcept){
		List<ConceptAnswer> conceptAnswersToUpdate = this.getMatchingConceptAnswers(oldConcept);
		
		for (ConceptAnswer ca : conceptAnswersToUpdate){
			ca.setConcept(newConcept);
		}
	}
	
	/**
	 * 
	 * @param concept
	 * @return
	 */
	protected List<PersonAttributeType> getMatchingPersonAttributeTypes(Concept concept){
		
		PersonService personService = Context.getPersonService();
		List<PersonAttributeType> allPersonAttributeTypes = personService.getAllPersonAttributeTypes();
		List<PersonAttributeType> matchingPersonAttributeTypes = new ArrayList<PersonAttributeType>();
		
		for (PersonAttributeType p : allPersonAttributeTypes){
			if(p.getFormat().toLowerCase().contains("concept")){
				if(p.getForeignKey().equals(concept.getConceptId())){	
						matchingPersonAttributeTypes.add(p);
				}
			}
		}
		
		return matchingPersonAttributeTypes;	
	}
	
	/**
	 * 
	 * @param oldConcept
	 * @param newConcept
	 */
	public void updatePersonAttributeTypes(Concept oldConcept, Concept newConcept){
		List<PersonAttributeType> matchingPersonAttributeTypes = this.getMatchingPersonAttributeTypes(oldConcept);
		
		for ( PersonAttributeType m : matchingPersonAttributeTypes ){
			m.setForeignKey(newConcept.getConceptId());
		}
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
	@Authorized( {PrivilegeConstants.VIEW_CONCEPTS, PrivilegeConstants.VIEW_FORMS, PrivilegeConstants.VIEW_ORDERS, 
		PrivilegeConstants.VIEW_PERSON_ATTRIBUTE_TYPES, PrivilegeConstants.VIEW_PROGRAMS})
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
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "The same concept was chosen twice - please try again");
			return "redirect:chooseConcepts.form";
		}

		//handle concepts with different datatypes
		//TO DO - unless oldConcept is N/A (what if it's the other way around?) <-- is that right?
		if(!this.hasMatchingDatatypes(oldConcept, newConcept)){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Please choose concepts with same datatypes and try again");
			return "redirect:chooseConcepts.form";
		}

		//if both concepts' types are coded, make sure both answer sets are the same
		if(oldConcept.getDatatype().isCoded() && !this.hasCodedAnswers(oldConcept, newConcept)){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, 
					"Concept chosen to be retired has answers that the concept to keep does not have - please try again");
			return "redirect:chooseConcepts.form";
		}

		//if both concepts' types are numeric, make sure absolute high for concept to keep includes absolute high for concept to retire
		if(oldConcept.getDatatype().isNumeric() && this.hasCorrectAbsoluteHi(oldConceptId, newConceptId)){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, 
					"Absolute high for concept to be retired is greater than absolute high for concept to keep - please try again");
			return "redirect:chooseConcepts.form";
		}
		
		//if both concepts' types are numeric, make sure absolute low for concept to keep includes absolute low for concept to retire
		if(oldConcept.getDatatype().isNumeric() && this.hasCorrectAbsoluteLow(oldConceptId, newConceptId)){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, 
					"Absolute low for concept to be retired is less than absolute low for concept to keep - please try again");
			return "redirect:chooseConcepts.form";
		}
		
		//if both concepts' types are numeric, make sure units are the same
		if(oldConcept.getDatatype().isNumeric() && !this.hasMatchingUnits(oldConceptId, newConceptId)){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, 
					"Concepts you chose have different units - please try again");
			return "redirect:chooseConcepts.form";
		}
		
		//if both concepts' types are numeric, make sure both precision (y/n)s are the same
		if(oldConcept.getDatatype().isNumeric() && !this.hasMatchingPrecise(oldConceptId, newConceptId)){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, 
					"Concepts do not agree on precise (y/n) - please try again");
			return "redirect:chooseConcepts.form";
		}		
		
		//if both concepts' types are complex, make sure handlers are the same
		if(oldConcept.getDatatype().isComplex() && !this.hasMatchingComplexHandler(oldConceptId, newConceptId)){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, 
					"Complex concepts do not have the same handler - please try again");
			return "redirect:chooseConcepts.form";
		}
		
		model.addAttribute("oldConceptId", oldConceptId);
		model.addAttribute("newConceptId", newConceptId);
		model.addAttribute("oldForms", this.getMatchingForms(oldConcept));
		model.addAttribute("newForms", this.getMatchingForms(newConcept));
		model.addAttribute("oldDrugs", this.getMatchingDrugs(oldConcept));
		model.addAttribute("newDrugs", this.getMatchingDrugs(newConcept));
		model.addAttribute("oldOrders", this.getMatchingOrders(oldConcept));
		model.addAttribute("newOrders", this.getMatchingOrders(newConcept));
		model.addAttribute("oldPrograms", this.getMatchingPrograms(oldConcept));
		model.addAttribute("newPrograms", this.getMatchingPrograms(newConcept));
		model.addAttribute("oldConceptAnswers", this.getMatchingConceptAnswers(oldConcept));
		model.addAttribute("newConceptAnswers", this.getMatchingConceptAnswers(newConcept));
		model.addAttribute("oldPersonAttributeTypes", this.getMatchingPersonAttributeTypes(oldConcept));
		model.addAttribute("newPersonAttributeTypes", this.getMatchingPersonAttributeTypes(newConcept));
		
		MergeConceptsService service = Context.getService(MergeConceptsService.class);
		
		int newObsCount = service.getObsCount(newConceptId);
		int oldObsCount = service.getObsCount(oldConceptId);
		
		model.addAttribute("newObsCount", newObsCount);
		model.addAttribute("oldObsCount", oldObsCount);
		
		return "/module/mergeconcepts/preview";
		
		
	}
	
	
	/**
	 * check if concepts have matching datatypes
	 * TO DO - unless oldConcept is N/A (what if it's the other way around?)
	 * @param oldConcept
	 * @param newConcept
	 * @return
	 */
	private boolean hasMatchingDatatypes(Concept oldConcept, Concept newConcept){
		return (oldConcept.getDatatype().equals(newConcept.getDatatype()));
	}
	
	/**
	 * 
	 * @param oldConcept
	 * @param newConcept
	 * @return
	 */
	private boolean hasCodedAnswers(Concept oldConcept, Concept newConcept){
		if(newConcept.getAnswers(false) == null){
			return true;
		}
		
		else if(oldConcept.getAnswers(false) == null){
			return false;
		}

		return newConcept.getAnswers(false).containsAll(oldConcept.getAnswers(false));
	}
	
	/**
	 * 
	 * @param oldConceptId
	 * @param newConceptId
	 * @return
	 */
	private boolean hasCorrectAbsoluteHi(Integer oldConceptId, Integer newConceptId){
		ConceptService conceptService = Context.getConceptService();
		
		if(conceptService.getConceptNumeric(newConceptId).getHiAbsolute() == null){
			return true;
		}
		
		else if(conceptService.getConceptNumeric(oldConceptId).getHiAbsolute() == null){
			return false;
		}
				
		return (conceptService.getConceptNumeric(oldConceptId).getHiAbsolute() > conceptService.getConceptNumeric(newConceptId).getHiAbsolute());
	}
	
	private boolean hasCorrectAbsoluteLow(Integer oldConceptId, Integer newConceptId){
		//if has absolute lows
			ConceptService conceptService = Context.getConceptService();
			return (conceptService.getConceptNumeric(oldConceptId).getLowAbsolute() < conceptService.getConceptNumeric(newConceptId).getLowAbsolute());
			
		//else return true;
	}
	
	//if both concepts' types are numeric, make sure units are the same
	private boolean hasMatchingUnits(Integer oldConceptId, Integer newConceptId){
		
		ConceptService conceptService = Context.getConceptService();
		return conceptService.getConceptNumeric(oldConceptId).getUnits().equals(conceptService.getConceptNumeric(newConceptId).getUnits());
	}
	
	//if both concepts' types are numeric, make sure both precision (y/n)s are the same
	private boolean hasMatchingPrecise(Integer oldConceptId, Integer newConceptId){
		
		ConceptService conceptService = Context.getConceptService();
		return conceptService.getConceptNumeric(oldConceptId).getPrecise().equals(conceptService.getConceptNumeric(newConceptId).getPrecise());
	}	
	
	
	//if both concepts' types are complex, make sure handlers are the same
	private boolean hasMatchingComplexHandler(Integer oldConceptId, Integer newConceptId){
		
		ConceptService conceptService = Context.getConceptService();
		return conceptService.getConceptComplex(oldConceptId).getHandler().equals(conceptService.getConceptComplex(newConceptId).getHandler());
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
			
			//DRUGS
			this.updateDrugs(oldConcept, newConcept);
			
			//ORDERS
			this.updateOrders(oldConcept, newConcept);
		
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
	@Authorized( {PrivilegeConstants.VIEW_CONCEPTS, PrivilegeConstants.VIEW_FORMS})
	@RequestMapping("/module/mergeconcepts/results")
	public void results(ModelMap model, @RequestParam("oldConceptId") Integer oldConceptId,
									    @RequestParam("newConceptId") Integer newConceptId) {
		
		//redirect if something went wrong
		
		ConceptService conceptService = Context.getConceptService();
		
		Concept oldConcept = conceptService.getConcept(oldConceptId); 
		Concept newConcept = conceptService.getConcept(newConceptId);

		MergeConceptsService service = Context.getService(MergeConceptsService.class);
		
		int newObsCount = service.getObsCount(newConceptId);
		int oldObsCount = service.getObsCount(oldConceptId);
		
		model.addAttribute("newObsCount", newObsCount);
		model.addAttribute("oldObsCount", oldObsCount);
		model.addAttribute("oldForms", this.getMatchingForms(oldConcept));
		model.addAttribute("newForms", this.getMatchingForms(newConcept));
		//repeat for drugs (ConceptService.getCountOfDrugs) & orders :D
	}
	
}
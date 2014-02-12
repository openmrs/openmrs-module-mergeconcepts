package org.openmrs.module.mergeconcepts.web.controller;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptSet;
import org.openmrs.Drug;
import org.openmrs.Field;
import org.openmrs.Form;
import org.openmrs.FormField;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.OrderType;
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
public class MergeConceptsManageController extends BaseOpenmrsObject {
	
	private Logger log = Logger.getLogger(MergeConceptsManageController.class);
	
	//TODO add event
	
	/**
	 * Called when any page is requested, does not respond to concept search widgets
	 * @should set model attribute "newConcept" to concept user wants to keep
	 * @param newConceptId
	 * @return
	 */
	@ModelAttribute("newConcept")
	public Concept getNewConcept(@RequestParam(required=false, value="newConceptId") String newConceptId){
		//going to make this use ConceptEditor instead
        return getConceptService().getConcept(newConceptId);
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
        return getConceptService().getConcept(oldConceptId);
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
			return result; //TODO reconsider error handling strategy here
		
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
	
	/**
	 * 
	 * @param concept
	 * @return
	 */
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
	 * 
	 * @param concept
	 * @return
	 */
	protected List<Drug> getMatchingDrugs(Concept concept){
		
		ConceptService conceptService = getConceptService();
		
		List<Drug> drugsToUpdate = conceptService.getDrugsByConcept(concept);
		
		return drugsToUpdate;	
	}
	
	/**
	 * 
	 * @param concept
	 * @return
	 */
	protected List<Drug> getMatchingDrugIngredientDrugs(Concept concept){
		MergeConceptsService service = Context.getService(MergeConceptsService.class);
		List<Drug> drugIngredientsToUpdate = service.getDrugsByIngredient(concept);
		return drugIngredientsToUpdate;
	}
	
	/**
	 * 
	 * @param oldConcept
	 * @param newConcept
	 */
	public void updateDrugs(Concept oldConcept, Concept newConcept){
		
		MergeConceptsService service = Context.getService(MergeConceptsService.class);
		ConceptService conceptService = getConceptService();
		
		List<Drug> drugsToUpdate = this.getMatchingDrugs(oldConcept);
		
		if(drugsToUpdate!=null){
			for (Drug d : drugsToUpdate){
				d.setConcept(newConcept);
			}
		}
		
		//service.updateDrugRoutes(oldConcept.getConceptId(), newConcept.getConceptId());
		/**
		 * TODO need to fix hql in service before this will work
		List<Drug> dosageFormsToUpdate = new ArrayList<Drug>();
			
		if(service.getDosageForms(oldConcept.getConceptId())!=null){
			for (Integer d : service.getDosageForms(oldConcept.getConceptId())){
				dosageFormsToUpdate.addAll(conceptService.getDrugsByConcept(conceptService.getConcept(d)));
			}
		}*/
		
	}
	

	/**
	 * 
	 * @param concept
	 * @return
	 */
	protected List<Order> getMatchingOrders(Concept concept){
		
		List<Concept> conceptList = new ArrayList<Concept>();
		conceptList.add(concept);
		
		OrderService orderService = Context.getOrderService();
		
		List<Order> ordersToUpdate = orderService.getOrders(Order.class, null, conceptList, null, null, null, null);
		
		return ordersToUpdate;
		
	}
	

	/**
	 * 
	 * @param oldConcept
	 * @param newConcept
	 */
	public void updateOrders(Concept oldConcept, Concept newConcept){
		List<Order> ordersToUpdate = this.getMatchingOrders(oldConcept);
		
		for (Order o : ordersToUpdate){
			o.setConcept(newConcept);
		}
	}
	

	/**
	 * 
	 * @param concept
	 * @return
	 */
	protected List<Program> getMatchingPrograms(Concept concept){
		
		MergeConceptsService service = Context.getService(MergeConceptsService.class);
		
		List<Program> programsToUpdate = service.getProgramsByConcept(concept);
		
		return programsToUpdate;
	}
	

	/**
	 * 
	 * @param concept
	 * @return
	 */
	protected List<ProgramWorkflow> getMatchingProgramWorkflows(Concept concept){
		
		MergeConceptsService service = Context.getService(MergeConceptsService.class);
		
		List<ProgramWorkflow> programWorkflowsToUpdate = service.getProgramWorkflowsByConcept(concept);
		
		return programWorkflowsToUpdate;
	}
	
	/**
	 * 
	 * @param concept
	 * @return
	 */
	protected List<ProgramWorkflowState> getMatchingProgramWorkflowStates(Concept concept){
		
		MergeConceptsService service = Context.getService(MergeConceptsService.class);
		
		List<ProgramWorkflowState> programWorkflowStatesToUpdate = service.getProgramWorkflowStatesByConcept(concept);
		
		return programWorkflowStatesToUpdate;
		
	}
	
	/**
	 * 
	 * @param oldConcept
	 * @param newConcept
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
	 * get concept_set.concept_id concepts
	 * @param concept
	 * @return
	 */
	protected List<ConceptSet> getMatchingConceptSetConcepts(Concept concept){
		
		ConceptService conceptService = getConceptService();
		
		List<ConceptSet> conceptSetsToUpdate = conceptService.getSetsContainingConcept(concept);
		
		return conceptSetsToUpdate;
		
	}
	
	/**
	 * get concept_set.concept_set concepts
	 * @param concept
	 * @return
	 */
	protected List<ConceptSet> getMatchingConceptSets(Concept concept){
		
		ConceptService conceptService = getConceptService();
		
		List<ConceptSet> conceptSetsToUpdate = conceptService.getConceptSetsByConcept(concept);
		
		return conceptSetsToUpdate;
		
	}
	
	/**
	 * 
	 * @param oldConcept
	 * @param newConcept
	 */
	public void updateConceptSets(Concept oldConcept, Concept newConcept){
		
		//update concept_id
		List<ConceptSet> conceptSetConceptsToUpdate = this.getMatchingConceptSetConcepts(oldConcept);
		if(this.getMatchingConceptSetConcepts(oldConcept)!=null){
			for (ConceptSet csc : conceptSetConceptsToUpdate){
				csc.setConcept(newConcept);
			}
		}
		
		//concept_set
		List<ConceptSet> conceptSetsToUpdate = this.getMatchingConceptSets(oldConcept);
		if(this.getMatchingConceptSets(oldConcept)!=null){
			for (ConceptSet cs : conceptSetsToUpdate){
				cs.setConceptSet(newConcept);
			}
		}
	}
	
	/**
	 * 
	 * @param concept
	 * @return
	 */
	protected List<ConceptAnswer> getMatchingConceptAnswerQuestions(Concept concept){
		
		List<ConceptAnswer> matchingConceptAnswers = new ArrayList<ConceptAnswer>();
		
		for ( ConceptAnswer ca : concept.getAnswers()){
			matchingConceptAnswers.add(ca);
		}
		
		return matchingConceptAnswers;
	}
	
	/**
	 * 
	 * @param concept
	 * @return
	 */
	protected List<ConceptAnswer> getMatchingConceptAnswerAnswers(Concept concept){
		
		ConceptService conceptService = getConceptService();
		List<ConceptAnswer> matchingConceptAnswers = new ArrayList<ConceptAnswer>();
		
		//Concepts that are questions answered by this concept, and possibly others
		for ( Concept c : conceptService.getConceptsByAnswer(concept) ){
			
			//ConceptAnswers of all possible answers to question concept above
			for ( ConceptAnswer a : c.getAnswers() ){
				
				//only add ConceptAnswers with an answer matching this concept
				if(a.getAnswerConcept().equals(concept)){
					matchingConceptAnswers.add(a);
				}
			}
		}
		
		return matchingConceptAnswers;
	}
	
	/**
	 * 
	 * @param concept
	 * @return
	 */
	protected List<ConceptAnswer> getMatchingConceptAnswers(Concept concept){
		
		List<ConceptAnswer> a = this.getMatchingConceptAnswerAnswers(concept);
		
		for ( ConceptAnswer c : this.getMatchingConceptAnswerQuestions(concept)){
			a.add(c);
		}
		
		return a;
	}
	
	/**
	 * ConceptAnswers contain references to concepts 
	 * @param oldConcept
	 * @param newConcept
	 */
	public void updateConceptAnswers(Concept oldConcept, Concept newConcept){
		
		List<ConceptAnswer> conceptAnswerQuestionsToUpdate = this.getMatchingConceptAnswerQuestions(oldConcept);
		
		//update concept_id
		for (ConceptAnswer caq : conceptAnswerQuestionsToUpdate){
			caq.setConcept(newConcept);
		}
		
		List<ConceptAnswer> conceptAnswerAnswersToUpdate = this.getMatchingConceptAnswerAnswers(oldConcept);
		
		//update answer_concepts
		for (ConceptAnswer caa : conceptAnswerAnswersToUpdate){
			caa.setAnswerConcept(newConcept);
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
				if(p.getForeignKey() != null && p.getForeignKey().equals(concept.getConceptId())){	
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
	
	//TODO public void posibleDuplicateDataLogInfo()
	//if this.getMatching... log.info(...
	
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
		
		ConceptService conceptService = getConceptService();

		Concept oldConcept = conceptService.getConcept(oldConceptId); 
		Concept newConcept = conceptService.getConcept(newConceptId);

		//handle conceptIds are the same
		if(oldConceptId.equals(newConceptId)){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "The same concept was chosen twice - please try again");
			return "redirect:chooseConcepts.form";
		}

		//handle concepts with different datatypes
		//TODO - unless oldConcept is N/A (what if it's the other way around?) <-- is that right?
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
		if(oldConcept.getDatatype().isNumeric() && !this.hasCorrectAbsoluteHi(oldConceptId, newConceptId)){
			httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, 
					"Absolute high for concept to be retired is greater than absolute high for concept to keep - please try again");
			return "redirect:chooseConcepts.form";
		}
		
		//if both concepts' types are numeric, make sure absolute low for concept to keep includes absolute low for concept to retire
		if(oldConcept.getDatatype().isNumeric() && !this.hasCorrectAbsoluteLow(oldConceptId, newConceptId)){
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
		
		//preview drugs by name
		List<String> oldDrugNames = new ArrayList<String>();
		if(this.getMatchingDrugs(oldConcept)!=null){
			for (Drug od : this.getMatchingDrugs(oldConcept)){
				oldDrugNames.add(od.getFullName(null));
			}
		}
		List<String> newDrugNames = new ArrayList<String>();
		if(this.getMatchingDrugs(newConcept)!=null){
			for (Drug nd : this.getMatchingDrugs(newConcept)){
				newDrugNames.add(nd.getFullName(null));
			}
		}			
		model.addAttribute("oldDrugs", oldDrugNames);
		model.addAttribute("newDrugs", newDrugNames);
		
		model.addAttribute("oldConceptId", oldConceptId);
		model.addAttribute("newConceptId", newConceptId);
		
		if(this.getMatchingForms(oldConcept)!=null)
			model.addAttribute("oldForms", this.getMatchingForms(oldConcept));
		
		if(this.getMatchingForms(newConcept)!=null)
			model.addAttribute("newForms", this.getMatchingForms(newConcept));		
		
		if(this.getMatchingOrders(oldConcept)!=null)
			model.addAttribute("oldOrders", this.getMatchingOrders(oldConcept));
		
		if(this.getMatchingOrders(newConcept)!=null)
			model.addAttribute("newOrders", this.getMatchingOrders(newConcept));
		
		if(this.getMatchingPrograms(oldConcept)!=null)
			model.addAttribute("oldPrograms", this.getMatchingPrograms(oldConcept));
		
		if(this.getMatchingPrograms(newConcept)!=null)
			model.addAttribute("newPrograms", this.getMatchingPrograms(newConcept));
		
		//preview concept answers by id
		List<Integer> oldConceptAnswerIds = new ArrayList<Integer>();
		if(this.getMatchingConceptAnswers(oldConcept)!=null){
			for(ConceptAnswer a : this.getMatchingConceptAnswers(oldConcept)){
				oldConceptAnswerIds.add(a.getConceptAnswerId());
			}
		}	
		
		List<Integer> newConceptAnswerIds = new ArrayList<Integer>();
		if(this.getMatchingConceptAnswers(newConcept)!=null){
			for(ConceptAnswer b : this.getMatchingConceptAnswers(newConcept)){
				
			}
		}
		
		model.addAttribute("oldConceptAnswers", oldConceptAnswerIds);
		model.addAttribute("newConceptAnswers", this.getMatchingConceptAnswers(newConcept));
		
		List<Integer> oldConceptSetIds = new ArrayList<Integer>();
		if(this.getMatchingConceptSets(oldConcept)!=null){
			for(ConceptSet c : this.getMatchingConceptSets(oldConcept)){
				oldConceptSetIds.add(c.getConceptSetId());
			}
			if(this.getMatchingConceptSetConcepts(oldConcept)!=null){
				for(ConceptSet cs : this.getMatchingConceptSetConcepts(oldConcept)){
					oldConceptSetIds.add(cs.getConceptSetId());
				}
			}
		}
			
		List<Integer> newConceptSetIds = new ArrayList<Integer>();
		if(this.getMatchingConceptSets(newConcept)!=null){
			for(ConceptSet d : this.getMatchingConceptSets(newConcept)){
				newConceptSetIds.add(d.getConceptSetId());
			}
			if(this.getMatchingConceptSetConcepts(newConcept)!=null){
				for(ConceptSet ds : this.getMatchingConceptSetConcepts(newConcept)){
					newConceptSetIds.add(ds.getConceptSetId());
				}
			}
		}
			
		model.addAttribute("oldConceptSets", oldConceptSetIds);
		model.addAttribute("newConceptSets", newConceptSetIds);
		
		if(this.getMatchingPersonAttributeTypes(oldConcept)!=null)
			model.addAttribute("oldPersonAttributeTypes", this.getMatchingPersonAttributeTypes(oldConcept));
		
		if(this.getMatchingPersonAttributeTypes(newConcept)!=null)
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
	 * newConcept should have all answers of oldConcept
	 * @param oldConcept
	 * @param newConcept
	 * @return
	 */
	private boolean hasCodedAnswers(Concept oldConcept, Concept newConcept){
		if(newConcept.getAnswers(false) == null){
			if(oldConcept.getAnswers(false) == null){
				return true;
			}
			else return false;
		}
		
		else if(oldConcept.getAnswers(false) == null) return true;

		return newConcept.getAnswers(false).containsAll(oldConcept.getAnswers(false));
	}
	
	/**
	 * 
	 * @param oldConceptId
	 * @param newConceptId
	 * @return
	 */
	private boolean hasCorrectAbsoluteHi(Integer oldConceptId, Integer newConceptId){
		ConceptService conceptService = getConceptService();
		
		if(conceptService.getConceptNumeric(newConceptId).getHiAbsolute() == null){
			return true;
		}
		
		else if(conceptService.getConceptNumeric(oldConceptId).getHiAbsolute() == null){
			return false;
		}
				
		return (conceptService.getConceptNumeric(oldConceptId).getHiAbsolute() <= conceptService.getConceptNumeric(newConceptId).getHiAbsolute());
	}
	
	private boolean hasCorrectAbsoluteLow(Integer oldConceptId, Integer newConceptId){
		
		ConceptService conceptService = getConceptService();
		
		if(conceptService.getConceptNumeric(newConceptId).getLowAbsolute() == null){
			return true;
		}
		
		else if(conceptService.getConceptNumeric(oldConceptId).getLowAbsolute() == null){
			return false;
		}
		
		return (conceptService.getConceptNumeric(oldConceptId).getLowAbsolute() >= conceptService.getConceptNumeric(newConceptId).getLowAbsolute());
		
	}
	
	//if both concepts' types are numeric, make sure units are the same
	private boolean hasMatchingUnits(Integer oldConceptId, Integer newConceptId){
		
		ConceptService conceptService = getConceptService();
		if(conceptService.getConceptNumeric(oldConceptId).getUnits()==null)
			return true;
		return conceptService.getConceptNumeric(oldConceptId).getUnits().equals(conceptService.getConceptNumeric(newConceptId).getUnits());
	}
	
	//if both concepts' types are numeric, make sure both precision (y/n)s are the same
	private boolean hasMatchingPrecise(Integer oldConceptId, Integer newConceptId){
		
		ConceptService conceptService = getConceptService();
		return conceptService.getConceptNumeric(oldConceptId).getPrecise().equals(conceptService.getConceptNumeric(newConceptId).getPrecise());
	}	
	
	
	//if both concepts' types are complex, make sure handlers are the same
	private boolean hasMatchingComplexHandler(Integer oldConceptId, Integer newConceptId){
		
		ConceptService conceptService = getConceptService();
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
		
		ConceptService conceptService = getConceptService();
		
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
			
			//PROGRAMS
			this.updatePrograms(oldConcept, newConcept);
			
			//CONCEPT SETS
			this.updateConceptSets(oldConcept, newConcept);
			
			//CONCEPT ANSWERS
			this.updateConceptAnswers(oldConcept, newConcept);
			
			//PERSON ATTRIBUTE TYPES
			this.updatePersonAttributeTypes(oldConcept, newConcept);
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
		
		ConceptService conceptService = getConceptService();
		
		Concept oldConcept = conceptService.getConcept(oldConceptId); 
		Concept newConcept = conceptService.getConcept(newConceptId);

		MergeConceptsService service = Context.getService(MergeConceptsService.class);
		
		//preview drugs by name
		List<String> oldDrugNames = new ArrayList<String>();
		if(this.getMatchingDrugs(oldConcept)!=null){
			for (Drug od : this.getMatchingDrugs(oldConcept)){
				oldDrugNames.add(od.getFullName(null));
			}
		}
		List<String> newDrugNames = new ArrayList<String>();
		if(this.getMatchingDrugs(newConcept)!=null){
			for (Drug nd : this.getMatchingDrugs(newConcept)){
				newDrugNames.add(nd.getFullName(null));
			}
		}			
		model.addAttribute("oldDrugs", oldDrugNames);
		model.addAttribute("newDrugs", newDrugNames);
		
		model.addAttribute("oldConceptId", oldConceptId);
		model.addAttribute("newConceptId", newConceptId);
		
		if(this.getMatchingForms(oldConcept)!=null)
			model.addAttribute("oldForms", this.getMatchingForms(oldConcept));
		
		if(this.getMatchingForms(newConcept)!=null)
			model.addAttribute("newForms", this.getMatchingForms(newConcept));		
		
		if(this.getMatchingOrders(oldConcept)!=null)
			model.addAttribute("oldOrders", this.getMatchingOrders(oldConcept));
		
		if(this.getMatchingOrders(newConcept)!=null)
			model.addAttribute("newOrders", this.getMatchingOrders(newConcept));
		
		if(this.getMatchingPrograms(oldConcept)!=null)
			model.addAttribute("oldPrograms", this.getMatchingPrograms(oldConcept));
		
		if(this.getMatchingPrograms(newConcept)!=null)
			model.addAttribute("newPrograms", this.getMatchingPrograms(newConcept));
		
		//preview concept answers by id
		List<Integer> oldConceptAnswerIds = new ArrayList<Integer>();
		if(this.getMatchingConceptAnswers(oldConcept)!=null){
			for(ConceptAnswer a : this.getMatchingConceptAnswers(oldConcept)){
				oldConceptAnswerIds.add(a.getConceptAnswerId());
			}
		}	
		
		List<Integer> newConceptAnswerIds = new ArrayList<Integer>();
		if(this.getMatchingConceptAnswers(newConcept)!=null){
			for(ConceptAnswer b : this.getMatchingConceptAnswers(newConcept)){
				
			}
		}
		
		model.addAttribute("oldConceptAnswers", oldConceptAnswerIds);
		model.addAttribute("newConceptAnswers", this.getMatchingConceptAnswers(newConcept));
		
		List<Integer> oldConceptSetIds = new ArrayList<Integer>();
		if(this.getMatchingConceptSets(oldConcept)!=null){
			for(ConceptSet c : this.getMatchingConceptSets(oldConcept)){
				oldConceptSetIds.add(c.getConceptSetId());
			}
			
			if(this.getMatchingConceptSetConcepts(oldConcept)!=null){
				for(ConceptSet cs : this.getMatchingConceptSetConcepts(oldConcept)){
					oldConceptSetIds.add(cs.getConceptSetId());
				}
			}
		}
			
		List<Integer> newConceptSetIds = new ArrayList<Integer>();
		if(this.getMatchingConceptSets(newConcept)!=null){
			for(ConceptSet d : this.getMatchingConceptSets(newConcept)){
				newConceptSetIds.add(d.getConceptSetId());
			}
			
			if(this.getMatchingConceptSetConcepts(newConcept)!=null){
				for(ConceptSet ds : this.getMatchingConceptSetConcepts(newConcept)){
					newConceptSetIds.add(ds.getConceptSetId());
				}
			}
		}
			
		model.addAttribute("oldConceptSets", oldConceptSetIds);
		model.addAttribute("newConceptSets", newConceptSetIds);
		
		if(this.getMatchingPersonAttributeTypes(oldConcept)!=null)
			model.addAttribute("oldPersonAttributeTypes", this.getMatchingPersonAttributeTypes(oldConcept));
		
		if(this.getMatchingPersonAttributeTypes(newConcept)!=null)
			model.addAttribute("newPersonAttributeTypes", this.getMatchingPersonAttributeTypes(newConcept));
		
		int newObsCount = service.getObsCount(newConceptId);
		int oldObsCount = service.getObsCount(oldConceptId);
		
		model.addAttribute("newObsCount", newObsCount);
		model.addAttribute("oldObsCount", oldObsCount);
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

    protected ConceptService getConceptService() {
        return Context.getConceptService();
    }

}
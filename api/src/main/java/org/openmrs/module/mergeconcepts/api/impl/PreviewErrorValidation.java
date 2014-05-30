package org.openmrs.module.mergeconcepts.api.impl;

import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.web.WebConstants;

import javax.servlet.http.HttpSession;

public class PreviewErrorValidation {
    //if both concepts' types are numeric, make sure both precision (y/n)s are the same
    private boolean hasMatchingPrecise(Integer oldConceptId, Integer newConceptId) {
        return getConceptService().getConceptNumeric(oldConceptId).getPrecise().equals(getConceptService().getConceptNumeric(newConceptId).getPrecise());
    }

    /**
     * check if concepts have matching datatypes
     * TO DO - unless oldConcept is N/A (what if it's the other way around?)
     */
    private static boolean hasMatchingDatatypes(Concept oldConcept, Concept newConcept) {
        return (oldConcept.getDatatype().equals(newConcept.getDatatype()));
    }

    /**
     * newConcept should have all answers of oldConcept
     */
    private static boolean hasCodedAnswers(Concept oldConcept, Concept newConcept) {
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

    //if both concepts' types are complex, make sure handlers are the same
    private boolean hasMatchingComplexHandler(Integer oldConceptId, Integer newConceptId) {
        return getConceptService().getConceptComplex(oldConceptId).getHandler().equals(getConceptService().getConceptComplex(newConceptId).getHandler());
    }

    public boolean hasErrors(Integer oldConceptId, Integer newConceptId, HttpSession httpSession) {

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
        if (!hasMatchingDatatypes(oldConcept, newConcept)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Please choose concepts with same datatypes and try again");
            return true;
        }

        //if both concepts' types are coded, make sure both answer sets are the same
        if (oldConcept.getDatatype().isCoded() && !hasCodedAnswers(oldConcept, newConcept)) {
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
        if (oldConcept.getDatatype().isComplex() && !hasMatchingComplexHandler(oldConceptId, newConceptId)) {
            httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                    "Complex concepts do not have the same handler - please try again");
            return true;
        }
        return false;
    }
    public ConceptService getConceptService() {
        return Context.getConceptService();
    }

    private boolean isNumeric(Concept oldConcept) {
        return oldConcept.getDatatype().isNumeric();
    }
}

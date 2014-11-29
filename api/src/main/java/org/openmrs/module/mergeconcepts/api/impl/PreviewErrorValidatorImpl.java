package org.openmrs.module.mergeconcepts.api.impl;

import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mergeconcepts.api.PreviewErrorValidator;
import org.openmrs.web.WebConstants;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

public class PreviewErrorValidatorImpl implements PreviewErrorValidator {

    public boolean hasErrors(Integer oldConceptId, Integer newConceptId, HttpSession httpSession) {
        List<ErrorBucket> errorBuckets = getErrorBuckets(oldConceptId, newConceptId);

        for (ErrorBucket bucket : errorBuckets) {
            if(bucket.hasError()) {
                setErrorMessage(httpSession, bucket.getMessage());
                return true;
            }
        }
        return false;
    }

    private List<ErrorBucket> getErrorBuckets(Integer oldConceptId, Integer newConceptId) {
        Concept oldConcept = getConceptService().getConcept(oldConceptId);
        Concept newConcept = getConceptService().getConcept(newConceptId);

        boolean anIDIsNull           = oldConceptId == null || newConceptId == null;
        boolean idsAreTheSame        = oldConceptId.equals(newConceptId);
        boolean differentDatatypes   = !hasMatchingDatatypes(oldConcept, newConcept);
        boolean differentAnswerSets  = oldConcept.getDatatype().isCoded() && !hasCodedAnswers(oldConcept, newConcept);
        boolean retiredIsTooHigh     = isNumeric(oldConcept) && !hasCorrectAbsoluteHi(oldConceptId, newConceptId);
        boolean retiredIsTooLow      = isNumeric(oldConcept) && !hasCorrectAbsoluteLow(oldConceptId, newConceptId);
        boolean differentUnits       = isNumeric(oldConcept) && !hasMatchingUnits(oldConceptId, newConceptId);
        boolean differentPrecision   = isNumeric(oldConcept) && !hasMatchingPrecise(oldConceptId, newConceptId);
        boolean differentHandlers    = oldConcept.getDatatype().isComplex() && !hasMatchingComplexHandler(oldConceptId, newConceptId);

        List<ErrorBucket> errors = new ArrayList<ErrorBucket>();

        errors.add(new ErrorBucket(anIDIsNull,          "Please choose two concepts and try again"));
        errors.add(new ErrorBucket(idsAreTheSame,       "The same concept was chosen twice - please try again"));
        errors.add(new ErrorBucket(differentDatatypes,  "Please choose concepts with same datatypes and try again"));
        errors.add(new ErrorBucket(differentAnswerSets, "Concept chosen to be retired has answers that the concept to keep does not have - please try again"));
        errors.add(new ErrorBucket(retiredIsTooHigh,    "Absolute high for concept to be retired is greater than absolute high for concept to keep - please try again"));
        errors.add(new ErrorBucket(retiredIsTooLow,     "Absolute low for concept to be retired is less than absolute low for concept to keep - please try again"));
        errors.add(new ErrorBucket(differentUnits,      "Concepts you chose have different units - please try again"));
        errors.add(new ErrorBucket(differentPrecision,  "Concepts do not agree on precise (y/n) - please try again"));
        errors.add(new ErrorBucket(differentHandlers,   "Complex concepts do not have the same handler - please try again"));

        return errors;
    }

    private void setErrorMessage(HttpSession httpSession, String message) {
        httpSession.setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
                message);
    }

    /**
     * check if concepts have matching datatypes
     * TO DO - unless oldConcept is N/A (what if it's the other way around?)
     */
    private static boolean hasMatchingDatatypes(Concept oldConcept, Concept newConcept) {
        return (oldConcept.getDatatype().equals(newConcept.getDatatype()));
    }

    //if both concepts' types are numeric, make sure both precision (y/n)s are the same
    private boolean hasMatchingPrecise(Integer oldConceptId, Integer newConceptId) {
        return getConceptService().getConceptNumeric(oldConceptId).getPrecise().equals(getConceptService().getConceptNumeric(newConceptId).getPrecise());
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

    private ConceptService getConceptService() {
        return Context.getConceptService();
    }

    private boolean isNumeric(Concept oldConcept) {
        return oldConcept.getDatatype().isNumeric();
    }

    class ErrorBucket {
        private boolean hasError;
        private String message;

        public ErrorBucket(boolean hasError, String message) {
            this.hasError = hasError;
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public boolean hasError() {
            return hasError;
        }
    }
}

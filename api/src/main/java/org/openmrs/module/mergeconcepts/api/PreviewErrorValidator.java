package org.openmrs.module.mergeconcepts.api;

import javax.servlet.http.HttpSession;

public interface PreviewErrorValidator {

    public boolean hasErrors(Integer oldConceptId, Integer newConceptId, HttpSession httpSession);

}

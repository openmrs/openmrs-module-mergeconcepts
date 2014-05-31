package org.openmrs.module.mergeconcepts.api;

import org.openmrs.api.OpenmrsService;

import javax.servlet.http.HttpSession;

/**
 * Created by Thoughtworker on 5/31/14.
 */
public interface PreviewErrorValidator {

    public boolean hasErrors(Integer oldConceptId, Integer newConceptId, HttpSession httpSession);

}

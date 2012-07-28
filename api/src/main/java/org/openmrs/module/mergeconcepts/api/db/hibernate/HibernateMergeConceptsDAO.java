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
 */
package org.openmrs.module.mergeconcepts.api.db.hibernate;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.SessionFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.hibernate.JDBCException;
import org.hibernate.Session;
//import org.hibernate.jdbc.Work; cannot be resolved
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mergeconcepts.api.db.MergeConceptsDAO;
import org.springframework.transaction.annotation.Transactional;

/**
 * It is a default implementation of  {@link MergeConceptsDAO}.
 */
public class HibernateMergeConceptsDAO implements MergeConceptsDAO {
	protected final Log log = LogFactory.getLog(this.getClass());
	
	private SessionFactory sessionFactory;
	
	/**
     * @param sessionFactory the sessionFactory to set
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
	    this.sessionFactory = sessionFactory;
    }
    
	/**
     * @return the sessionFactory
     */
    public SessionFactory getSessionFactory() {
	    return sessionFactory;
    }
    
    /**
     * 
     * @param conceptId
     * @param retireConcept
     * @return
     */
    @Transactional
    public Integer obsQuery(Integer conceptId, boolean retireConcept){
    	Integer obsCount = -1;

    	if(!retireConcept){
    		//sessionFactory.getCurrentSession().doWork( new Work() { void execute(Connection connection) { } } );
    		return obsCount;
    	}
    	
    	else{
    		List<Integer> obsIds = new ArrayList<Integer>();//mysql query to make List obsIds
    		
    		ObsService obsService = Context.getObsService();
    		//String msg = "Converted question concept from " + oldConcept + " to " + newConcept; <-- use SessionFactory
    		
    		int i = 0;
    		for(Integer obsId : obsIds){
    			//update obs
    			Obs o = obsService.getObs(obsId);
    			//o.setConcept(newConcept);
    			//obsService.saveObs(o, msg);
    			
    			i++;
    			if(i==200){
    				Context.flushSession();
    				Context.clearSession();
    				i = 0;
    			}
    		}
    	}
    	
    	
    	return obsCount;
    }
}
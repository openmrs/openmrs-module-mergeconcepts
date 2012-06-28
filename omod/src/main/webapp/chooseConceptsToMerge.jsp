<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<h2><spring:message code="mergeconcepts.title" /></h2>

This page lets you replace references to one concept with another concept. 
<br/>
WARNING: Only merge concepts if you are 100% sure they are duplicates. Please backup your database before proceeding.

<form method="POST" >

	Choose concept to merge:
	<openmrs_tag:conceptField formFieldName="oldConceptId" />
	
	Choose concept to merge:
	<openmrs_tag:conceptField formFieldName="newConceptId" />
	
	<input type="submit" value="Continue"/>

</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>
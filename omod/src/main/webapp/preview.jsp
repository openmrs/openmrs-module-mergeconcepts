<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<!--links back to admin page etc -->
<h1>Merge Concepts</h1> 

<br/>	
		
<%@ include file="/WEB-INF/view/module/mergeconcepts/mergeConceptsDetails.jsp"%>

<!-- Are you sure? -->
Are you sure you want to continue?
<form action="executeMerge.form" method="POST">
	<input type="hidden" name="oldConceptId" value=${ oldConcept.conceptId }>
	<input type="hidden" name="newConceptId" value=${ newConcept.conceptId }>
	<input type="submit" value="Yes, Merge Concepts" />
</form>

<a href="chooseConcepts.form">No</a>
<br>

<%@ include file="/WEB-INF/template/footer.jsp"%>

<!-- going to want to go back to prepopulated chooseConcept page... 
		?conceptId=${concept.uuid} or something in href-->
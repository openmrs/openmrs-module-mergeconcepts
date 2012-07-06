<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<!--links back to admin page etc -->
This page lets you see references to concepts chosen in the previous page
<br/>
<!-- Are you sure? -->
<form action="executeMerge.form" method="POST">
	<!-- list of things referencing chosen concepts -->
	<input type="submit" value="Yes, Merge Concepts" />
</form>

<a href="chooseConcepts.form">No</a>


<%@ include file="/WEB-INF/template/footer.jsp"%>

<!-- going to want to go back to prepopulated chooseConcept page... 
		?conceptId=${concept.uuid} or something in href-->
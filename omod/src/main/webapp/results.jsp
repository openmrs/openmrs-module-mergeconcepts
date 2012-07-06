<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<!--links back to admin page etc -->

This page lets you see the updated references to each concept
<br/>
<!-- See what happened -->
<form action="chooseConcepts.form" method="GET">
	<!-- list of things referencing chosen concepts -->
	<input type="submit" value="Merge More Concepts" />
</form>



<%@ include file="/WEB-INF/template/footer.jsp"%>

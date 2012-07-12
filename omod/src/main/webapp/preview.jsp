<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<!--links back to admin page etc -->
This page lets you see references to concepts chosen in the previous page
<br/>

	<table id="previewTable" cellpadding="1" cellspacing="0">
		<colgroup span="1" style="background-color:#6AFB92;"></colgroup>
		
		<%--Concept Name--%>
		<tr>
			<td valign="top">
				<h4>Concept ID: ${ newConcept.conceptId }</h4>
			</td>
			
			<td valign="top">
				<h4>Concept ID: ${ oldConcept.conceptId }</h4>
			</td>
			
		</tr>
		<tr>
			<td></td>
			<td></td>
		</tr>
		<%--Forms
		<tr>
			<td valign="top">
				<h4>Forms</h4>
			</td>
			<td valign="top">
				<h4>Forms</h4>
			</td>
		</tr>--%>
		
		<%--obs--%>
		<tr>
			<td valign="top">
				<h4>Obs count</h4>
			</td>
			<td valign="top">
				<h4>Obs count</h4>
			</td>
		</tr>
		
	</table>

<!-- Are you sure? -->
<form action="executeMerge.form" method="POST">
	<!-- list of things referencing chosen concepts -->
	<input type="submit" value="Yes, Merge Concepts" />
</form>

<a href="chooseConcepts.form">No</a>


<%@ include file="/WEB-INF/template/footer.jsp"%>

<!-- going to want to go back to prepopulated chooseConcept page... 
		?conceptId=${concept.uuid} or something in href-->
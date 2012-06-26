<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<p>Hello ${user.systemId}!</p>
<form method="GET">
	Choose a concept: <openmrs_tag:conceptField formFieldName="conceptId"/>
	<input type="submit" value="View"/>
</form>
<hr/>

<table>
	<tr>
		<th>Concept ID</th>
		<td>${ concept.conceptId }</td>
	</tr>
	<tr>
		<th>UUID</th>
		<td>${ concept.uuid }</td>
	</tr>
</table>

<%@ include file="/WEB-INF/template/footer.jsp"%>
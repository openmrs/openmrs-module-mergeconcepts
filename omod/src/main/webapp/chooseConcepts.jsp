<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<h2><spring:message code="mergeconcepts.title" /></h2>

Retire a duplicate concept and replace references to its concept id with another concept id. Choose duplicate concepts here.
<br/>
"Preview" will allow you to view all references that will be updated by merging chosen concepts.
<br/>
WARNING: Only merge concepts if you are 100% sure they are duplicates. Concepts should also have compatible datatypes.
Please backup your database before proceeding.
<br/>
<br/>
<form action="preview.form" method="POST" >

	<table id="conceptTable" cellpadding="1" cellspacing="0">
		<colgroup span="1" style="background-color:#6AFB92;"></colgroup>
		
		<tr>
			<td>Choose concept to keep:<openmrs_tag:conceptField formFieldName="newConceptId" /></td>
			<td>Choose concept to retire:<openmrs_tag:conceptField formFieldName="oldConceptId" /></td>
		</tr>

	</table>
	<input type="submit" value="Preview"/>
</form>


<%@ include file="/WEB-INF/template/footer.jsp"%>
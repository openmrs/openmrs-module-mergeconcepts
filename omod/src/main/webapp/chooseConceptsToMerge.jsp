<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<h2><spring:message code="mergeconcepts.title" /></h2>

This page lets you replace references to one concept with another concept. The losing concept gets retired.
<br/>
WARNING: Only merge concepts if you are 100% sure they are duplicates. Please backup your database before proceeding.
<br/>
<br/>

<form method="POST" >

	<table width="100%" id="conceptTable" cellpadding="1" cellspacing="0">
		<colgroup span="1" style="background-color:#6AFB92;" width="46%"></colgroup>
		<colgroup><col width=22></colgroup>
		<%--choose concepts (need to make page respond to choosing a concept and
								disable keeping a concept that is already retired/voided)--%>
		<tr>
			<td>Choose concept to keep:<openmrs_tag:conceptField formFieldName="newConceptId" /></td>
			<td>Choose concept to retire:<openmrs_tag:conceptField formFieldName="oldConceptId" /></td>
		</tr>
		
		
		<%--Forms--%>
		<tr>
			<td valign="top">
				<h4>Forms</h4>
			</td>
			<td valign="top">
				<h4>Forms</h4>
			</td>
		</tr>
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
	<input type="submit" />

</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>

<%--
	Choose concept to merge:
	<openmrs_tag:conceptField formFieldName="oldConceptId" />
	
	Choose concept to merge:
	<openmrs_tag:conceptField formFieldName="newConceptId" />
	
	<c:if test="${newConceptId != null}"></c:if>
	
	<colgroup>
			<col width=22>
			<col id="right">
		</colgroup>
	
--%>
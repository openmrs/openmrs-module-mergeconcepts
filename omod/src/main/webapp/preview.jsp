<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<!--links back to admin page etc -->
This page lets you see references to concepts chosen in the previous page
<br/>

	<table id="previewTable" cellpadding="1" cellspacing="0">
		<colgroup span="1" style="background-color:#6AFB92;"></colgroup>
		
		<%--Concept Name and Id--%>
		<tr>
			<td valign="top">
				<h4>Concept Name: </h4>
				<ul id="name1">
					<li>${ newConcept.name }
				</ul>
			</td>
			
			<td valign="top">
				<h4>Concept Name: </h4>
				<ul id="name2">
					<li>${ oldConcept.name }
				</ul>
			</td>
		</tr>		
		<tr>
			<td valign="top">
				<h4>Concept Id: </h4>
				<ul id="id1">
					<li>${ newConcept.conceptId }
				</ul>
			</td>
			
			<td valign="top">
				<h4>Concept Id: </h4>
				<ul id="id2">
					<li>${ oldConcept.conceptId }
				</ul>
			</td>
		</tr>
		<tr>
			<td></td>
			<td></td>
		</tr>	
<tr>
			<td valign="top">
				<h4>Concept References</h4>
	<table>
		<tr>
			<th align="left">Obs Count</th>
			<td id="ref10">${ newObsCount }</td>
		</tr>
		<tr>
			<th align="left">Forms</th>
			<td id="ref11">...</td>
		</tr>
	</table>

			</td>
			
				<td valign="top">
					<h4>Concept References</h4>
	<table>
		<tr>
			<th align="left">Obs Count</th>
			<td id="ref20">${ oldObsCount }</td>
		</tr>
		<tr>
			<th align="left">Forms</th>
			<td id="ref21">...</td>
		</tr>
	</table>
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
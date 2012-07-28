<%@ include file="/WEB-INF/template/include.jsp"%>

<%@ include file="/WEB-INF/template/header.jsp"%>

<!--links back to admin page etc -->

This page lets you see the updated references to each concept
<br/>
<!-- See what happened -->

	<table id="conceptTable" cellpadding="1" cellspacing="0">
		<colgroup span="1" style="background-color:#6AFB92;"></colgroup>
		<tr>
			<td></td>
			<td align="center" valign="middle" rowspan="9" id="patientDivider">
				<img src="/openmrs/images/leftArrow.gif"/>
			</td>
			<td></td>
		</tr>

		<%--Concept Name and Id--%>
		<tr>
			<td valign="top">
				<h4>Concept Name: </h4>
				<ul type="none" id="name1">
					<li>${ newConcept.name }
				</ul>
			</td>
			
			<td><!--  valign="top" -->
				<h4>Concept Name: </h4>
				<ul type="none" id="name2">
					<li>${ oldConcept.name }
				</ul>
			</td>
		</tr>		
		<tr>
			<td valign="top">
				<h4>Concept Id: </h4>
				<ul type="none" id="id1">
					<li>${ newConcept.conceptId }
				</ul>
			</td>
			
			<td valign="top">
				<h4>Concept Id: </h4>
				<ul type="none" id="id2">
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

<a href="chooseConcepts.form">Merge More Concepts</a>



<%@ include file="/WEB-INF/template/footer.jsp"%>

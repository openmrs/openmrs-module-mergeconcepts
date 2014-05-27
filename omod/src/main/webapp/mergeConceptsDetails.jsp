	<table id="conceptTable" cellpadding="1" cellspacing="0">
		<colgroup span="1" style="background-color:#6AFB92;"></colgroup>
		<tr>
			<td>
				<h3><i>Keep this concept</i></h3>
			</td>
			<td></td>
			<td>
				<h3><i>Retire this concept</i></h3>
			</td>
		</tr>
		<tr>
			<td></td>
			<td align="center" valign="middle" rowspan="9" id="patientDivider">
				<img src="/openmrs-standalone/images/leftArrow.gif"/>
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
			<th align="left" valign="top">Forms</th>
			<td id="ref11">
			<ul type="none" id="id3">
				<c:forEach var="f" items="${ newForms }">
				<li>${ f }
				</c:forEach>
			</ul>
			</td>
		</tr>
		<tr>
			<th align="left" valign="top">Drugs</th>
			<td id="ref12">
			<ul type="none" id="id3">
				<c:forEach var="d" items="${ newDrugs }">
				<li>${ d }
				</c:forEach>
			</ul>
			</td>
		</tr>
		<tr>
			<th align="left" valign="top">Orders</th>
			<td id="ref13">
			<ul type="none" id="id3">
				<c:forEach var="o" items="${ newOrders }">
				<li>${ o }
				</c:forEach>
			</ul>
			</td>
		</tr>
		<tr>
			<th align="left" valign="top">Programs</th>
			<td id="ref14">
			<ul type="none" id="id3">
				<c:forEach var="p" items="${ newPrograms }">
				<li>${ p }
				</c:forEach>
			</ul>
			</td>
		</tr>
		<tr>
			<th align="left" valign="top">ConceptAnswers</th>
			<td id="ref15">
			<ul type="none" id="id3">
				<c:forEach var="ca" items="${ newConceptAnswers }">
				<li>${ ca }
				</c:forEach>
			</ul>
			</td>
		</tr>
		<tr>
			<th align="left" valign="top">ConceptSets</th>
			<td id="ref16">
			<ul type="none" id="id3">
				<c:forEach var="cs" items="${ newConceptSets }">
				<li>${ cs }
				</c:forEach>
			</ul>
			</td>
		</tr>
		<tr>
			<th align="left" valign="top">PersonAttributeTypes</th>
			<td id="ref17">
			<ul type="none" id="id3">
				<c:forEach var="pat" items="${ newPersonAttributeTypes }">
				<li>${ pat }
				</c:forEach>
			</ul>
			</td>
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
			<th align="left" valign="top">Forms</th>
			<td id="ref21">
			<ul type="none" id="id3">
				<c:forEach var="f" items="${ oldForms }">
				<li>${ f }
				</c:forEach>
			</ul>
			</td>
		</tr>
		<tr>
			<th align="left" valign="top">Drugs</th>
			<td id="ref22">
			<ul type="none" id="id3">
				<c:forEach var="d" items="${ oldDrugs }">
				<li>${ d }
				</c:forEach>
			</ul>
			</td>
		</tr>
		<tr>
			<th align="left" valign="top">Orders</th>
			<td id="ref23">
			<ul type="none" id="id3">
				<c:forEach var="o" items="${ oldOrders }">
				<li>${ o }
				</c:forEach>
			</ul>
			</td>
		</tr>
		<tr>
			<th align="left" valign="top">Programs</th>
			<td id="ref24">
			<ul type="none" id="id3">
				<c:forEach var="p" items="${ oldPrograms }">
				<li>${ p }
				</c:forEach>
			</ul>
			</td>
		</tr>
		<tr>
			<th align="left" valign="top">ConceptAnswers</th>
			<td id="ref25">
			<ul type="none" id="id3">
				<c:forEach var="ca" items="${ oldConceptAnswers }">
				<li>${ ca }
				</c:forEach>
			</ul>
			</td>
		</tr>
		<tr>
			<th align="left" valign="top">ConceptSets</th>
			<td id="ref26">
			<ul type="none" id="id3">
				<c:forEach var="cs" items="${ oldConceptSets }">
				<li>${ cs }
				</c:forEach>
			</ul>
			</td>
		</tr>
		<tr>
			<th align="left" valign="top">PersonAttributeTypes</th>
			<td id="ref27">
			<ul type="none" id="id3">
				<c:forEach var="pat" items="${ oldPersonAttributeTypes }">
				<li>${ pat }
				</c:forEach>
			</ul>
			</td>
		</tr>
	</table>
				</td>
		</tr>
	</table>

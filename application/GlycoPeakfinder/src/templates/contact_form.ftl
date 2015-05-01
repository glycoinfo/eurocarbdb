<#include "./header.ftl">

<form method="POST" action="./Contact.action" name="values">

<table border="0" cellpadding="0" cellspacing="0" width="702">
<tr><TD class="gpf_heading" colspan="3">Glyco-Peakfinder</TD></tr>
<tr>
    <TD colspan="3" height="15">
        <input type="text" class="gpf_information" height="1" name="pageTo" value="">
        <input type="text" class="gpf_information" height="1" name="pageFrom" value="cont">
    </TD></tr>
<TR>
    <TD colspan="3" class="gpf_table_top"/>
</TR>
<TR>
    <TD class="gpf_table_left"/>
    <td bgcolor="#cddcec" width="700">
        <table cellpadding="0" cellpadding="0" border="0" width="700">
<!-- Start of data areal -->
            <tr><TD colspan="3" height="10"></TD></tr>
            <tr>
            	<TD width="10"/>
				<TD class="gpf_contens_b_left">Name:*</TD>
				<TD colspan="2"><input type="text" name="contact.name" size="70" value="${contact.name}"></TD>
			</tr>
            <tr><TD colspan="4" height="10"></TD></tr>
			<tr>
				<TD width="10"/>
				<TD class="gpf_contens_b_left">Email:*</TD>
				<TD colspan="2"><input type="text" name="contact.email" size="70" value="${contact.email}"></TD>
			</tr>
            <tr><TD colspan="4" height="10"></TD></tr>
			<tr>
				<TD width="10"/>
				<TD class="<#if contact.missSubject>gpf_contens_bmiss_left<#else>gpf_contens_b_left</#if>">Subject:</TD>
				<TD colspan="2"><input type="text" name="contact.subject" size="70" value="${contact.subject}"></TD>
			</tr>
            <tr><TD colspan="4" height="10"></TD></tr>
			<tr>
				<TD width="10"/>
				<TD class="gpf_contens_b_left">Type:</TD>
				<TD colspan="2">
	                <select size="1" name="contact.type">
                        <option value="comments" selected>General Comments</option>
                        <option value="question">Question for Additional Features</option>
    	    			<option value="critic">criticism</option>
						<option value="commendation">commendation</option>
						<option value="debug">error report</option>
						<option value="other">other</option>						
        	       	</select>
				</TD>
			</tr>
            <tr><TD colspan="4" height="10"></TD></tr>
			<tr>
				<TD width="10"/>
				<TD class="<#if contact.missContent>gpf_contens_bmiss_left<#else>gpf_contens_b_left</#if>">Concern:</TD>
				<TD colspan="2"><textarea name="contact.content" rows="10" cols="52"> ${contact.content}</textarea></TD>
			</tr>
            <tr><TD colspan="4" height="10"></TD></tr>
			<tr>
				<TD width="10"/>
				<TD class="gpf_contens_left">(* optional)</TD>
				<TD>
                    <input class="peakfinder_button" type="submit" name="Send" value="Send">
				<TD/>
			</tr>
            <tr><TD colspan="4" height="10"></TD></tr>
<!-- End of data areal -->
        </table>
    </td>
    <TD class="gpf_table_right"/>
</TR>
<TR>
    <TD class="gpf_table_bottom_left"/>
    <TD class="gpf_table_bottom"/>
    <TD class="gpf_table_bottom_right"/>
</TR>
<TR>
    <TD colspan="3" height="20"/>
</TR>
</table>

</form>

<#include "./footer.ftl">
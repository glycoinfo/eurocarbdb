<#if ! show_detail >
<contributor id="${x.contributorId?c}" name="${x.contributorName}" />
<#else>
<contributor id="${x.contributorId?c}" name="${x.contributorName}">
    <fullname>${x.fullName!""}</fullname>
    <institution>${x.institution!""}</institution>
    <email>${x.email!""}</email>
    <joindate>${x.dateEntered}</joindate>
    <lastLogin>${x.lastLogin}</lastLogin>
    <isActivated>${x.isActivated}</isActivated>
    <isBlocked>${x.isBlocked}</isBlocked>
</contributor>
</#if>

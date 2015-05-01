function switchToPage( a_strType )
{
    if ( a_strType == 'file' )
    {
        document.values.pageTo.value = "mass";
        document.values.submit();
    }
    if ( a_strType == 'motif' )
    {
        document.values.pageTo.value = "motif";
        document.values.pageFrom.value = "motif";
        document.values.submit();
    }
    document.values.pageTo.value = a_strType;
    document.values.submit();
}

function clearFileSettings( a_strBool )
{
	if ( a_strBool == 'true' )
	{
    	document.values.myFile.value = "";
	}
	else
	{
    	document.values['settings.peakList'].value = "";
	}
}

function setFromPage( a_strType )
{
    if ( a_strType == 'file' )
    {
        document.values.pageFrom.value = "file";
    }
    else
        document.values.pageFrom.value = a_strType;	
}
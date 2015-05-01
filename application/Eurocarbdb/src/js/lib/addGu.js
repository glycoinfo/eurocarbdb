function checknumber(){
	        var x=document.addGuValue.guValue.value
			                var anum=/(^\d+$)|(^\d+\.\d+$)/
					                if (anum.test(x))
								                        testresult=true
												                else{
															                        alert("Please input a valid number!")
																			                                testresult=false
																							                }
		        return (testresult)
}


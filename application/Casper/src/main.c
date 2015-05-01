/* 
This file is part of CASPER.

    CASPER is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    CASPER is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with CASPER.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2010 Roland Stenutz, Magnus Lundborg, Göran Widmalm
*/

#include <stdlib.h>
#include "parser.h"
#include "commands.h"
#include "methods.h"
#include "residue.h"
#include "variables.h"
#include "type.h"
#include "rule.h"
#include "ccpn.h"
#include "memops/general/Util.h"

#include "ccp.h"


#define STARTUP	"load 'casper.rc'"

/* Not nice to have these global, but it makes them a lot more manageable */
Impl_MemopsRoot Global_CcpnProject;
Nmr_NmrProject Global_NmrProject;
int projectValid=1;

int main(int argc, char *argv[])
{
  int status;
  char *cmd_str=STARTUP;

  Global_NmrProject=0;
  Global_CcpnProject=0;

  if (argc == 2)
    {
      if(!CC_LoadProject(&Global_CcpnProject, argv[1]))
	{
	  printf("Could not open CCPN project file!\n");
	  return(EXIT_FAILURE);
	}
    }
  status=PA_Parse((FILE *)cmd_str, stdout, NULL, /*Global*/MainCmds);

  ME_EmptyList(&ResidueMethod, &ResidueList);
  /*  ME_EmptyList(&VariableMethod, &VariableList);*/
  ME_EmptyList(&TypeMethod, &TypeList);
  ME_EmptyList(&RU_RuleMethod, &RuleList);

  if(Global_NmrProject)
    {
      ApiObject_Free(Global_NmrProject);
    }
  if(Global_CcpnProject)
    {
      ApiObject_Free(Global_CcpnProject);
    }

  if (status==PA_SUCCESS)
    {
      return(EXIT_SUCCESS);
    }
  return(EXIT_FAILURE);
}

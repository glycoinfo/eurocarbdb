/********************************************************************/
/*                         psnd_version.c                           */
/*                                                                  */
/* 1998, Albert van Kuik                                            */
/********************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include "version.h"

char   *psnd_get_version_string(void);

char *psnd_get_version_string()
{
    static char *version_string = PSND_VERSION;
    return version_string;
}


/************************************************************************/
/*                               genrbtree.h                            */
/*                                                                      */
/*  Platform : All                                                      */
/*  Module   : General include file                                     */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/

#ifndef _GENRBTREE_H
#define _GENRBTREE_H


int   RB_initTree(void);
int   RB_insertValue(int unit, void *keyvalue, void  *data);
void *RB_searchTree(int unit, void *keyvalue);
int   RB_numValues(int unit);
int   RB_deleteValue(int unit, void *keyvalue);
void  RB_killTree(int unit);
int   RB_doForAll(int unit, 
                  int (*func)(void *key, void *data, void *userdata),
                  void *userdata);

#endif

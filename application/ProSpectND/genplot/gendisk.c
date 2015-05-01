/************************************************************************/
/*                               gendisk.c                              */
/*                                                                      */
/*  Platform : All                                                      */
/*  Module   : Genplot objects to disk                                  */
/*                                                                      */
/*  Albert van Kuik                                                     */
/************************************************************************/

#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#ifdef DEBUG
#include "mshell.h"
#endif

#include "btree.h"

#include "genplot.h"
#include "g_inter.h"

#define KEY1 			0
#define KEYLEN 			32
#define GENPLOT_VERSION		"GP00"
#define NUMBER_OF_KEYS		1

void g_create_object_file(char *name, char *ident, int compress)
{
    int id;
    dfile_create(name, ident, GENPLOT_VERSION, 
                 NUMBER_OF_KEYS, USE_BIG_ENDIAN, compress);    
    id = dfile_open(name, TRUE);
    btree_build(id, KEY1, KEYLEN);   
    dfile_close(id);
}

int g_open_object_file(char *name, int w)
{
    int id;
    
    if ((id = dfile_open(name, w)) == G_ERROR)
        return G_ERROR;
    btree_init(id);
    if (strncmp(dfile_get_version(id), "GP00", 4) != 0) {
        fprintf(stderr, "Unkown Object-File Version\n");
        return G_ERROR;
    }
    return id;
}

char *g_get_object_file_ident(int file_id)
{
    return dfile_get_ident(file_id);
}

void g_close_object_file(int file_id)
{
    dfile_close(file_id);
    btree_close(file_id);
}

int g_erase_fileobject(int file_id, char *label)
{
    int pt;
    
    if ((pt=btree_locate(file_id, KEY1, label)) == 0) 
        return G_ERROR;
    if (btree_deletekey(file_id, KEY1, label, pt) == G_ERROR)
        return G_ERROR;
    if (dfile_delete_record(file_id, pt)==G_ERROR)
        return G_ERROR;
    dfile_write_header(file_id);
    return G_OK;
}

int g_first_fileobject(int file_id, char *label)
{
    if (!btree_firstkey(file_id, KEY1))
        return G_ERROR;
    btree_keyval(file_id, KEY1, label);
    label[KEYLEN] = '\0';
    return G_OK;
}

int g_next_fileobject(int file_id, char *label)
{
    if (!btree_nextkey(file_id, KEY1))
        return G_ERROR;
    btree_keyval(file_id, KEY1, label);
    label[KEYLEN] = '\0';
    return G_OK;
}

int g_prev_fileobject(int file_id, char *label)
{
    if (!btree_prevkey(file_id, KEY1))
        return G_ERROR;
    btree_keyval(file_id, KEY1, label);
    label[KEYLEN] = '\0';
    return G_OK;
}

int g_last_fileobject(int file_id, char *label)
{
    if (!btree_lastkey(file_id, KEY1))
        return G_ERROR;
    btree_keyval(file_id, KEY1, label);
    label[KEYLEN] = '\0';
    return G_OK;
}

/****************************************************/
int INTERNAL_write_object_file(int file_id, char *label, BYTE * obj, 
                               int size, int overwrite)
{
    int pt;
    char key[KEYLEN+1];
    
    if ((pt=btree_locate(file_id, KEY1, label)) != 0) {
        if (!overwrite)
            return G_ERROR;
        if (dfile_put_record(file_id, pt, obj, size)==G_ERROR)
            return G_ERROR;
    }
    else {
        if ((pt = dfile_new_record(file_id, obj, size)) == 0)
            return G_ERROR;
        memset(key, 0, KEYLEN+1);
        strncpy(key, label, KEYLEN);
        if (btree_insertkey(file_id, KEY1, key, pt, TRUE) == G_ERROR)
            return G_ERROR;
    }
    dfile_write_header(file_id);
    return G_OK;
}

int INTERNAL_read_object_file(int file_id, char *label, BYTE **obj)
{
    int pt, size = 0;;
    
    if ((pt=btree_locate(file_id, KEY1, label)) != 0) {
        if ((size = dfile_get_record_size(file_id, pt)) == 0)
            return G_ERROR;
        *obj = (BYTE*) malloc(size);
        return dfile_get_record(file_id, pt, *obj);
    }
    return G_ERROR;
}

int INTERNAL_find_object_file(int file_id, char *label)
{
    return btree_locate(file_id, KEY1, label);
}



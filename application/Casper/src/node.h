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

/* This is the header file for list handling (node.o)	*/
/* The commands available are:				*/

/* void *MakeList(Size,Name)	Allocates space for an empty list */
/* void *MakeNode(Size,Name)	Allocates space for a node and initializes it */
/* int AddNode(Pred,Node)	Inserts a node after Pred */
/* node *First(List)		First node in list */
/* node *Last(List)		Last node in list */
/* int AddHead(List,Node)	*/
/* int AddTail(List,Node)	*/
/* void SwapNode(Node1,Node2)	Swaps to nodes in a list */
/* void SortList(List,Cmp)	Bubblesorts a list using Cmp()		*/
/* void *RemNode(Node)		Removes a node from a list */
/* void *FreeNode(Node)		Removes a node from a list and deallocates its space */
/* struct Node *FindNode(List,Name) Returns the position of the first node in List that */
/*				is called Name */
/* struct Node *CpyNode(Size,Node,Name|NULL)		Duplicates a node */
/* struct List *CpyList(ListSize,NodeSize,List,Name|NULL)	Duplicates a list */
/* int ListLen(List)			Returns the number of nodes in a list */

#ifndef _NODE_H
#define _NODE_H		NULL

#ifndef TRUE
#define TRUE (-1)
#define FALSE (0)
#endif

#ifndef NULL
#define NULL	0
#endif

#define NODE_NAME_LENGTH	(48)	/* Longest name */

/* The node structure */
struct Node{
	struct Node *Pred, *Succ;	/* Linking */
	char Name[NODE_NAME_LENGTH];	/* Name of the node */
	};

/* The list structure */
struct List{
	struct Node Head, Tail;		/* Defines first and last node */
	};
/* Note: Head.Pred=NULL and Tail.Succ=NULL */


void *MakeList(int Size, char *Name);      /* Allocates a list structure */
void InitList(struct List *List);          /* Initializes a list structure */
void *MakeNode(int Size, char *Name);      /* Allocates a Node structure */
int AddNode(struct Node *Pred, 
	    struct Node *Node);            /* Inserts a Node after Pred */
void *RemNode(struct Node *Node);          /* Removes a Node from a list */
void SwapNode(struct Node *Node1,
	      struct Node *Node2);         /* Swaps two nodes in a list */
void SortList(struct List *list,
	      int (*cmp)(),char mode);        /* Sorts a list */
struct Node *partition(struct Node **lb,struct Node **rb,int (*cmp)(), char mode);
void QuickSort(struct Node *lb,struct Node *rb,int (*cmp)(), char mode);

void *FreeNode(struct Node *Node);	   /* Deletes a Node from a list and deallocates it */
void FreeList(struct List *List);          /* Deletes an entire list */
void *FindNode(struct Node *Node, const char *Name);	/* Finds a Node in a list */
void *FindNodeNr(struct Node *Node, const int i); /* Finds Node nr i in a list */
struct Node *CpyNode(int Size, const struct Node *Node,
		     const char *Name);          /* Makes a copy of a node */
struct List *CpyList(int ListSize, int NodeSize,
		     const struct List *List, const char *Name);/* Makes a copy of a list */
int ListLen(const struct List *List);
int NameCompare(const struct Node *node1, const struct Node *node2, int mode);

#define First(List)		(List.Head.Succ)
#define Last(List)		(List.Tail.Pred)
#define AddHead(List,xNode)	{AddNode((struct Node *)&(List.Head),xNode);} 
#define AddTail(List,xNode)	{AddNode((List.Tail.Pred),xNode);}




#define CN_CPY_NAME	NULL
#define CL_CPY_NAME	NULL



#endif /* _NODE_H */

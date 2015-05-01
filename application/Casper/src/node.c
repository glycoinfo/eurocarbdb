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

/* This file contains c-code for list handling					*/
/* The commands available are:						*/

/* void *MakeList(Size,Name)	Allocates space for an empty list	*/
/* void *MakeNode(Size,Name)	Allocates space for a node and initializes it */
/* int AddNode(Pred,Node)	Inserts a node after Pred		*/
/* int AddHead(List,Node)						*/
/* int AddTail(List,Node)						*/
/* void SwapNode(Node1,Node2)	Swaps to nodes in a list		*/
/* void SortList(List,Cmp)	Bubblesorts a list using Cmp()		*/
/* void *RemNode(Node)		Removes a node from a list		*/
/* void *FreeNode(Node)		Removes a node from a list and deallocates its space */
/* struct Node *FindNode(List,Name) Returns the position of the first node in List that */
/*				is called Name				*/
/* struct Node *CpyNode(Size,Node,Name|NULL)		Duplicates a node */
/* struct List *CpyList(ListSize,NodeSize,List,Name|NULL)	Duplicates a list */
/* int ListLen(List)			Returns the number of nodes in a list */

#include "node.h"
#include "build.h"
#include <stdlib.h>
#include <string.h>
#include <strings.h>
#include <stdio.h>

/* Allocates a list structure */
void *MakeList(int Size, char *Name)
{
  struct List *List;
  List=(struct List *)malloc(Size);
  if (List==NULL) return(NULL);
  InitList(List);
  if (Name!=NULL) strncpy(List->Head.Name,Name,NODE_NAME_LENGTH);
  return(List);
}

/* Initializes a list structure */
void InitList(struct List *List)
{
  List->Head.Pred=NULL;
  List->Head.Succ=(&(List->Tail));
  List->Tail.Pred=(&(List->Head));
  List->Tail.Succ=NULL;
}

/* Allocates a Node structure */
void *MakeNode(int Size, char *Name)
{
  struct Node *Node;
  Node=(struct Node *)malloc(Size);
  if (Node==NULL) return(NULL);
  Node->Pred=NULL;
  Node->Succ=NULL;
  if (Name!=NULL) strncpy(Node->Name,Name, NODE_NAME_LENGTH);
  Node->Name[NODE_NAME_LENGTH-1]=0;
  return(Node);
}

/* Inserts a Node after Pred */
int AddNode(struct Node *Pred, struct Node *Node)
{
  if (Pred->Succ==NULL) return((int)NULL); /* Nodes with Succ=NULL are not valid! */
  Node->Succ=Pred->Succ;
  Node->Pred=Pred;
  Pred->Succ->Pred=Node;
  Pred->Succ=Node;
  return(-1);
}

/* Removes a Node from a list */
void *RemNode(struct Node *Node)
{
  struct Node *next;
  next=Node->Succ;

  if (Node->Succ==NULL) return(NULL);
  if (Node->Pred==NULL) return(NULL);
  Node->Pred->Succ=Node->Succ;
  Node->Succ->Pred=Node->Pred;
  free(Node);
  return(next);
  /*  return(Node->Succ);*/
}

/* Swaps place of two nodes */
void SwapNode(struct Node *Node1, struct Node *Node2)
{
  struct Node *N1_Pred, *N2_Pred, *N1_Succ, *N2_Succ;

  N1_Succ=Node1->Succ;
  N1_Pred=Node1->Pred;
  N2_Succ=Node2->Succ;
  N2_Pred=Node2->Pred;

  N1_Succ->Pred=Node2;
  N1_Pred->Succ=Node2;

  N2_Succ->Pred=Node1;
  N2_Pred->Succ=Node1;

  if (N1_Succ==Node2)
    {
      Node1->Succ=N2_Succ;
      Node1->Pred=Node2;
      Node2->Succ=Node1;
      Node2->Pred=N1_Pred;
    };

  if (N2_Succ==Node1)
    {
      Node2->Succ=N1_Succ;
      Node2->Pred=Node1;
      Node1->Succ=Node2;
      Node1->Pred=N2_Pred;
    };

  if ((N1_Succ!=Node2)&&(N2_Succ!=Node1))
    {
      Node1->Succ=N2_Succ;
      Node1->Pred=N2_Pred;
      Node2->Succ=N1_Succ;
      Node2->Pred=N1_Pred;
    };

}

/* Sorts a list using the cmp-function */
/* cmp should return 0 if equal, <0 if first less, */
/* and >0 if first greater */
void SortList(struct List *list, int (*cmp)(), char mode)
{
  struct Node *node, *succ;
  int flag;
  flag=TRUE;
  while (flag==TRUE)
    {
      flag=FALSE;
      node=(struct Node *)list->Head.Succ;
      while ( (node->Succ!=NULL)&&(node->Succ->Succ!=NULL) )
	{
	  succ=(struct Node *)node->Succ;
	  if (cmp(node,succ,mode)>0) 
	    {
	      SwapNode(node,succ);
	      flag=TRUE;
	    }
	  else node=succ;
	}
    }
}

/* Partition (and thereby QuickSort) is currently not working. The element
   swapping causes problems. If the Units List can be copied properly
   it should be fixed. */
struct Node *partition(struct Node **lb,struct Node **rb,int (*cmp)(), char mode) 
{
  struct Node *pivot,*i, *j, *temp;
  int done;           /* record if pointers cross (means we're done!) */

   /********************************************************
    * partition list [lb..rb], and return pointer to pivot *
    ********************************************************/

  /* scan from both ends, swapping when needed */
  /* care must be taken not to address outside [lb..rb] with pointers */
  i = *lb;
  j = *rb;


  /* If there are only two elements to compare make a quick check and put them
     in the right order */
  if(i->Succ==j)
    {
      if((int)cmp(i,j,mode)>0)
	{
	  SwapNode(i,j);
	  *lb=j;
	  *rb=i;
	}
      return j;
    }

  /* Select a pivot in the middle of lb and rb, by stepping j twice as */
  /* fast as i and stopping when j reaches the end. */
  j=*lb;
  while(j!=*rb && j->Succ!=*rb)
    {
      j=(struct Node *)j->Succ;
      j=(struct Node *)j->Succ;
      i=(struct Node *)i->Succ;
   }
  pivot=i;

  done = 0;

  i = *lb; j = *rb;

  while(1) 
    {
      while ((int)cmp(j, pivot,mode)>0 && j!=pivot)
	{
	  j = j->Pred;
	  if (i == j) done = 1;
	}
      while ((int)cmp(i, pivot,mode)<=0 && i!=pivot)
	{
	  i = i->Succ;
	  if (i == j) done = 1;
	}
      if (done==1)
	{
	  return pivot;
	}

      if(i==pivot)
	{
	  temp=j->Succ;
	  j->Pred->Succ=j->Succ;
	  j->Succ->Pred=j->Pred;
	  j->Succ=pivot;
	  j->Pred=pivot->Pred;
	  pivot->Pred=j;
	  j->Pred->Succ=j;
	  if(j==*rb)
	    {
	      *rb=temp->Pred;
	    }
	  j=temp;
	}
      if(j==pivot)
	{
	  temp=i->Pred;
	  i->Pred->Succ=i->Succ;
	  i->Succ->Pred=i->Pred;
	  i->Pred=pivot;
	  i->Succ=pivot->Succ;
	  pivot->Succ=i;
	  i->Succ->Pred=i;
	  if(i==*lb)
	    {
	      *lb=temp->Succ;
	    }
	  i=temp;
	}
      if(i==j)
	{
	  return pivot;
	}
      if(i!=pivot && j!=pivot)
	{
	  SwapNode(i,j);
	  temp=i;
	  i=j;
	  j=temp;
      
	  if(i==*lb)
	    {
	      *lb=j;
	    }
	  else if(j==*lb)
	    {
	      *lb=i;
	    }
	  if(i==*rb)
	    {
	      *rb=j;
	    }
	  else if(j==*rb)
	    {
	      *rb=i;
	    }
	}
    
      /* examine next element */
      if(i!=pivot)
	{
	  i = i->Succ;
	}
      if(j!=pivot)
	{
	  j = j->Pred;
	}
      if (i == j)
	{
	  return pivot;
	  /*	  done=1;*/
	}
    }
}

void QuickSort(struct Node *lb, struct Node *rb, int (*cmp)(), char mode)
{
  struct Node *m;
  
  /************************
   *  sort list [lb..rb]  *
   ************************/

  if (lb == rb || lb->Succ==0)
    {
      return;
    }

  m = partition(&lb, &rb, cmp, mode);
  if(lb->Succ==rb)
    {
      return;
    }

  /*  struct BU_Struct *temp=(struct BU_Struct *)lb;
  printf("\nPartition finished. The breaking point was %s\n", m->Name);
  printf("Left partition (lower than the pivot):\n");

  if((lb==m||(struct Node *)temp!=m->Succ) && temp && temp->Node.Succ!=NULL)
    {
      while((struct Node *)temp!=m->Succ)
	{
	  printf("%s\t%f\n",temp->Node.Name, temp->CFit);
	  temp=(struct BU_Struct *)temp->Node.Succ;
	}
    }
  printf("Right partition (higher than pivot):\n");
  while((struct Node *)temp!=rb->Succ && temp && temp->Node.Succ!=NULL)
    {
      printf("%s\t%f\n",temp->Node.Name, temp->CFit);
      temp=(struct BU_Struct *)temp->Node.Succ;
    }
  
  printf("Sort left side:\n");*/
  if (lb != m && lb != m->Pred)
    {
      QuickSort(lb, m->Pred, cmp,mode);              /* sort left side */
    }
  /*  printf("Sorted left side.\n");
      printf("Sort right side:\n"); */
  if (rb != m && m->Succ!=rb)
    {
      QuickSort((struct Node *)m->Succ, rb, cmp, mode);        /* sort right side */
    }
      /* printf("Sorted right side.\n");*/

}







/* Deletes a Node from a list and deallocates it */
void *FreeNode(struct Node *Node)
{
  struct Node *Succ;
  if (Node->Succ==NULL) return(NULL);
  if (Node->Pred==NULL) return(NULL);
  Node->Succ->Pred=Node->Pred;
  Node->Pred->Succ=Node->Succ;
  Succ=Node->Succ;
  free(Node);
  return(Succ);
}

/* Deletes an entire list */
void FreeList(struct List *List)
{
  while(List->Head.Succ!=&(List->Tail))
    FreeNode(List->Head.Succ);
}

/* Finds a Node in a list */
void *FindNode(struct Node *Node, const char *Name)
{
  if (Name==NULL) return (NULL);
  while (Node->Succ!=NULL)
    {
      Node=Node->Succ;
      if (strcasecmp(Node->Name,Name)==0)
	return (Node);
    }
  return(NULL);			/* Return NULL if not found */
}

/* Finds Node number i in a list. i==0 is Node.Head, i==1 is the first entry. */
void *FindNodeNr(struct Node *Node, const int i)
{
  int n=0;

  while(Node->Succ!=NULL)
    {
      Node=Node->Succ;
      n++;
      if(n==i)
	return(Node);
    }
  return(NULL);
}

/* Makes a copy of a node */
struct Node *CpyNode(int Size, const struct Node *Node, const char *Name)
{
  struct Node *Copy;
  Copy=(struct Node *)malloc(Size);
  if (Copy==NULL) return(NULL);
  memcpy(Copy,Node,Size);	/* Clones Node */
  Copy->Pred=NULL;		/* Adjusts linking */
  Copy->Succ=NULL;
  if (Name!=CN_CPY_NAME) strncpy(Copy->Name,Name,NODE_NAME_LENGTH); /* New name if provided */
  return(Copy);
}

/* Makes a copy of a list */
struct List *CpyList(int ListSize, int NodeSize, const struct List *List, const char *Name)
{
  struct List *Copy;
  struct Node *Node, *NewNode;
  Copy=(struct List *)malloc(ListSize);
  if (Copy==NULL) return(NULL);
  memcpy(Copy,List,ListSize);
  InitList(Copy);
  if (Name!=CL_CPY_NAME) strncpy(Copy->Head.Name,Name,NODE_NAME_LENGTH);
  for(Node=(struct Node *)List->Head.Succ; Node!=&(List->Tail); Node=Node->Succ)
    {
      NewNode=CpyNode(NodeSize,Node,CN_CPY_NAME);
      if (NewNode==NULL) 
	{
	  FreeList(Copy);
	  return(NULL);
	};
      AddHead((*Copy),NewNode);
    };
  return(Copy);
}

/* Counts the nodes in a list 950214 */
int ListLen(const struct List *List)
{
  int Count;
  struct Node *Node;
  Count=0;
  Node=List->Head.Succ;
  while (Node->Succ!=NULL)
    {
      Count++;
      Node=Node->Succ;
    };
  return(Count);
}

/* Compares the names of two nodes. Returns -1 if node1->Name is lower than
   node2->Name. If they are equal it returns 0.
   int mode is just kept to keep the same number of parameters as PE_Compare - 
   it's not actually used */
int NameCompare(const struct Node *node1, const struct Node *node2, int mode)
{
  return(strcasecmp(node1->Name, node2->Name));
}

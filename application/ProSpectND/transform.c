/*
 * transform.c 
 *
 * some basic transformations
 */

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <math.h>
#include <assert.h>
#include "transform.h"


/*
 * some OpenGL stuff for PostScript output
 */
MATRIXSTACK *new_stack()
{
    MATRIXSTACK *head, *tail;
    head = (MATRIXSTACK *)malloc(sizeof(MATRIXSTACK));
    assert(head);
    tail = (MATRIXSTACK *)malloc(sizeof(MATRIXSTACK));
    assert(tail);
    tail->next = NULL;
    head->next = tail;
    return head;
}


void delete_stack(MATRIXSTACK *head)
{
    MATRIXSTACK *t1, *t2;
    t1 = head;
    while (t1) {
        t2 = t1->next;
        free(t1);
        t1 = t2;
    }
}

MATRIX4F* push_stack(MATRIXSTACK *head)
{
    MATRIXSTACK *tail;
    tail = (MATRIXSTACK *)malloc(sizeof(MATRIXSTACK));
    assert(tail);
    memcpy(&(tail->m), &(head->next->m), sizeof(MATRIX4F));
    tail->next = head->next;
    head->next = tail;
    return &(tail->m);
}

MATRIX4F* pop_stack(MATRIXSTACK *head)
{
    MATRIXSTACK *tail;
    if (head->next->next == NULL)
        return &(head->next->m);;
    tail = head->next->next;
    free(head->next);
    head->next = tail;
    return &(tail->m);
}

MATRIX4F* peek_stack(MATRIXSTACK *head)
{
    return &(head->next->m);
}

int stackempty(MATRIXSTACK *head)
{
    return (head->next->next == NULL);
}

/*****************************************************/
/* 
 * Set a Transform matrix to an identity matrix. 
 * transform to operate on 
 */
void identity_f(MATRIX4F mat)
{
    register int i, j;

    for (i = 0; i < 4; i++)
        for (j = 0; j < 4; j++)
            mat[i][j] = (i == j);
}

void translate_f(MATRIX4F mat, float x, float y, float z)
{
    identity_f(mat);
    mat[0][3] = x;
    mat[1][3] = y;
    mat[2][3] = z;
}

void rotateX_f(MATRIX4F mat, float angle)
{
    identity_f(mat);
    angle *= PI/180.0;
    mat[1][1] = mat[2][2] = cos(angle);
    mat[1][2] = -(mat[2][1] = sin(angle));
}

void rotateY_f(MATRIX4F mat, float angle)
{
    identity_f(mat);
    angle *= PI/180.0;
    mat[0][0] = mat[2][2] = cos(angle);
    mat[2][0] = -(mat[0][2] = sin(angle));
}

void rotateZ_f(MATRIX4F mat, float angle)
{
    identity_f(mat);
    angle *= PI/180.0;
    mat[0][0] = mat[1][1] = cos(angle);
    mat[0][1] = -(mat[1][0] = sin(angle));
}

void scale_f(MATRIX4F mat, float x, float y, float z)
{
    identity_f(mat);
    mat[0][0] = x;
    mat[1][1] = y;
    mat[2][2] = z;
}


void look_at_f(MATRIX4F mat,
              float eyex, float eyey, float eyez,
              float centerx, float centery, float centerz,
              float upx, float upy, float upz )
{
    float x[3], y[3], z[3];
    MATRIX4F t;

    /* Make rotation matrix */

    z[0] = eyex - centerx;
    z[1] = eyey - centery;
    z[2] = eyez - centerz;
    normalize_f(z);

    y[0] = upx;
    y[1] = upy;
    y[2] = upz;

    /* X vector = Y cross Z */
    cross_product_f(y, z, x);
    /* Recompute Y = Z cross X */
    cross_product_f(z, x, y);

    normalize_f(x);
    normalize_f(y);

    t[0][0] = x[0];  t[0][1] = x[1];  t[0][2] = x[2];  t[0][3] = 0.0;
    t[1][0] = y[0];  t[1][1] = y[1];  t[1][2] = y[2];  t[1][3] = 0.0;
    t[2][0] = z[0];  t[2][1] = z[1];  t[2][2] = z[2];  t[2][3] = 0.0;
    t[3][0] = 0.0;   t[3][1] = 0.0;   t[3][2] = 0.0;   t[3][3] = 1.0;

    concatenate_transforms_f(mat, t, mat);

   /* Translate Eye to Origin */
    translate_f(t, -eyex, -eyey, -eyez );
    concatenate_transforms_f(mat, t, mat);
}

/*
Perspective Projection
l = left
r = right
b = bottom
t = top
n = near
f = far

l != r
b != t
n != f

glFrustum(l,r,b,t,n,f)


    | 2n/(r-l)    0      (r+l)/(r-l)     0      |
    |    0     2n/(t-b)  (t+b)/(t-b)     0      |
R = |    0        0     -(f+n)/(f-n) -2fn/(f-n) |
    |    0        0          -1          0      |


*/

void frustum_f(MATRIX4F mat,
               float l, float r, float b,
               float t, float n, float f)
{
    MATRIX4F tmat;

    if (l == r || t == b || n == f)
        return;

    tmat[0][0] =  (2.0*n)/(r-l);
    tmat[1][0] =  0.0;
    tmat[2][0] =  0.0;
    tmat[3][0] =  0.0;

    tmat[0][1] =  0.0;
    tmat[1][1] =  (2.0*n)/(t-b);
    tmat[2][1] =  0.0;
    tmat[3][1] =  0.0;

    tmat[0][2] =  (r+l)/(r-l);
    tmat[1][2] =  (t+b)/(t-b);
    tmat[2][2] = -(f+n)/(f-n);
    tmat[3][2] = -1.0;

    tmat[0][3] =  0.0;
    tmat[1][3] =  0.0;
    tmat[2][3] =  -(2.0*f*n)/(f-n);
    tmat[3][3] =  0.0;

    concatenate_transforms_f(mat, tmat, mat);
}

void ortho_f(MATRIX4F mat,
             float l, float r, float b,
             float t, float n, float f)
{
    MATRIX4F tmat;

    if (l == r || t == b || n == f)
        return;

    tmat[0][0] =  2.0/(r-l);
    tmat[1][0] =  0.0;
    tmat[2][0] =  0.0;
    tmat[3][0] =  0.0;

    tmat[0][1] =  0.0;
    tmat[1][1] =  2.0/(t-b);
    tmat[2][1] =  0.0;
    tmat[3][1] =  0.0;

    tmat[0][2] =  0.0;
    tmat[1][2] =  0.0;
    tmat[2][2] =  -2.0/(f-n);
    tmat[3][2] =  0.0;

    tmat[0][3] = -(r+l)/(r-l);
    tmat[1][3] = -(t+b)/(t-b);
    tmat[2][3] = -(f+n)/(f-n);
    tmat[3][3] =  1.0;

    concatenate_transforms_f(mat, tmat, mat);
}

void copy_transform_f(MATRIX4F mat1, MATRIX4F mat2)
{
    memcpy(mat1, mat2, sizeof(MATRIX4F));
}

/* 
 * Use matrix multiplication to combine two transforms into one. 
 */
void concatenate_transforms_f(MATRIX4F mat1, MATRIX4F mat2, 
                            MATRIX4F matresult)
{
   register int i, j, k; 
   MATRIX4F temporary;  
   /*
    * Using a temporary result allows a single transform to be passed in
    * as both one of the original transforms and as the new result. 
    */
   for (i = 0; i < 4; i++) {
      for (j = 0; j < 4; j++) {
         temporary[i][j] = 0.0;
         for (k = 0; k < 4; k++) {
            temporary[i][j] += mat1[i][k] * mat2[k][j];
         }
      }
   }
   for (i = 0; i < 4; i++)
      for (j = 0; j < 4; j++)
         matresult[i][j] = temporary[i][j];
}


/* 
 * Apply a Transform matrix to a point. 
 * Transform transform;  transform to apply to the point 
 * Point p;              the point to transform 
 * Point tp;             the returned point after transformation 
 */
void transform_point_f(MATRIX4F mat, float *p, float *tp)
{
    int i, j;
    float homogeneous[4];
    float sum;

    for (i = 0; i < 4; i++) {
        sum = 0.0;
        for (j = 0; j < 3; j++)
            sum += p[j] * mat[i][j];
        homogeneous[i] = sum + mat[i][3];
    }

    for (i = 0; i < 3; i++)
        tp[i] = homogeneous[i] / homogeneous[3];
   
}

/******************************************************/

float dot_product_f(float *p1, float *p2)
{
    return p1[0] * p2[0] + p1[1] * p2[1] + p1[2] * p2[2];
}

float normalize_f(float *p)
{
    float length,length2 = dot_product_f(p, p);

    if ( length2 > 1.0e-10 ) {
        length = sqrt(length2);
	p[0] /= length;
	p[1] /= length;
	p[2] /= length;	
    }
    else {
        length = 0;
	p[0] = 0.0;
	p[1] = 0.0;
	p[2] = 0.0;
    }
    return length;
}

void diff_vector_f(float *p1, float *p2, float *v1)
{
    v1[0] = p2[0] - p1[0];
    v1[1] = p2[1] - p1[1];
    v1[2] = p2[2] - p1[2];
}

void copy_vertex_f(VERTEXF in, VERTEXF out)
{
    out[0] = in[0];
    out[1] = in[1];
    out[2] = in[2];
}

void add_vertex_f(VERTEXF in1, VERTEXF in2, VERTEXF out)
{
    out[0] = in2[0] + in1[0];
    out[1] = in2[1] + in1[1];
    out[2] = in2[2] + in1[2];
}

/* 
 * Compute the cross product of two vectors. 
 * Point v1, v2;  the vectors to take the cross product of 
 * Point c;       the result 
 */
void cross_product_f(float *v1, float *v2, float *c)
{
    c[0] = v1[1] * v2[2] - v1[2] * v2[1];
    c[1] = v1[2] * v2[0] - v1[0] * v2[2];
    c[2] = v1[0] * v2[1] - v1[1] * v2[0];
}

/*********************************************************/


/*
// return the inverse of this matrix, that is, 
// the inverse of the rotation, the inverse of the scaling, and 
// the opposite of the translation vector.
*/
#define MSWAP(a,b) {temp=(a);(a)=(b);(b)=temp;}
void gaussj_f(MATRIX4F mat4, MATRIX4F mat2) 
{
    MATRIX4F ident,mat;
    int i, icol, irow, j, k, n, m;
    int indxc[4], indxr[4], ipiv[4];
    float big, dum, pivinv, temp;
  
    /*
     * Gauss-jordan elimination with full pivoting.  
     * from numerical recipies in C first edition, pg 36
     */
    /*
     * Make a copy of mat4, so that mat4 is not destroyed
     */
    for (i=0;i<=3;i++) 
        for (j=0;j<=3;j++)
            mat[i][j] = mat4[i][j];
    identity_f(ident);
    for (j=0;j<=3;j++)
        ipiv[j] = 0;
    for (i=0;i<=3;i++) {
        big = 0.0;
        for (j=0;j<=3;j++) {
            if (ipiv[j] != 1) {
                for (k=0;k<=3;k++) {
                    if (ipiv[k] == 0) {
                        if (fabs(mat[j][k]) >= big) {
                            big = fabs(mat[j][k]);
                            irow = j;
                            icol = k;
                        }
                    } 
                    else if (ipiv[k] > 1) {
                        printf("Singular matrix\n");
                        exit(1);
                    }
                } 
            }
        }
        ++(ipiv[icol]);
        if (irow != icol) {
            for (n=0;n<=3;n++) {
                MSWAP(mat[irow][n],mat[icol][n]);
                MSWAP(ident[irow][n],ident[icol][n]);
            }
        }
        indxr[i]=irow;
        indxc[i]=icol;
        if (mat[icol][icol] == 0.0) {
            printf("Singular matrix\n");
            exit(1);
        }
        pivinv = 1.0/mat[icol][icol];
        mat[icol][icol] = 1.0;
        for (n=0;n<=3;n++) {
            mat[icol][n]   *= pivinv;
            ident[icol][n] *= pivinv;
        }
        for (m=0;m<=3;m++) {
            if (m != icol) {
                dum = mat[m][icol];
                mat[m][icol]=0.0;
                for (n=0;n<=3;n++) {
                    mat[m][n]   -= mat[icol][n] * dum;
                    ident[m][n] -= ident[icol][n] * dum;
                }
            }
        }
    }
    for (n=3;n>=0;n--) {
        if (indxr[n] != indxc[n]) {
            for (k=0;k<=3;k++) {
                MSWAP(mat[k][indxr[n]],mat[k][indxc[n]]);
            }
        }
    }
    for (i=0;i<=3;i++) 
        for (j=0;j<=3;j++)
            mat2[i][j] = mat[i][j];
}



/*
 * transform.h
 */

#ifndef _TRANSFORM_H
#define _TRANSFORM_H

#ifndef PI
#define PI 3.14159265358979323846
#endif

typedef float MATRIX4F[4][4];
typedef float VERTEXF[3];

typedef double MATRIX4D[4][4];
typedef double VERTEXD[3];

typedef struct MSTACKTYPE {
    MATRIX4F m;
    struct MSTACKTYPE *next;
} MATRIXSTACK;

MATRIXSTACK *new_stack();
void delete_stack(MATRIXSTACK *head);
MATRIX4F* push_stack(MATRIXSTACK *head);
MATRIX4F* pop_stack(MATRIXSTACK *head);
MATRIX4F* peek_stack(MATRIXSTACK *head);
int stackempty(MATRIXSTACK *head);


void identity_f(MATRIX4F mat);
void gaussj_f(MATRIX4F mat1, MATRIX4F mat2); 
void translate_f(MATRIX4F mat, float x, float y, float z);
void rotateX_f(MATRIX4F mat, float angle);
void rotateY_f(MATRIX4F mat, float angle);
void rotateZ_f(MATRIX4F mat, float angle);
void scale_f(MATRIX4F mat, float x, float y, float z);
void look_at_f(MATRIX4F mat,
              float eyex, float eyey, float eyez,
              float centerx, float centery, float centerz,
              float upx, float upy, float upz );
void frustum_f(MATRIX4F mat,
             float l, float r, float b,
             float t, float n, float f);
void ortho_f(MATRIX4F mat,
             float l, float r, float b,
             float t, float n, float f);
void copy_transform_f(MATRIX4F mat1, MATRIX4F mat2);
void concatenate_transforms_f(MATRIX4F mat1, MATRIX4F mat2, 
                            MATRIX4F matresult);
void transform_point_f(MATRIX4F mat, float *p, float *tp);
float dot_product_f(float *p1, float *p2);
float normalize_f(float *p);
void diff_vector_f(float *p1, float *p2, float *v1);
void copy_vertex_f(VERTEXF in, VERTEXF out);
void add_vertex_f(VERTEXF in1, VERTEXF in2, VERTEXF out);
void cross_product_f(float *v1, float *v2, float *c);
void diag_f(float a[3][3], float s[3][3]);

#endif


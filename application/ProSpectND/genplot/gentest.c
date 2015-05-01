/*************************************************************************/
/*                            GENTEST.C                                  */
/* Genplot test program                                                  */
/*************************************************************************/
#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include <string.h>

#include <time.h>
#include "genplot.h"

#ifdef DEBUG
#include "mshell.h"
#endif


#ifndef min
#define min(a,b)        (((a) < (b)) ? (a) : (b))
#endif
#ifndef max
#define max(a,b)        (((a) > (b)) ? (a) : (b))
#endif


/**********************************/
int id_cons;
char *buffer;
int  device = G_SCREEN;


static char pause(void)
{
    static G_EVENT ui;
    char ch;
    int result;

    do {
        result = g_get_event(&ui);
    } while (result != G_KEYPRESS && result != G_BUTTON1PRESS && result !=
           G_COMMAND && result != G_WINDOWDESTROY);
    if ( ui.keycode != 'q' && device == G_SCREEN) {
        if (ui.keycode == 0)
            ui.keycode = '0';
        if (ui.event & G_COMMAND)
            printf("Menu item = %d\n", ui.keycode);
        else
            printf("win=%d vp=%d x=%.1f y=%.1f char=%c\n",
                ui.win_id,ui.vp_id,ui.x,ui.y,ui.keycode);
	}
    ch = ui.keycode;
    ui.keycode = 0;
    return ch;
}

static void plotje(int obj)
{
    float fig1[] = {
        555, 150,
        510, 150,
        465, 150,
        420, 105,
        375, 60,
        330, 60,
        285, 105};
    float fig2[] = {
        420, 105,
        420, 150,
	375, 150,
        330, 105};
    float fig3[] = {
        465, 150,
        420, 195,
        420, 240,
        375, 240,
        330, 195};
    int i=0;
    char s[20];

    sprintf(s,"Plotje%d", obj);
    g_open_object_byname(obj, s);
    g_moveto(fig1[i],fig1[i+1]);
    i+=2;
    while (i < 14) {
        g_lineto(fig1[i],fig1[i+1]);
        i+=2;
    }
    i=0;
    g_moveto(fig2[i],fig2[i+1]);
	i+=2;
    while (i < 8) {
        g_lineto(fig2[i],fig2[i+1]);
        i+=2;
    }
    i=0;
    g_moveto(fig3[i],fig3[i+1]);
    i+=2;
    while (i < 8) {
        g_lineto(fig3[i],fig3[i+1]);
	i+=2;
    }
    g_close_object(obj);
}

static void title(char *t)
{
    g_set_viewport(90, 0.0, 0.0, 1.0, 1.0);
    g_select_viewport(90);
    g_set_world(90, 0, 0, 400, 300);
    g_moveto(10, 280);
    g_label(t);
}

static int current_clip = TRUE;

void NewPage(void)
{
	g_newpage();
	/* --- restore old gc parameters --- */
	g_pop_gc();
	g_push_gc();

	g_set_clipping(current_clip);
}

void TestViewport(int win_id)
{
    static int id1,id2,id3;
    int i,j,list[20];
    G_EVENT ui;

    if (id1)
        g_delete_object(id1);
    else
        id1 = g_unique_object();
    if (id2)
        g_delete_object(id2);
    else
        id2 = g_unique_object();
    if (id3)
	g_delete_object(id3);
    else
        id3 = g_unique_object();

    g_open_object_byname(id2, "Test Viewport");
    plotje(id1);
    plotje(id3);
    title("Test Viewport");
    g_set_viewport(11, 0.1, 0.1, 0.3, 0.3);
    g_select_viewport(11);
    g_set_world(11, 0, -150, 800, 450);
    g_rectangle(   0, -150, 800, 450);
    g_call_object(id1);

    /* --- just testing --- */
    g_close_object(id2);
    g_append_object(id2);

    g_set_viewport(12, 0.1, 0.4, 0.3, 0.6);
    g_select_viewport(12);
    g_set_world(12, 200, 0, 600, 300);
    g_rectangle(   200, 0, 600, 300);
    g_call_object(id1);
    g_set_viewport(13, 0.1, 0.7, 0.3, 0.9);
    g_set_world(13, 240, 40, 560, 260);
    g_select_viewport(13);
    g_rectangle(   240, 40, 560, 260);
    g_call_object(id1);

    g_set_viewport(14, 0.4, 0.1, 0.6, 0.3);
    g_set_world(14, 600, 0, 200, 300);
    g_select_viewport(14);
    g_rectangle(   200, 0, 600, 300);
    g_call_object(id1);
    g_set_viewport(15, 0.7, 0.1, 0.9, 0.3);
    g_set_world(15, 600, 300, 200, 0);
    g_select_viewport(15);
    g_rectangle(   200, 0, 600, 300);
    g_call_object(id1);
    g_set_viewport(16, 0.4, 0.4, 0.9, 0.9);
    g_set_world(16, 600, 300, 200, 0);
    g_select_viewport(16);
    g_rectangle(   200, 0, 600, 300);
    g_call_object(id3);
    g_close_object(id2);
    g_call_object(id2);
    i = g_get_viewports_in_window(win_id, list);
    for (j=0;j<i;j++)
        fprintf(stderr, "Viewport = %d %d\n", list[j],j);
    do {
        int id[100], status[100];
        i = g_count_objects();
        g_list_objects(id, status);

/*
        for (j=0;j<i;j++)
            fprintf(stderr,"id = %d status = %d\n",id[j],status[j]);
*/

        g_get_event(&ui);
	if (ui.event == G_WINDOWDESTROY)
            return;
	if (ui.event == G_KEYPRESS || ui.event == G_BUTTON1PRESS ||
            ui.event == G_COMMAND) {
			if (ui.keycode == 0)
            ui.keycode = '0';
            if (ui.event & G_COMMAND)
                printf("Menu item = %d\n", ui.keycode);
            else
                printf("win=%d vp=%d x=%.1f y=%.1f char=%c\n",
                    ui.win_id,ui.vp_id,ui.x,ui.y,ui.keycode);
        }
        switch (ui.keycode) {
        case 's':
	    g_set_objectstatus(id3,G_SLEEP);
	    break;
        case 'a' :
            g_set_objectstatus(id3,G_AWAKE);
            break;
        case 'q' :
            return;
        }
        g_push_viewport();
	g_select_viewport(16);
        g_clear_viewport();
        g_pop_viewport();
        g_plotall();
    }
    while (ui.keycode != 'q');
}


void TestClip(void)
{
    long x1 = 2000,
         x2 = 6000,
         y1 = 2000,
         y2 = 4000;
    float poly[] = { 3000, 1000, 4000, 3060, 5678, 2333, 7000, 2222,
                     7000, 1000, 4353, 2160, 3000, 1000};
    static int id;
    int i;

    if (id)
        g_delete_object(id);
    else
        id = g_unique_object();
    g_open_object_byname(id, "TestClip");
    title("Test Clip");
    g_set_world(1, x1, y1, x2, y2);
    g_set_viewport(1, 0.2, 0.1, 0.8, 0.8);
    g_select_viewport(1);
{
int j;
for (j=0;j<100;j++) {
    for (i=-100;i<100;i++) {
        g_moveto(1000,1000);
        g_lineto(1000 + i * 50, 6000);
    }
}
}
    for (i=0;i<50;i++) {
        int x = 4000 + i * 50, y = 1550 + i * 40;
        int dx = 500, dy = 200;
        g_rectangle(x, y, x+ dx, y+dy);
    } 
    g_drawpoly(7, poly);
    g_rectangle(x1,y1,x2,y2);
    g_moveto(1000,1000);
    g_lineto(7000,5000);
    g_moveto(0,0);
    g_lineto(8000,6000);
    g_moveto(1000,5000);
    g_lineto(7000,3000);
    g_moveto(2500,3000);
    g_set_foreground(G_BLUE);
    g_fillcircle(600);
    g_moveto(1500, 2000);
    g_set_foreground(G_RED);
    g_label("Dit is een test, 1234567890 en nog meer en meer");
    g_close_object(id);
    g_call_object(id);
}


void TestPoly(void)
{
    float poly[] = {150, 150, 200, 200, 250, 150, 200, 100, 150, 150};
    static int id;

    if ((id = g_get_object_id("TestPoly")) != 0)
        g_delete_object(id);
    id = g_unique_object();
    g_open_object_byname(id, "TestPoly");
    title("Test Poly");
    g_select_viewport(2);
    g_set_world(2,50,50,210,170);
    g_set_viewport(2, 0.0, 0.0, 0.4, 0.4);
    g_set_foreground(G_BLUE);

    g_moveto(100, 100);
    g_circle(10);
    g_moveto(100, 110);
    g_circle(20);
    g_moveto(100, 120);
    g_circle(30);
    g_moveto(100, 130);
    g_circle(40);

    g_set_viewport(2, 0.2, 0.0, 0.6, 0.4);
    g_moveto(100, 130);
    g_fillcircle(40);
    g_set_foreground(G_RED);
    g_moveto(100, 120);
    g_fillcircle(30);
    g_set_foreground(G_YELLOW);
    g_moveto(100, 110);
    g_fillcircle(20);
    g_set_foreground(G_GREEN);
    g_moveto(100, 100);
    g_fillcircle(10);

    g_set_linewidth(3);
    g_set_linestyle(G_SHORT_DASHED);
    g_set_viewport(2, 0.4, 0.0, 0.8, 0.4);
    g_moveto(100, 130);
    g_circle(40);
    g_set_foreground(G_RED);
    g_moveto(100, 120);
    g_circle(30);
    g_set_foreground(G_YELLOW);
    g_moveto(100, 110);
    g_circle(20);
    g_set_foreground(G_GREEN);
    g_moveto(100, 100);
    g_circle(10);

    g_select_viewport(2);
    g_set_world(2,0,0,250,200);
    g_set_viewport(2, 0.0, 0.5, 0.4, 1.0);
    g_set_foreground(G_BLUE);

    g_drawpoly(5, poly);
    g_set_viewport(2, 0.0, 0.4, 0.4, 0.9);
    g_set_foreground(G_MAGENTA);
    g_fillpoly(4, poly);
    g_set_viewport(2, 0.0, 0.3, 0.4, 0.8);
    g_set_foreground(G_GREEN);
    g_fillpoly(4, poly);
    g_set_viewport(2, 0.0, 0.2, 0.4, 0.7);
    g_set_foreground(G_CYAN);
    g_fillpoly(4, poly);

    g_select_viewport(3);
    g_set_world(3,0,0,1000,750);
    g_set_viewport(3, 0.5, 0.5, 1.0, 1.0);
    g_set_foreground(G_RED);
    g_set_linestyle(G_LONG_DASHED);
    g_rectangle(100,100,900,200);
    g_set_linestyle(G_SOLID);
    g_set_linewidth(1);
    g_rectangle(150,50,850,250);
    g_set_foreground(G_BLACK);
    g_fillrectangle(200, 20, 800, 280);
    g_set_foreground(G_WHITE);
    g_fillrectangle(250, 70, 750, 230);

    g_close_object(id);
    g_call_object(id);
}

void TestLabel(void)
{
	static int id;
    int i;

    if (id)
        g_delete_object(id);
    else
        id = g_unique_object();
    g_open_object_byname(id, "TestLabel");
    g_push_gc();
/*
    g_set_font(G_FONT_DEFAULT, G_IN_RELATION_TO_DEVICE);
    g_set_charsize(1);
*/
    title("Test Label");
    g_set_viewport(1, 0, 0, 1, 1);
    g_set_world(1,0,0,400,300);
    g_select_viewport(1);

    for (i=0;i<=20;i++) {
		g_moveto(0,15 * i);
        g_lineto(400, 15 * i);
    }
    for (i=0;i<=20;i++) {
        g_moveto(20 * i, 0);
        g_lineto(20 * i, 300);
    }

    g_moveto(20, 15);
	g_set_font(G_FONT_COURIER, G_RELATIVE_FONTSCALING);
    g_label("Courier size 1");
    g_set_charsize(2);
    g_moveto(20, 90);
    g_set_font(G_FONT_TIMES_ITALIC, G_RELATIVE_FONTSCALING);
    g_label("Times size 2");
    g_moveto(20, 60);
    g_set_font(G_FONT_COURIER_BOLD_ITALIC, G_RELATIVE_FONTSCALING);
    g_label("Courier size 2");
    g_moveto(20, 30);
	g_set_font(G_FONT_HELVETICA, G_RELATIVE_FONTSCALING);
    g_label("Helvetica size 2");
    g_moveto(100, 150);
    g_set_charsize(3.5);
    g_label("Helvetica size 3.5");
    g_moveto(280, 30);
    g_set_charsize(2);
    g_set_textdirection(90);
    g_set_font(G_FONT_HELVETICA, G_RELATIVE_FONTSCALING);
	g_label("Helvetica 2, ROTATE");
    g_pop_gc();

    g_close_object(id);
    g_call_object(id);
}

void TestMouse(void)
{
    long x = 640 ,y = 480;
	float fx, fy;
    char label[80];
    static int id;

    if (id)
        g_delete_object(id);
    else
        id = g_unique_object();
    g_open_object_byname(id, "TestMouse");
	title("Test Mouse Input");
    g_set_viewport(1, 0.2, 0.4, 0.6, 0.8);
    g_set_world(1,0,0,x,y);
    g_select_viewport(1);
    g_moveto(0,0);
    g_lineto(0,y);
    g_lineto(x,y);
    g_lineto(x,0);
    g_lineto(0,0);
    g_moveto(10,100);
	sprintf(label,"Rectangle at (0, 0) and (%ld, %ld)",x,y);
    g_label(label);
    g_moveto(10,50);
    sprintf(label,"Press 'q' to Quit");
    g_label(label);
    g_set_viewport(2, 0.0, 0.0, 1.0, 1.0);
    g_set_world(2,-600,-300,600,600);
    g_select_viewport(2);
    g_rectangle(-600, -300, 600, 600);
	g_moveto(-180, -100);
    g_lineto(-20 , -100);
    g_moveto(-100, -180);
    g_lineto(-100,  -20);
    g_moveto(-100, -100);
    g_circle(80);
    g_moveto(10, -100);
    g_label("Circle Centre at (-100, -100)");
    g_close_object(id);
    g_call_object(id);
	g_set_cursorposition(-100, -100);
    g_get_cursorposition(&fx, &fy);
    printf( "Cursor position x = %.0f y = %.0f\n", fx, fy);
}

void TestColor(void)
{
    char *colornames[] = {
    "G_BLACK",
    "G_BLUE",
    "G_GREEN",
    "G_CYAN",
    "G_RED",
    "G_MAGENTA",
    "G_BROWN",
    "G_LIGHTGRAY",
    "G_DARKGRAY",
    "G_LIGHTBLUE",
    "G_LIGHTGREEN",
    "G_LIGHTCYAN",
    "G_LIGHTRED",
    "G_LIGHTMAGENTA",
    "G_YELLOW",
    "G_WHITE"};
    int i, fp;
    static int id, id2;

    if ((id = g_get_object_id("TestColor")) != 0)
	g_delete_object(id);
    id = g_unique_object();
    g_open_object_byname(id, "TestColor");

/*
{
	G_PALETTEENTRY pal;
	pal.r = 77;
	pal.g = 33;
	pal.b = 10;
	g_set_paletteentry(1, pal);
	g_set_paletteentry(2, pal);
	g_set_paletteentry(3, pal);
	g_set_paletteentry(4, pal);
	g_set_paletteentry(5, pal);
}
*/
    g_set_linestyle(G_SOLID);
    g_set_linewidth(3);
    g_set_world(1,0,0,100,100);
    g_set_viewport(1, 0.0, 0.0, 1.0, 1.0);
    g_select_viewport(1);
    for (i=0; i<g_get_palettesize(); i++) {
	g_set_foreground(G_BLACK);
        g_moveto(10,5*i + 11);
        g_label(colornames[i]);
        g_set_foreground(i);
        g_moveto(10,5*i + 10);
        g_lineto(90,5*i + 10);
    }
    
    if ((id2 = g_get_object_id("SUBOBJECT")) != 0)
	g_delete_object(id2);
    id2 = g_unique_object();
    g_open_object_byname(id2, "SUBOBJECT");
    g_moveto(20,20);
    g_set_foreground(G_BLACK);
    g_label("Yet another object");
    g_close_object(id2);
    g_call_object(id2);

    
    g_close_object(id);
    g_call_object(id);
}

#define ID_QUIT     100
#define ID_UNZOOM   101
#define ID_BUFFER   102
void TestZoom(void)
{
    G_EVENT ui;
    int i, j, ok, zoom, vp_id = 50;
    float x, y;
	static int id;
    int menu_id1, menu_id2, win_id2;

    /* --- restore old gc parameters --- */
    g_pop_gc();
    g_push_gc();
    g_push_gc();

	win_id2 = g_open_window(device,100,200,600,450,
		"SCROLL TEST",
            G_WIN_BUFFER|
             G_WIN_MENUBAR|
	     G_WIN_COMMANDLINE_FOCUS|
	     G_WIN_COMMANDLINE_HIDEHISTORY|
	     G_WIN_BUTTONBAR2|
             G_WIN_SCROLLBAR|
             G_WIN_COMMANDLINE);
/*
G_WIN_MENUBAR | G_WIN_SCROLLBAR |
		G_WIN_PRIMARY_SHELL| G_WIN_COMMANDLINE_RETURN | G_WIN_BUFFER);
*/

	menu_id1 = g_menu_create_menubar(win_id2);
	menu_id2 = g_menu_append_submenu(menu_id1, "&Menu");

	/*
        menu_id2 = g_menu_create_buttonbar(win_id2);
        */

	g_menu_append_button(menu_id2, "&Zoom out", ID_UNZOOM);
	g_menu_append_toggle(menu_id2, "&Set Screen Buffer", ID_BUFFER, TRUE);
	g_menu_append_button(menu_id2, "&Quit", ID_QUIT);

	g_set_clipping(TRUE);
	g_set_cursortype(win_id2, G_CURSOR_CROSSHAIR);
	g_set_world(vp_id,100,0,0,100);
	g_set_world(vp_id + 2, 100, 0, 0, 20);
	g_set_world(vp_id + 3, 0, 0, 40, 100);

	if (id)
		g_delete_object(id);
	else
		id = g_unique_object();
	g_open_object_byname(id, "TestZoom");
	g_set_linestyle(G_SOLID);
	title("ScrollTest");
g_set_textdirection(0);
	g_set_viewport(vp_id, 0.1, 0.2, 0.8, 0.9);
	g_select_viewport(vp_id);
/*    g_clear_viewport();*/
	g_set_linewidth(1);
j=0;

/*
g_set_linestyle(G_SHORT_DASHED);
for (j=0;j<1000;j++)
*/
	for (i=1; i<=10; i++) {
		g_set_foreground(i);
		x = i * 6 + (float)j/1000;
		y = i * 6;
		g_rectangle(x, y, x + i * 2 , y + i * 2);
	}
g_set_linestyle(G_SOLID);
	g_set_viewport(vp_id+1, 0.1, 0.2, 0.801, 0.901);
	g_set_world(vp_id+1, 0,0,100,100);
    g_select_viewport(vp_id+1);
    g_set_linewidth(1);
    g_set_foreground(G_BLACK);
    g_rectangle(0,0,100,100);


    g_set_viewport(vp_id + 2, 0.1, 0.1, 0.8, 0.18);
    g_select_viewport(vp_id + 2);
/*    g_clear_viewport();*/

    g_set_linewidth(1);
    g_set_foreground(G_BLACK);
    g_moveto(0, 15);
    g_lineto(100, 15);
    for (i=0;i<=10;i++) {
        char s[10];
        g_moveto(i * 10, 15);
        g_lineto(i * 10, 10);
		g_moveto(i * 10, 2);
        sprintf(s, "%d", i);
        g_label(s);
    }

    g_set_viewport(vp_id + 3, 0.81, 0.2, 1.0, 0.9);
    g_select_viewport(vp_id + 3);
/*    g_clear_viewport();*/
    g_moveto(5, 0);
    g_lineto(5, 100);
    g_set_textdirection(90);
    for (i=0;i<=10;i++) {
        char s[10];
        g_moveto(5,  i * 10);
        g_lineto(10, i * 10);
        g_moveto(12, i * 10);
        sprintf(s, "%d", i);
        g_label(s);
    }
/*
g_pop_gc();
*/
    g_close_object(id);
    g_call_object(id);

    g_set_viewport(vp_id -1, 0.8, 0.8, 1.0, 1.0);
    g_select_viewport(vp_id -1);
    g_set_world(vp_id-1, 0.9, 0.9, 1.0, 1.0);


    ok = FALSE;
    zoom = 0;
    g_set_commandlocking(win_id2, TRUE);
    g_enable_nextcommand(win_id2);
    do {

        g_get_event(&ui);
        if (ui.win_id == win_id2) switch (ui.event) {
        case G_WINDOWDESTROY:
            ok = TRUE;
            break;
            
        case G_KEYPRESS:
            switch (ui.keycode) {
			case 'b':{
                static int i;
                if (i==0)
                    g_send_event(&ui);
                if (i==1)
                    g_bell();
                i++;
                }
                break;
            case 'q':
                ok = TRUE;
                break;
            case 'w': {
                float x1, y1, x2, y2;
                g_get_world(vp_id, &x1, &y1, &x2, &y2);
                printf("World %d: %.2f %.2f %.2f %.2f\n", vp_id,x1, y1, x2, y2);
                g_get_world(vp_id+2, &x1, &y1, &x2, &y2);
                printf("World %d: %.2f %.2f %.2f %.2f\n", vp_id+2,x1, y1, x2, y2);
                g_get_world(vp_id+3, &x1, &y1, &x2, &y2);
                printf("World %d: %.2f %.2f %.2f %.2f\n", vp_id+3,x1, y1, x2, y2);                
                }
                break;

            }
            break;

        case G_BUTTON1PRESS:
            if (ui.vp_id != vp_id) {
                if (zoom) {
                    g_set_rubbercursor(win_id2, vp_id, ui.x, ui.y, G_RUBBER_NONE);
                    zoom = 0;
                }
                g_bell();
                break;
            }
            (zoom) ? (zoom = 0) : (zoom = 1);
            if (zoom) {
                x = ui.x;
                y = ui.y;
                g_set_rubbercursor(win_id2, vp_id, ui.x, ui.y, G_RUBBER_BOX);
            }
            else {
                float x1, y1, x2, y2;

                x1 = x;
                y1 = y;
                x2 = ui.x;
                y2 = ui.y;
if (fabs(x1-x2) <0.02) break;
if (fabs(y1-y2) <0.02) break;

                g_scrollbar_connect(win_id2, vp_id, x1, y1, x2, y2, 
                                    G_SCROLL_CLEARVIEWPORT);
                g_scrollbar_connect(win_id2, vp_id + 2, x1, 0, x2, 20, 
                                    G_SCROLL_CLEARVIEWPORT);
                g_scrollbar_connect(win_id2, vp_id + 3, 0, y1, 40, y2,
                                    G_SCROLL_CLEARVIEWPORT | G_SCROLL_LASTVIEWPORT);
                g_set_rubbercursor(win_id2, vp_id, ui.x, ui.y, G_RUBBER_NONE);
            }
            break;

	case G_BUTTON3PRESS: 
		if (ui.vp_id == vp_id && !zoom)
			g_set_rubbercursor(win_id2, vp_id, ui.x, ui.y, G_RUBBER_PANNER_BORDER);
		break;

        case G_BUTTON3RELEASE:
            g_set_rubbercursor(win_id2, vp_id, ui.x, ui.y, G_RUBBER_NONE);
            break;

        case G_COMMAND:

            switch (ui.keycode) {

            case ID_QUIT:
                ok = TRUE;
                break;

            case ID_UNZOOM:
                g_scrollbar_reset(win_id2);
                break;

            case ID_BUFFER:
                g_set_windowbuffer(win_id2,g_menu_get_toggle(menu_id2, ID_BUFFER));
                break;
            }
            break;

        case G_COMMANDLINE:
            {
                char *p;
                char s[20];
                static int i;

                i++;
                p = ui.command;
                if (p != NULL)
                    fprintf(stderr,"%s\n", p);

                sprintf(s,"Default %d", i);
                g_set_commandline(win_id2, s);
                sprintf(s, "Prompt %d",i);
                g_set_commandprompt(win_id2,s);
                g_enable_nextcommand(win_id2);
            }
            break;

        }
    }
    while (!ok);
    g_scrollbar_disconnect(win_id2);
    g_close_window(win_id2);
    /* --- restore old gc parameters --- */
    g_pop_gc();
    g_push_gc();
}

void TestLinestyle(void)
{
    int linestyle[] = {
    G_SOLID,
    G_SHORT_DASHED,
    G_LONG_DASHED,
    G_DOTTED};
    char *stylenames[] = {
    "G_SOLID",
    "G_SHORT_DASHED",
    "G_LONG_DASHED",
    "G_DOTTED"};
    int linewidth[] = {1, 3, 5, 7};
    char label[40];
    int i,j,k;
    static int id;

    if (id)
        g_delete_object(id);
    else
        id = g_unique_object();
    g_open_object_byname(id, "TestLinestyle");
    title("Test Linestyle");
    g_set_viewport(1, 0.0, 0.0, 1.0, 1.0);
    g_set_world(1,0,0,100,100);
    g_select_viewport(1);
    g_set_linestyle(G_SOLID);
    for (j=0; j<3; j++) {
        g_set_linewidth(linewidth[j]);
        for (i=0; i<4; i++) {
            k = 5 * (i + j * 5);
            g_set_linestyle(G_SOLID);
            g_moveto(10,k + 11);
            g_label(stylenames[i]);
            g_set_linestyle(linestyle[i]);
            g_moveto(10,k + 10);
            g_lineto(90,k + 10);
        }
        k = 5 * (i + j * 5);
        g_moveto(10, k + 11);
        sprintf(label, "=== LINE WIDTH %d ===", linewidth[j]);
        g_label(label);
    }
    g_close_object(id);
    g_call_object(id);
}

void TestCopyWindow(int win_id, int device)
{
    int dest_win_id;
    FILE *outfile;

    switch (device) {
        case G_POSTSCRIPT:
	    outfile = fopen("gentest.eps", "w+");
	    g_set_outfile(outfile);
            /* -- A3 -- */
            g_set_devicesize(G_POSTSCRIPT, 42.0, 29.7);
            /* -- A4 -- */
            g_set_devicesize(G_POSTSCRIPT, 29.7, 21.0);
/* 
	    dest_win_id = g_open_window(G_POSTSCRIPT,1.0, 1.0, 16.0, 12.0,
					  "TEST Copy",
					  G_WIN_BLACK_ON_WHITE);
*/
	    dest_win_id = g_open_window(G_POSTSCRIPT,2.0, 2.0, 16.0, 12.0,
					  "TEST Copy",
					  G_WIN_EPS /*| G_WIN_PORTRAIT*/);
            break;

	case G_HPGL:
	    /* -- A3 -- */
	    g_set_devicesize(G_HPGL, 42.0, 29.7);
	    /* -- A4 -- */
	    g_set_devicesize(G_HPGL, 29.7, 21.0);
	    outfile = fopen("gentest.hp", "w+");
	    g_set_outfile(outfile);
	    dest_win_id = g_open_window(G_HPGL,1.0, 1.0, 16.0, 12.0,
					  "TEST Copy",
					   G_WIN_PCL5 );
	    break;

	case G_WMF:
	    /* -- A3 -- */
	    g_set_devicesize(G_WMF, 42.0, 29.7);
	    /* -- A4 -- */
	    g_set_devicesize(G_WMF, 29.7, 21.0);
	    outfile = fopen("gentest.wmf", "w+");
	    g_set_outfile(outfile);
	    dest_win_id = g_open_window(G_WMF,1.0, 1.0, 16.0, 12.0,
					  "TEST Copy",
					   0 );
	    break;


	case G_SCREEN:
	    dest_win_id = g_open_window(device,20,40,600,450,
					  "TEST Copy",0 );
	    break;

	case G_PRINTER:
	    dest_win_id = g_open_window(device,3.0, 5.0,12.0, 9.0,
					  "TEST Copy",0 );
	    break;

	default :
		return;
    }
    g_copy_window(dest_win_id, win_id);
    if (device == G_SCREEN)
	pause();
    g_close_window(dest_win_id);
    if (device != G_SCREEN && device != G_PRINTER)
	fclose(outfile);
    if (device == G_PRINTER)
        g_close_device(G_PRINTER);
    g_select_window(win_id);
}

void TestConsole(int win_id)
{
	G_EVENT ui;
	int i,ok = FALSE;
	int menu_id1, menu_id2;

	id_cons = g_open_window(G_SCREEN,10,10,400,200,
	   "CONSOLE", G_WIN_CONSOLE |G_WIN_MENUBAR |G_WIN_POINTERFOCUS|
				  G_WIN_COMMANDLINE /*_RETURN */|G_WIN_PRIMARY_SHELL);
	menu_id1 = g_menu_create_menubar(id_cons);
	menu_id2 = g_menu_append_submenu(menu_id1, "&Menu");
	g_menu_append_button(menu_id2, "&Quit", ID_QUIT);

	g_console_printf(id_cons,"Enter 'q' to quit\n");
	do {
		g_get_event(&ui);
		switch (ui.event) {
                case G_WINDOWDESTROY:
                    ok = TRUE;
                    break;

		case G_COMMANDLINE:
			g_console_printf(id_cons, "The last command was: %s\n", ui.command);
			if (ui.command[0] == 'q')
				ok = TRUE;
			if (ui.command[0] == 'r')
				g_raise_xterm();
			if (ui.command[0] == 'l')
				g_lower_xterm();
			if (ui.command[0] == 's')
		for (i=0;i<1000;i++)
		    g_console_printf(id_cons, "Test %d\n",i);
	    break;
	case G_COMMAND:
	    if (ui.keycode == ID_QUIT)
		ok = TRUE;
	    break;
	}
    } while (!ok);
    g_close_window(id_cons);
    g_select_window(win_id);
/*
#ifdef _WIN32
    g_select_window(id_cons);
#endif
    printf("TEST >");
    gets(buffer);
    printf("\"%s\"\n", buffer);
    puts("End Test Console\n");
#ifdef _WIN32
    g_select_window(win_id);
#endif
*/
}

typedef struct tagBARBUTTON {
    int   id;
    int   status;
    int   vp_id;
    float x1,y1,x2,y2;
    char  label[20];
} BARBUTTON;

void TestButtonBar(void)
{
    G_EVENT ui;
    int j, ok, result;
    int win_id3, obj_id;
    static BARBUTTON but[4];
    int fcolor, bcolor;

    win_id3 = g_open_window(device,20,30,50,200,
					            "TEST ButtonBar", 0 );
    fcolor = g_get_foreground();
    bcolor = g_get_background();

    for (j=0;j<4;j++) {
        but[j].vp_id =j + 20;
        but[j].x1 = 0.0;
        but[j].x2 = 1.0;
        but[j].y1 = j * 0.25;
        but[j].y2 = but[j].y1 + 0.25;
        sprintf(but[j].label, "Button %d", j+1);
        but[j].status = FALSE;
        but[j].id = g_open_object(g_unique_object());
        g_rectangle(0, 0, 100, 100);
        g_moveto(10, 40);
        g_label(but[j].label);
        g_close_object(but[j].id);
    }
    obj_id = g_open_object(g_unique_object());
    for (j=0;j<4;j++) {
        g_set_viewport(but[j].vp_id, but[j].x1, but[j].y1, but[j].x2, but[j].y2);
        g_set_world(but[j].vp_id, 0, 0, 100, 100);
        g_select_viewport(but[j].vp_id);
        g_call_object(but[j].id);
    }
    g_close_object(obj_id);
    g_call_object(obj_id);
    ok = FALSE;
    while (!ok) {
        result = g_get_event(&ui);
        switch (result) {
            case G_WINDOWDESTROY:
                ok = TRUE;
                break;
            case G_KEYPRESS:
                ok = TRUE;
                break;
            case G_BUTTON1PRESS:
                if (ui.vp_id == G_ERROR)
                    break;
                for (j=0;j<4;j++)
                    if (but[j].vp_id == ui.vp_id)
                        break;
                if (j==4)
                    break;

                g_delete_object(but[j].id);
                g_open_object(but[j].id);
                if (but[j].status) {
                    g_set_foreground(bcolor);
                    g_fillrectangle(0, 0, 100, 100);
                    g_set_foreground(fcolor);
                    g_rectangle(0, 0, 100, 100);
                    g_moveto(10,40);
                    g_label(but[j].label);
                    but[j].status = FALSE;
                }
                else {
                    g_set_foreground(G_GREEN);
                    g_fillrectangle(0, 0, 100, 100);
                    g_set_foreground(fcolor);
                    g_moveto(10,40);
                    g_label(but[j].label);
                    but[j].status = TRUE;
                }
                g_close_object(but[j].id);
                g_plotall();
                break;
        }
    }
    for (j=0;j<4;j++)
        g_delete_object(but[j].id);
    g_delete_object(obj_id);
    g_close_window(win_id3);
}



int bla_id;

void mycallback(G_POPUP_CHILDINFO *ci)
{
    switch (ci->type) {
		case G_CHILD_TEXTBOX:
			printf("id = %d %s\n",ci->id,ci->label);
			break;
		case G_CHILD_CHECKBOX:
			printf("CHECK: id = %d item = %d  state = %d \n",
                            ci->id,ci->item,ci->select[ci->item]);
			break;
		case G_CHILD_PUSHBUTTON:
/*
g_popup_set_selection(ci->cont_id,bla_id, 0);
                  break;
      */
		case G_CHILD_OPTIONMENU:
		case G_CHILD_LISTBOX:
		case G_CHILD_SPINBOXTEXT:
		case G_CHILD_RADIOBOX:
		case G_CHILD_SPINBOX:
		case G_CHILD_SCALE:
			printf("id = %d item = %d \n",
                            ci->id,ci->item);
			break;
	}
/*
//	g_popup_enable_item(ci->id, FALSE);


//	g_popup_set_selection(ci->id, 0);
//	g_popup_set_label(ci->id,"GNA");
//	g_popup_set_label(0,"GNA,,,,");
//	g_popup_set_title(ci->id,"Title");
//	g_popup_set_checkmark(ci->id,0,1);
//	g_popup_set_focus(6);
*/
}

void TestContainer(int win_id)
{

	int i;
	static char title[] = "Any Title";
	static char text[] = "Test ";
	static char label[] = "Just a bit of text";
	static char *radio[] = {"one","two","three 123456789","four"};
	static char *check[] = {"Check item 1","Check item 2","Check item 3"};
	static int check_select[] = {0,1,0};
      static int scale_range[4] = {30, -100, 0};
/*
      static int scale_range[4] = {10, 100, 0};

*/      static int spin_range[4] = {10, 100, 2, 2};
	static char *list[] = {"List item one","List item two",
                   "List item three","List item four"};
	static G_POPUP_CHILDINFO ci[20];
	static int id,cont_id,ok;

 /*
 if (!cont_id){
 */
	cont_id = g_popup_container_open(win_id, "Container Box",G_POPUP_WAIT|G_POPUP_TAB);

	id = 0;

	g_popup_init_info(&(ci[id]));
	ci[id].type  = G_CHILD_TAB;
	ci[id].label = "Tab 1";
	g_popup_add_child(cont_id, &(ci[id]));

       id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type  = G_CHILD_PANEL;
	ci[id].item  = 1;
	ci[id].title = label;
	ci[id].horizontal  = 1;
	ci[id].frame      = TRUE;
	g_popup_add_child(cont_id, &(ci[id]));

	id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type  = G_CHILD_PUSHBUTTON;
	ci[id].id    = id;
	ci[id].label = label;
	ci[id].func       = mycallback;
	ci[id].userdata   = (void*)label;
	g_popup_add_child(cont_id, &(ci[id]));

      id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type  = G_CHILD_LABEL;
	ci[id].id    = id;
	ci[id].label = label;
	ci[id].title      = title;
	ci[id].frame      = TRUE;
	g_popup_add_child(cont_id, &(ci[id]));


      id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type  = G_CHILD_PANEL;
	ci[id].item  = 0;
	g_popup_add_child(cont_id, &(ci[id]));

      id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type  = G_CHILD_SCALE;
	ci[id].id    = id;
	ci[id].label = label;
	ci[id].frame       = 1;
	ci[id].item       = 20;
      ci[id].select = scale_range;
      ci[id].items_visible = 10;
      ci[id].item_count = 10;
      ci[id].horizontal = TRUE;
	ci[id].func       = mycallback;
	g_popup_add_child(cont_id, &(ci[id]));


      id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type  = G_CHILD_SPINBOX;
	ci[id].id    = id;
	ci[id].label = label;
	ci[id].frame       = 0;
	ci[id].item       = 20;
	ci[id].horizontal = TRUE;
      ci[id].select = spin_range;
      ci[id].items_visible = 10;
	ci[id].func       = mycallback;
	g_popup_add_child(cont_id, &(ci[id]));

      id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type  = G_CHILD_TAB;
	ci[id].label = "Tab 2";
	g_popup_add_child(cont_id, &(ci[id]));


      id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type  = G_CHILD_SPINBOXTEXT;
	ci[id].id    = id;
	ci[id].label = label;
	ci[id].item       = 2;
	ci[id].item_count  = 4;
       ci[id].data = list; 
       ci[id].items_visible = 20;
	ci[id].func       = mycallback;
	g_popup_add_child(cont_id, &(ci[id]));
 bla_id=id;
#ifdef bbb
	id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type       = G_CHILD_RADIOBOX;
	ci[id].id         = id;
	ci[id].title      = title;
	ci[id].frame      = TRUE;
	ci[id].item_count = 4;
	ci[id].data       = radio;
	ci[id].item       = 3;
	ci[id].horizontal = TRUE;
	ci[id].func       = mycallback;
	g_popup_add_child(cont_id, &(ci[id]));
#endif
	id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type       = G_CHILD_MULTILINETEXT;
	ci[id].id         = id;
	ci[id].title      = title;
	ci[id].frame      = TRUE;
	ci[id].item_count = 8;
	ci[id].items_visible = 20;
	ci[id].label = label;
	ci[id].func       = mycallback;
	g_popup_add_child(cont_id, &(ci[id]));


      id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type  = G_CHILD_TAB;
	ci[id].label = "Tab 3";
	g_popup_add_child(cont_id, &(ci[id]));

	id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type       = G_CHILD_TEXTBOX;
	ci[id].id         = id;
	ci[id].title      = title;
	ci[id].frame      = TRUE;
	ci[id].item_count = 20;
	ci[id].items_visible = 20;
	ci[id].label = label;
	ci[id].func       = mycallback;
	g_popup_add_child(cont_id, &(ci[id]));

#ifdef blaa
	id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type = G_CHILD_SEPARATOR;
	g_popup_add_child(cont_id, &(ci[id]));

	id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type          = G_CHILD_LISTBOX;
	ci[id].id            = id;
	ci[id].item_count    = 4;
	ci[id].data          = list;
	ci[id].item          = 2;
	ci[id].items_visible = 3;
	ci[id].func          = mycallback;
	g_popup_add_child(cont_id, &(ci[id]));
#endif

	id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type = G_CHILD_CHECKBOX;
	ci[id].id         = id;
	ci[id].title      = label;
	ci[id].frame      = TRUE;
	ci[id].item_count = 3;
	ci[id].data       = check;
	ci[id].select     = check_select;
	ci[id].func       = mycallback;
	g_popup_add_child(cont_id, &(ci[id]));

	id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type       = G_CHILD_OPTIONMENU;
	ci[id].id         = id;
	ci[id].item_count = 4;
	ci[id].data       = radio;
	ci[id].item       = 3;
	ci[id].horizontal = TRUE;
      ci[id].title = "balalal";
	ci[id].func       = mycallback;
	g_popup_add_child(cont_id, &(ci[id]));

	id++;
	g_popup_init_info(&(ci[id]));
	ci[id].type       = G_CHILD_OPTIONMENU;
	ci[id].id         = id;
	ci[id].item_count = 2;
	ci[id].label      = label;
	ci[id].data       = radio;
	ci[id].item       = 1;
	ci[id].horizontal = TRUE;
      ci[id].frame      = TRUE;
	ci[id].func       = mycallback;
	g_popup_add_child(cont_id, &(ci[id]));
 

 /*
  }
  */
	ok = g_popup_container_show(cont_id);

	if (ok) for (i=0;i<=id;i++) {
		switch (ci[i].type) {
		case G_CHILD_TEXTBOX:
			printf("id = %d %s\n",ci[i].id,ci[i].label);
			break;
		case G_CHILD_CHECKBOX:{
			int j;
			for (j=0;j<ci[i].item_count;j++)
			   printf("id = %d item = %d  state = %d \n",ci[i].id,
							 j ,ci[i].select[j]);
			}
			break;
		case G_CHILD_SCALE:
		case G_CHILD_OPTIONMENU:
		case G_CHILD_LISTBOX:
		case G_CHILD_SPINBOX:
		case G_CHILD_SPINBOXTEXT:
		case G_CHILD_RADIOBOX:
			printf("id = %d item = %d \n",ci[i].id,ci[i].item);
			break;
		}
	}

}



int handler(G_EVENT *ui, G_CALLDATA call_data)
{
	void (*func)(void) = (void*) call_data;

	func();
	return TRUE;
}


#define PIXBUTTONS
#include "pixmaps.h"
#include "psnd_defs.h"

static void psnd_setup_buttonbar(int win_id)
{
   int id = 444; 
   int bar_id = g_menu_create_buttonbox2(win_id,3);
/* Start of Row 1 */
/*
#ifdef PIXBUTTONS
    g_menu_append_pixbutton(bar_id, divx_pix, divx_pix, PSND_XDIV2);
    g_menu_append_pixbutton(bar_id, mulx_pix, mulx_pix, PSND_XMUL2);
    g_menu_append_pixbutton(bar_id, divy_pix, divy_pix, PSND_YDIV2);
    g_menu_append_pixbutton(bar_id, muly_pix, muly_pix, PSND_YMUL2);
#else
    g_menu_append_button(bar_id, "< X >", PSND_XDIV2);
    g_menu_append_button(bar_id, " >X< ", PSND_XMUL2);
    g_menu_append_button(bar_id, "< Y >", PSND_YDIV2);
    g_menu_append_button(bar_id, " >Y< ", PSND_YMUL2);
#endif
*/
    g_menu_append_button(bar_id, "Full ", PSND_RESET);
    g_menu_append_toggle(bar_id, "Scale", PSND_SC, id++);
    g_menu_append_button(bar_id, "Mouse", PSND_CURSOR);
    g_menu_append_button(bar_id, "ZoomU", PSND_UNDO_ZOOM);
    g_menu_append_button(bar_id, "ZoomR", PSND_REDO_ZOOM);
    g_menu_append_separator(bar_id);
    g_menu_append_toggle(bar_id, " 2D  ", PSND_CO, FALSE);
    g_menu_append_toggle(bar_id, "Scale", PSND_SC_2D, id++);
    g_menu_append_button(bar_id, " Row ", PSND_ROW_COLUMN);
    g_menu_append_button(bar_id, "CL+/-", PSND_LEVELS_BOTH);
    g_menu_append_toggle(bar_id, "Phase", PSND_PHASE_2D, id++);

g_menu_append_separator(bar_id);

/* Start of Row 2 */  
/*  
         g_menu_append_button(bar_id, "ShwWn", PSND_SHOW_WINDOW);
*/

/*
    g_menu_append_button(bar_id, "     ", 0);
    g_menu_append_button(bar_id, " Win", PSND_DO_WINDOW);
    g_menu_append_button(bar_id, " FT", PSND_FT);
    g_menu_append_button(bar_id, " PK", PSND_PK);
*/
/*
       g_menu_append_button(bar_id, "ShwBc", PSND_SHOW_BASELINE);
*/
    g_menu_append_button(bar_id, "     ", 0);
    g_menu_append_button(bar_id, " BC", PSND_BASELINE);
    g_menu_append_button(bar_id, " PL", PSND_PL);
    g_menu_append_button(bar_id, " RN"  , PSND_RN);
    g_menu_append_button(bar_id, " DR"  , PSND_DR);

    g_menu_append_separator(bar_id);

    g_menu_append_button(bar_id, " CP", PSND_CP);
    g_menu_append_button(bar_id, " Up  ", PSND_UP);
    g_menu_append_button(bar_id, " Crop", PSND_CROP);
    g_menu_append_button(bar_id, "PlnUp", PSND_PLANE_UP);
    g_menu_append_button(bar_id, "Pln U", PSND_PLANE_UNDO);

g_menu_append_separator(bar_id);

/* Start of Row 3 */

    g_menu_append_button(bar_id, "  +  ", PSND_DOKEY_PLUS);
    g_menu_append_button(bar_id, "  -  ", PSND_DOKEY_MINUS);
    g_menu_append_button(bar_id, " Undo", PSND_UNDO);
    g_menu_append_button(bar_id, " Redo", PSND_REDO);

    g_menu_append_button(bar_id, " AU  ", PSND_AU);
#ifdef PIXBUTTONS
    g_menu_append_pixbutton(bar_id, script_pix, script_pix, PSND_SCRIPT);
#else
    g_menu_append_button(bar_id, "Scrip", PSND_SCRIPT);
#endif
    g_menu_append_button(bar_id, "BookM", PSND_POPUP_BOOKMARK);
    g_menu_append_toggle(bar_id, " PL+", PSND_APPEND_PLOT, id++);
#ifdef PIXBUTTONS
    g_menu_append_pixtoggle(bar_id, grid_pix, grid_gray_pix, 
                                         grid_pix, grid_gray_pix, 
                                         PSND_SHOW_GRID, id++);
#else
    g_menu_append_toggle(bar_id, "  #  ", PSND_SHOW_GRID, id++);
#endif

    g_menu_append_separator(bar_id);

    g_menu_append_button(bar_id, "PL 2D", PSND_CP2);
    g_menu_append_button(bar_id, " Down", PSND_DOWN);
    g_menu_append_button(bar_id, "UCrop", PSND_UNCROP);
    g_menu_append_button(bar_id, "PlnDn", PSND_PLANE_DOWN);
    g_menu_append_button(bar_id, "Pln R", PSND_PLANE_REDO);
 
g_menu_append_separator(bar_id);

    g_menu_enable_item(bar_id, PSND_CO, FALSE);
 }




#define ID_BEEP         0
#define ID_EXIT         2
#define ID_LINESTYLE    3
#define ID_CLIP         4
#define ID_COLOR        5
#define ID_MOUSE        6
#define ID_LABEL        7
#define ID_POLY         8
#define ID_VIEWPORT     9
#define ID_CONSOLE      10
#define ID_BUTTONBAR    11
#define ID_ZOOM         12
#define ID_MESSAGE      13
#define ID_GETFILE      14
#define ID_YESNO	15
#define ID_PROMPT	16
#define ID_SAVEAS       17
#define ID_YESNOCANCEL	18
#define ID_LISTBOX	19
#define ID_RADIOBOX	20
#define ID_CHECKBOX     21
#define ID_CONTAINER	22
#define ID_READFILE	23
#define ID_WRITEFILE	24

#define ID_POPMENU1	50
#define ID_POPMENU2	51
#define ID_POPMENU3	52
#define ID_POPMENU4	53

#define ID_SETLABEL	60
#define ID_MENUBAR2	61
#define ID_TEST		62

#define ID_TOGGLE_CURSOR       200
#define ID_TOGGLE_CLIPPING     201
#define ID_TOGGLE_ALTERNATE    202
#define ID_TOGGLE_SCREENBUF    203
#define ID_TOGGLE_TEXTROTATION 204

#define ID_SCREEN       30
#define ID_HPGL         31
#define ID_POSTSCRIPT   42
#define ID_WMF		43
#define ID_PRINTER		44

void gna(int win_id, int gnak)
{
    int menubar_id, menub_id1, menub_id2, menub_id3, menub_id4, menub_id5;
    int menupopup_id, menupopup2_id, menupopup3_id;
    int menubar2_id, menub2_id1;

    if (gnak == 1) {
        g_menu_destroy_menubar2_children(win_id);
        return;
    }
    if (gnak == 2) {
        g_menu_destroy_menubar2(win_id);
        return;
    }
    menubar_id = g_menu_create_menubar2(win_id);
    menub_id1 = g_menu_append_submenu(menubar_id, "&Tests");
    g_menu_append_button(menub_id1, "&Linestyle", ID_LINESTYLE);
    g_menu_append_button(menub_id1, "&Clip", ID_CLIP);
    g_menu_append_button(menub_id1, "C&olor", ID_COLOR);
    g_menu_append_button(menub_id1, "Mo&use", ID_MOUSE);
    g_menu_append_button(menub_id1, "L&abel", ID_LABEL);
    g_menu_append_button(menub_id1, "&Polygon", ID_POLY);
    g_menu_append_button(menub_id1, "&Viewport", ID_VIEWPORT);
    g_menu_append_button(menub_id1, "Co&nsole", ID_CONSOLE);
    g_menu_append_button(menub_id1, "&Zoom", ID_ZOOM);
    g_menu_append_button(menub_id1, "&Beep", ID_BEEP);
    g_menu_append_button(menub_id1, "Bu&tton Bar", ID_BUTTONBAR);
    g_menu_append_separator(menub_id1);
    g_menu_append_button(menub_id1, "&Read Object", ID_READFILE);
    g_menu_append_button(menub_id1, "&Write Object", ID_WRITEFILE);
    g_menu_append_separator(menub_id1);
    menub_id3 = g_menu_append_submenu(menub_id1, "Cop&y Window");
    g_menu_append_button(menub_id3, "to &Screen", ID_SCREEN);
    g_menu_append_button(menub_id3, "to &HPGL", ID_HPGL);
    g_menu_append_button(menub_id3, "to &PostScript", ID_POSTSCRIPT);
    g_menu_append_button(menub_id3, "to &Window Meta File", ID_WMF);
    g_menu_append_separator(menub_id1);
    g_menu_append_button(menub_id1, "E&xit", ID_EXIT);

    menub_id2 = g_menu_append_submenu(menubar_id, "T&oggles");
    g_menu_append_toggle(menub_id2, "&Cursor Crosshair",
												ID_TOGGLE_CURSOR, TRUE);
    g_menu_append_toggle(menub_id2, "&Enable Clipping",
				   ID_TOGGLE_CLIPPING, current_clip);
    g_menu_append_toggle(menub_id2, "&Alternate Cursor", ID_TOGGLE_ALTERNATE, FALSE);
    g_menu_append_toggle(menub_id2, "&Use Screen Buffer", ID_TOGGLE_SCREENBUF, TRUE);
    g_menu_append_toggle(menub_id2, "&Real textrotation in Motif", ID_TOGGLE_TEXTROTATION, FALSE);
    menub_id4 = g_menu_append_submenu(menubar_id, "&Popup");
    g_menu_append_button(menub_id4, "Message Box", ID_MESSAGE);
    g_menu_append_button(menub_id4, "Yes No Box", ID_YESNO);
    g_menu_append_button(menub_id4, "Yes No Cancel Box", ID_YESNOCANCEL);
    g_menu_append_button(menub_id4, "Prompt Dialog", ID_PROMPT);
    g_menu_append_button(menub_id4, "Get Filename", ID_GETFILE);
    g_menu_append_button(menub_id4, "Save as ..", ID_SAVEAS);
    g_menu_append_button(menub_id4, "List Box", ID_LISTBOX);
    g_menu_append_button(menub_id4, "Radio Box", ID_RADIOBOX);
    g_menu_append_button(menub_id4, "Check Box", ID_CHECKBOX);
    g_menu_append_button(menub_id4, "Container Box", ID_CONTAINER);

}

void TestMenus(void)
{
    G_EVENT ui;
    int win_id, ok, result, alternate_cursor;
    int buttonbar_id;
    int menubar_id, menub_id1, menub_id2, menub_id3, menub_id4, menub_id5;
    int menupopup_id, menupopup2_id, menupopup3_id;
    int menubar2_id, menub2_id1;
    int show=0;
    win_id = g_open_window(device,230,160,600,400,"MENU TEST",
    /*
//		   G_WIN_POINTERFOCUS | G_WIN_MENUBAR | G_WIN_BUTTONBAR_32
//		 | G_WIN_BUFFER | G_WIN_COMMANDLINE_RETURN);

//win_id = g_open_window(device,230,60,600,400,"MENU TEST", G_WIN_MENUBAR |
//		   G_WIN_POINTERFOCUS | G_WIN_BUFFER  | G_WIN_PRIMARY_SHELL);
	   //  G_WIN_COMMANDLINE_FOCUS|
	    // G_WIN_COMMANDLINE_HIDEHISTORY|
           //  G_WIN_SCROLLBAR//|
*/
             G_WIN_BUFFER|
             G_WIN_MENUBAR |
	     G_WIN_BUTTONBAR2 |
             G_WIN_COMMANDLINE
               );

    menubar_id = g_menu_create_menubar(win_id);
    menub_id1 = g_menu_append_submenu(menubar_id, "&Tests");
    g_menu_append_button(menub_id1, "&Linestyle	LS", ID_LINESTYLE);
    g_menu_append_button(menub_id1, "&Clip	CP", ID_CLIP);
    g_menu_append_button(menub_id1, "C&olor	CO", ID_COLOR);
    g_menu_append_button(menub_id1, "Mo&use", ID_MOUSE);
    g_menu_append_button(menub_id1, "L&abel", ID_LABEL);
    g_menu_append_button(menub_id1, "&Polygon", ID_POLY);
    g_menu_append_button(menub_id1, "&Viewport", ID_VIEWPORT);
    g_menu_append_button(menub_id1, "Co&nsole", ID_CONSOLE);
    g_menu_append_button(menub_id1, "&Zoom", ID_ZOOM);
    g_menu_append_button(menub_id1, "&Beep", ID_BEEP);
    g_menu_append_button(menub_id1, "Bu&tton Bar", ID_BUTTONBAR);
    g_menu_append_separator(menub_id1);
    g_menu_append_button(menub_id1, "&Read Object", ID_READFILE);
    g_menu_append_button(menub_id1, "&Write Object", ID_WRITEFILE);
    g_menu_append_separator(menub_id1);
    menub_id3 = g_menu_append_submenu(menub_id1, "Cop&y Window");
    g_menu_append_button(menub_id3, "to &Screen", ID_SCREEN);
    g_menu_append_button(menub_id3, "to &HPGL", ID_HPGL);
    g_menu_append_button(menub_id3, "to &PostScript", ID_POSTSCRIPT);
    g_menu_append_button(menub_id3, "to &Window Meta File", ID_WMF);
    g_menu_append_button(menub_id3, "to Prin&ter", ID_PRINTER);
    g_menu_append_separator(menub_id1);
    g_menu_append_button(menub_id1, "E&xit", ID_EXIT);

    menub_id2 = g_menu_append_submenu(menubar_id, "T&oggles");
    g_menu_append_toggle(menub_id2, "&Cursor Crosshair",
												ID_TOGGLE_CURSOR, TRUE);
    g_menu_append_toggle(menub_id2, "&Enable Clipping",
				   ID_TOGGLE_CLIPPING, current_clip);
    g_menu_append_toggle(menub_id2, "&Alternate Cursor", ID_TOGGLE_ALTERNATE, FALSE);
    g_menu_append_toggle(menub_id2, "&Use Screen Buffer", ID_TOGGLE_SCREENBUF, TRUE);
    g_menu_append_toggle(menub_id2, "&Real textrotation in Motif", ID_TOGGLE_TEXTROTATION, FALSE);
    menub_id4 = g_menu_append_submenu(menubar_id, "&Popup");
    g_menu_append_button(menub_id4, "Message Box", ID_MESSAGE);
    g_menu_append_button(menub_id4, "Yes No Box", ID_YESNO);
    g_menu_append_button(menub_id4, "Yes No Cancel Box", ID_YESNOCANCEL);
    g_menu_append_button(menub_id4, "Prompt Dialog", ID_PROMPT);
    g_menu_append_button(menub_id4, "Get Filename", ID_GETFILE);
    g_menu_append_button(menub_id4, "Save as ..", ID_SAVEAS);
    g_menu_append_button(menub_id4, "List Box", ID_LISTBOX);
    g_menu_append_button(menub_id4, "Radio Box", ID_RADIOBOX);
    g_menu_append_button(menub_id4, "Check Box", ID_CHECKBOX);
    g_menu_append_button(menub_id4, "Container Box", ID_CONTAINER);
    menub_id5 = g_menu_append_submenu(menubar_id, "&SetPopupMenu");
    g_menu_append_button(menub_id5, "Popup Menu 1", ID_POPMENU1);
    g_menu_append_button(menub_id5, "Popup Menu 2", ID_POPMENU2);
    g_menu_append_button(menub_id5, "Popup Menu 3", ID_POPMENU3);
    g_menu_append_button(menub_id5, "No Popup Menu", ID_POPMENU4);

     menupopup_id = g_menu_create_popup(win_id);
    g_menu_append_button(menupopup_id, "L&abel", ID_LABEL);
    g_menu_append_button(menupopup_id, "&Polygon", ID_POLY);

    menupopup2_id = g_menu_create_popup(win_id);
    g_menu_append_button(menupopup2_id, "&Linestyle", ID_LINESTYLE);
    g_menu_append_button(menupopup2_id, "C&olor", ID_COLOR);
    g_menu_append_button(menupopup2_id, "L&abel", ID_LABEL);
    g_menu_append_button(menupopup2_id, "&Polygon", ID_POLY);
    g_menu_append_button(menupopup2_id, "&Beep", ID_BEEP);

    menupopup3_id = g_menu_create_popup(win_id);
    g_menu_append_button(menupopup3_id, "&Linestyle", ID_LINESTYLE);
    g_menu_append_button(menupopup3_id, "C&olor", ID_COLOR);
    g_menu_append_button(menupopup3_id, "L&abel", ID_LABEL);
    g_menu_append_button(menupopup3_id, "&Polygon", ID_POLY);

{
#include "buttons_xpm.h"
#ifdef _WIN32
#include "pixmaps\zip.xpm"
#include "pixmaps\zip_gray.xpm"
#include "pixmaps\toolbox.xpm"
#include "pixmaps\toolbox_gray.xpm"
#include "pixmaps\disk.xpm"
#include "pixmaps\disk_gray.xpm"
#include "pixmaps\disk_sel.xpm"
#else
#include "pixmaps/zip.xpm"
#include "pixmaps/zip_gray.xpm"
#include "pixmaps/toolbox.xpm"
#include "pixmaps/toolbox_gray.xpm"
#include "pixmaps/disk.xpm"
#include "pixmaps/disk_gray.xpm"
#include "pixmaps/disk_sel.xpm"
#endif
/*
#include "pixmaps/alltry.xpm"
*/
typedef enum  {
    ID_MOUSE_POS = 1288,
    ID_MOUSE_SELECT, 
    ID_MOUSE_BUFFER1, 
    ID_MOUSE_ZOOMXY,
    ID_MOUSE_ZOOMX, 
    ID_MOUSE_ZOOMY,
    ID_MOUSE_PHASE_P0,
    ID_MOUSE_PHASE_P1,
    ID_MOUSE_PHASE_I0,
    ID_MOUSE_CALIBRATE,
    ID_MOUSE_INTEGRATE,
    ID_MOUSE_PEAKPICK,
    ID_MOUSE_DISTANCE,
    ID_MOUSE_SCALE,
    ID_MOUSE_NOISE, 
    ID_MOUSE_ROWCOL, 
    ID_MOUSE_SUM, 
    ID_MOUSE_ROWCOL_PLANE
} blal;

 /* g_menu_create_buttonbox2(win_id,0); */

    buttonbar_id = g_menu_create_buttonbar(win_id);
    g_menu_set_group(buttonbar_id, TRUE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_pos_xpm, NULL,
				  NULL,NULL, ID_MOUSE_POS, TRUE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_select_xpm, NULL,
				  NULL,NULL, ID_MOUSE_SELECT, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_region_xpm, NULL,
				  NULL,NULL, ID_MOUSE_BUFFER1, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_spline_xpm, NULL,
				  NULL,NULL, ID_MOUSE_BUFFER1, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_watwa_xpm, NULL,
				  NULL,NULL, ID_MOUSE_BUFFER1, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_baseline_xpm, NULL,
				  NULL,NULL, ID_MOUSE_BUFFER1, FALSE);
    g_menu_append_separator(buttonbar_id);
    g_menu_append_pixtoggle(buttonbar_id, mouse_zoomxy_xpm, NULL,
				  NULL,NULL, ID_MOUSE_ZOOMXY, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_zoomx_xpm, NULL,
				  NULL,NULL, ID_MOUSE_ZOOMX, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_zoomy_xpm, NULL,
				  NULL,NULL, ID_MOUSE_ZOOMY, FALSE);
    g_menu_append_separator(buttonbar_id);

    g_menu_set_group(buttonbar_id, 0);
    g_menu_set_group(buttonbar_id, TRUE);

    g_menu_append_pixtoggle(buttonbar_id, mouse_phase_p0_xpm, NULL,
				  NULL,NULL, ID_MOUSE_PHASE_P0, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_phase_p1_xpm, NULL,
				  NULL,NULL, ID_MOUSE_PHASE_P1, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_phase_i0_xpm, NULL,
				  NULL,NULL, ID_MOUSE_PHASE_I0, FALSE);
    g_menu_append_separator(buttonbar_id);
    g_menu_append_pixtoggle(buttonbar_id, mouse_calibrate_xpm, NULL,
				  NULL,NULL, ID_MOUSE_CALIBRATE, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_integrate_xpm, NULL,
				  NULL,NULL, ID_MOUSE_INTEGRATE, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_peakpick_xpm, NULL,
				  NULL,NULL, ID_MOUSE_PEAKPICK, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_distance_xpm, NULL,
				  NULL,NULL, ID_MOUSE_DISTANCE, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_scale_xpm, NULL,
				  NULL,NULL, ID_MOUSE_SCALE, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_noise_xpm, NULL,
				  NULL,NULL, ID_MOUSE_NOISE, FALSE);
    g_menu_append_separator(buttonbar_id);
    g_menu_append_pixtoggle(buttonbar_id, mouse_rowcol_xpm, NULL,
				  NULL,NULL, ID_MOUSE_ROWCOL, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_sum_xpm, NULL,
				  NULL,NULL, ID_MOUSE_SUM, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, mouse_rowcol_plane_xpm, NULL,
				  NULL,NULL, ID_MOUSE_ROWCOL_PLANE, FALSE);

    g_menu_enable_item(buttonbar_id, ID_MOUSE_PHASE_I0,
					   0);
    g_menu_enable_item(buttonbar_id, ID_MOUSE_POS,
					   0);
    g_menu_enable_item(buttonbar_id, ID_MOUSE_SELECT,
					   0);

    g_menu_add_tooltip(ID_MOUSE_NOISE, "Noise");
    g_menu_add_tooltip(ID_MOUSE_ROWCOL, "Row Col");
    g_menu_add_tooltip(ID_MOUSE_DISTANCE, "Distance");


/*
    g_menu_append_separator(buttonbar_id);
    g_menu_append_pixtoggle(buttonbar_id, try18_xpm, NULL,
				  NULL,NULL, ID_LINESTYLE, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, try19_xpm, NULL,
				  NULL,NULL, ID_LINESTYLE, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, try20_xpm, NULL,
				  NULL,NULL, ID_LINESTYLE, FALSE);
    g_menu_append_pixtoggle(buttonbar_id, try21_xpm, NULL,
				  NULL,NULL, ID_LINESTYLE, FALSE);
*/



    g_menu_append_pixbutton(buttonbar_id, zip_xpm, zip_gray_xpm, ID_LINESTYLE);
    g_menu_append_pixbutton(buttonbar_id, toolbox_xpm, toolbox_gray_xpm, ID_COLOR);
    g_menu_append_pixtoggle(buttonbar_id, disk_xpm, disk_gray_xpm,
				  disk_sel_xpm, disk_gray_xpm, ID_POLY, TRUE);
    g_menu_append_separator(buttonbar_id);
    g_menu_append_toggle(buttonbar_id, "Disable", ID_SETLABEL,FALSE);

{
 int  bar3_id, bar2_id, bar_id = g_menu_create_buttonbar2(win_id);
    g_menu_append_pixbutton(bar_id, divx_pix, divx_pix, PSND_XDIV2);
    g_menu_append_pixbutton(bar_id, mulx_pix, mulx_pix, PSND_XMUL2);
    g_menu_append_pixbutton(bar_id, divy_pix, divy_pix, PSND_YDIV2);
    g_menu_append_pixbutton(bar_id, muly_pix, muly_pix, PSND_YMUL2);

    g_menu_append_pixbutton(bar_id, full_xpm, NULL, PSND_RESET);
    g_menu_append_pixtoggle(bar_id, scale_xpm, NULL,NULL, NULL, PSND_SC, 0);
    g_menu_append_pixbutton(bar_id, mouse_xpm, NULL, PSND_CURSOR);
    g_menu_append_pixbutton(bar_id, zoomu_xpm, NULL, PSND_UNDO_ZOOM);
    g_menu_append_pixbutton(bar_id, zoomr_xpm, NULL, PSND_REDO_ZOOM);
    g_menu_append_separator(bar_id);
    g_menu_append_pixtoggle(bar_id, twod_xpm, NULL, NULL, NULL, PSND_CO, FALSE);
    g_menu_append_pixtoggle(bar_id, scale_xpm, NULL, NULL, NULL, PSND_SC_2D, 1);
    g_menu_append_pixbutton(bar_id, row_xpm, NULL, PSND_ROW_COLUMN);
    g_menu_append_pixbutton(bar_id, pclpm_xpm, NULL, PSND_LEVELS_BOTH);
    g_menu_append_pixtoggle(bar_id, phase2d_xpm, NULL, NULL, NULL, PSND_PHASE_2D, 0);

   bar2_id = g_menu_create_buttonbox2(win_id,2);
    g_menu_append_pixbutton(bar2_id, empty_xpm, NULL, 0);
    g_menu_append_pixbutton(bar2_id, wn_xpm, NULL, PSND_DO_WINDOW);
    g_menu_append_pixbutton(bar2_id, ft_xpm, NULL, PSND_FT);
    g_menu_append_pixbutton(bar2_id, pk_xpm, NULL, PSND_PK);
    g_menu_append_pixbutton(bar2_id, empty_xpm, NULL, 0);
    g_menu_append_pixbutton(bar2_id, bc_xpm, NULL, PSND_BASELINE);
    g_menu_append_pixbutton(bar2_id, pl_xpm, NULL, PSND_PL);
    g_menu_append_pixbutton(bar2_id, rn_xpm, NULL, PSND_RN);
    g_menu_append_pixbutton(bar2_id, dr_xpm, NULL, PSND_DR);
    g_menu_append_separator(bar2_id);

    g_menu_append_pixbutton(bar2_id, cp_xpm, NULL, PSND_CP);
    g_menu_append_pixbutton(bar2_id, up_xpm, NULL, PSND_UP);
    g_menu_append_pixbutton(bar2_id, crop_xpm, NULL, PSND_CROP);
    g_menu_append_pixbutton(bar2_id, planeup_xpm, NULL, PSND_PLANE_UP);
    g_menu_append_pixbutton(bar2_id, planeu_xpm, NULL, PSND_PLANE_UNDO);


    bar3_id = g_menu_create_buttonbox2(win_id,3);
    g_menu_append_pixbutton(bar3_id, next_xpm, next_xpm, PSND_DOKEY_PLUS);
    g_menu_append_pixbutton(bar3_id, prev_xpm, prev_xpm, PSND_DOKEY_MINUS);
    g_menu_append_pixbutton(bar3_id, undo_xpm, NULL, PSND_UNDO);
    g_menu_append_pixbutton(bar3_id, redo_xpm, NULL, PSND_REDO);
    g_menu_append_pixbutton(bar3_id, au_xpm, NULL, PSND_AU);
    g_menu_append_pixbutton(bar3_id, script_pix, script_pix, PSND_SCRIPT);
    g_menu_append_pixbutton(bar3_id, bookm_xpm, NULL, PSND_POPUP_BOOKMARK);
    g_menu_append_pixtoggle(bar3_id, plplus_xpm, NULL,NULL,NULL, PSND_APPEND_PLOT, 0);

    g_menu_append_pixtoggle(bar3_id, grid_pix, grid_gray_pix, 
                                         grid_pix, grid_gray_pix, 
                                         PSND_SHOW_GRID, 0);
    g_menu_append_separator(bar3_id);

    g_menu_append_pixbutton(bar3_id, pl2d_xpm, NULL, PSND_CP2);
    g_menu_append_pixbutton(bar3_id, down_xpm, NULL, PSND_DOWN);
    g_menu_append_pixbutton(bar3_id, uncrop_xpm, NULL, PSND_UNCROP);
    g_menu_append_pixbutton(bar3_id, planedn_xpm, NULL, PSND_PLANE_DOWN);
    g_menu_append_pixbutton(bar3_id, planer_xpm, NULL, PSND_PLANE_REDO);
 g_menu_create_buttonbox2(win_id,-1);

/*
  // g_menu_append_button(buttonbar_id2, "2de MenuBar", ID_MENUBAR2);
   // g_menu_append_button(buttonbar_id2, "test", ID_TEST);
*/
}
}

/*
psnd_setup_buttonbar( win_id);
*/

    g_set_clipping(current_clip);
    g_set_cursortype(win_id, G_CURSOR_CROSSHAIR);
    g_add_eventhandler(G_BUTTON2PRESS, handler, (G_CALLDATA) g_bell);

    alternate_cursor = FALSE;
    ok = FALSE;
    while (!ok) {
	result = g_get_event(&ui);
	if (ui.command != NULL)
	    fprintf(stderr,"%s\n", ui.command);

	switch (result) {
            case G_WINDOWQUIT:
g_bell();
                if (ui.win_id == win_id)
                    ok = TRUE;
g_select_window(win_id);
                break;

            case G_WINDOWDESTROY:
                if (ui.win_id == win_id)
                    ok = TRUE;
g_select_window(win_id);
                break;

	    case G_KEYPRESS:
		if (ui.keycode == 'q')
		    ok = TRUE;
            	break;

	    case G_BUTTON1PRESS:
		if (alternate_cursor) {
		    static int count;

		    switch (count%3) {
			case 0:
			g_set_rubbercursor(win_id, ui.vp_id, ui.x, ui.y,
					       G_RUBBER_NONE);
			    break;
			case 1:
			    g_set_rubbercursor(win_id, ui.vp_id, ui.x, ui.y,
					       G_RUBBER_BOX);
			    break;
			case 2:
			    g_set_rubbercursor(win_id, ui.vp_id, ui.x, ui.y,
					       G_RUBBER_PANNER);
			    break;
		    }
		    count++;
		}
		ui.keycode = '0';
		printf("win=%d vp=%d x=%.1f y=%.1f char=%c\n",
		ui.win_id,ui.vp_id,ui.x,ui.y,ui.keycode);
		break;

	    case G_COMMAND:
		switch (ui.keycode) {
                 case ID_WRITEFILE:
                    {
                        int num_items, j;
                        int do_compression = TRUE;
                        char *list[100];
                        num_items = g_count_objects_byname();
                        g_list_objects_byname(list);
                        for (j=0;j<num_items;j++)
                            printf("%s\n", list[j]);
	                result = g_popup_listbox(win_id,"Objects in File", "Select Object", 
                                                     num_items, list, 1, 8);
                        if (result) {
                            int fp;
                            char *s;
			    s = g_popup_saveas(win_id, "Object File", "gna.gp");
                            if (s) {
                                printf("Writing Object: %s\n",list[result-1]);
                                if ((fp = g_open_object_file(s, TRUE)) == G_ERROR) {
                                    g_create_object_file(s,"GENTEST", do_compression);
                                    fp = g_open_object_file(s, TRUE);
                                }
                                g_write_fileobject(fp, list[result-1], TRUE, TRUE);
                                g_close_object_file(fp);
                            }
                       }
                    }
                    break;

		 case ID_READFILE:
		    {
			char *s, *list[100], label[33];
                        int fp, num_items = 0, result, obj_id;
			s = g_popup_getfilename(win_id, "Get object file", "*.gp");
			if (s) {
                            if ((fp = g_open_object_file(s, FALSE)) == G_ERROR)
                                break;
                            NewPage();
                            if (g_first_fileobject(fp, label)!= G_ERROR) 
                            do {
                                printf("Object name is: %s\n",label);
                                list[num_items] = (char*) malloc(33);
                                strcpy(list[num_items], label);
                                num_items++;          
                                if (num_items == 100)
                                    break;
                            } 
                            while (g_next_fileobject(fp, label)!= G_ERROR);                 
                            if (num_items == 0) {
                                g_close_object_file(fp);
                                break;
                            }
		            result = g_popup_listbox(win_id,"Objects in File", "Select Object", 
                                                     num_items, list, 1, 8);
                            if (result) {
                                obj_id = g_unique_object();
                                printf("Calling Object: %s id = %d\n",list[result-1], obj_id);
                                g_read_fileobject(fp, obj_id, list[result-1], TRUE, TRUE);
                                g_call_object(obj_id);
                            }
                            while (num_items) {
                                free(list[num_items-1]);
                                num_items--;
                            }
                            g_close_object_file(fp);
                        }
		    }
		    break;

		case ID_LINESTYLE:
		    NewPage();
		    TestLinestyle();
		    break;

		case ID_CLIP:
		    NewPage();
		    TestClip();
		    break;

		case ID_COLOR:
		    NewPage();
		    TestColor();
		    break;

		case ID_MOUSE:
		    NewPage();
		    TestMouse();
		    break;

		case ID_LABEL:
		    NewPage();
		    TestLabel();
		    break;

		case ID_POLY:
		    NewPage();
		    TestPoly();
		    break;

		case ID_VIEWPORT: {
		    int win_id2;
		    /* --- restore old gc parameters --- */
		    g_pop_gc();
		    g_push_gc();
		    win_id2 = g_open_window(device,20,300,200,150,
					    "TEST 2", G_WIN_BUFFER|G_WIN_PRIMARY_SHELL);

		    TestViewport(win_id2);
		    g_close_window(win_id2);
		    g_select_window(win_id);
		    }
		    break;

		case ID_BUTTONBAR:
		    g_pop_gc();
		    g_push_gc();
		    TestButtonBar();
		    g_select_window(win_id);
		    break;

		case ID_SCREEN:
		    TestCopyWindow(win_id, G_SCREEN);
		    break;

		case ID_HPGL:
		    g_pop_gc();
		    g_push_gc();
		    TestCopyWindow(win_id, G_HPGL);
		    break;

		case ID_POSTSCRIPT:
		    g_pop_gc();
		    g_push_gc();
		    TestCopyWindow(win_id, G_POSTSCRIPT);
		    break;

		case ID_WMF:
		    g_pop_gc();
		    g_push_gc();
		    TestCopyWindow(win_id, G_WMF);
		    break;

		case ID_PRINTER:
		    g_pop_gc();
		    g_push_gc();
		    TestCopyWindow(win_id, G_PRINTER);
		    break;

		case ID_CONSOLE:
		    TestConsole(win_id);
		    break;

		case ID_ZOOM:
		    TestZoom();
		    g_select_window(win_id);
		    break;

		case ID_BEEP:
		    g_bell();
		    break;

		case ID_MESSAGE:{
		    static int i;
		    char s[40];
		    i++;
		    sprintf(s,"          Message %d          ", i);
		    g_popup_messagebox(win_id, "Title", s, FALSE);
		    }
		    break;

		 case ID_GETFILE:
		    {
			char *s;
			s = g_popup_getfilename(win_id, "Get some file", "*");
			if (s)
			    printf("Filename: %s\n", s);
		    }
		    break;

		 case ID_SAVEAS:
		    {
			char *s;
			s = g_popup_saveas(win_id, "Save some file", "somefile.plt");
			if (s)
			    printf("Filename: %s\n", s);
		    }
		    break;

		 case ID_YESNO:
		    {
			int yesno;
			yesno = g_popup_messagebox2(win_id, "Any Question",
													"Push YES or NO","YES","NO");
			if (yesno == G_POPUP_BUTTON1)
			    printf("YES\n");
			else
			    printf("NO\n");
		    }
		    break;

		 case ID_YESNOCANCEL:
		    {
			int yesno;
			yesno = g_popup_messagebox3(win_id, "Any Question",
			       "Push YES, NO or CANCEL","YES","NO","CANCEL");
			if (yesno == G_POPUP_BUTTON1)
			    printf("YES\n");
			else if (yesno == G_POPUP_BUTTON2)
			    printf("NO\n");
			else
			    printf("CANCEL\n");
		    }
		    break;

		 case ID_PROMPT:
		    {
			char *s;
			s = g_popup_promptdialog(win_id,"Any Title",
                                "Put something on this line", "Something");
			if (s)
			    printf("%s\n",s);
		    }
		    break;

		case ID_LISTBOX: {
		    char *list[] = {
			"This is Item 1",
			"And this is Item 2",
			"Item 3",
			"And much more",
			"Another one",
			"This is item 6",
			"And This is item 7",
			"Item 8",
			"Item 9",
			"Item 10",
			"Item 11",
			"And finaly the last item on the list"
		    };
		    int result;
		    int num_items = 12, select_pos =12, items_visible = 8;
		    result = g_popup_listbox(win_id,"Any Title", "LIST", num_items,list,
					 select_pos,items_visible);
		    if (result)
			printf("%s\n",list[result-1]);
		    }
		    break;

		case ID_RADIOBOX: {
		    char *list[] = {
			"This is Item 1",
			"And this is Item 2",
			"Item 3",
			"And much more",
			"Another one",
			"This is item 6",
			"And This is item 7",
			"Item 8",
			"Item 9",
			"Item 10",
			"Item 11",
			"And finaly the last item on the list"
		    };
		    int result;
		    int num_items = 12, select_pos =6;
		    result = g_popup_radiobox(win_id,"Any Title", "SELECT ONE ITEM",
					      num_items, list, select_pos);
		    if (result)
			printf("%s\n",list[result-1]);
		    }
		    break;

		case ID_CHECKBOX: {
		    char *list[] = {
			"This is Item 1",
			"And this is Item 2",
			"Item 3",
			"And much more",
			"Another one",
			"This is item 6",
			"And This is item 7"
		    };
		    int select[] = {0,1,0,0,1,0,0};
		    int i, result;
		    int num_items = 7;
		    result = g_popup_checkbox(win_id,"Any Title" , "SUB-TITLE",
					       num_items, list, select);
		    if (result)
			for (i=0;i<num_items;i++)
			    if (select[i])
				printf("SELECT: %s\n",list[i]);
		    }
		    break;

		case ID_CONTAINER:
		    TestContainer(win_id);
		    break;

		case ID_EXIT:
		    ok = TRUE;
		    break;

		case ID_TOGGLE_CURSOR:
		    if (g_menu_get_toggle(menub_id2, ID_TOGGLE_CURSOR)) {
			g_set_cursortype(win_id, G_CURSOR_CROSSHAIR);
			g_menu_enable_item(menub_id2, ID_TOGGLE_ALTERNATE,
					   TRUE);
		    }
		    else {
			g_set_cursortype(win_id, G_CURSOR_UPDOWN);
			g_menu_enable_item(menub_id2, ID_TOGGLE_ALTERNATE,
					   FALSE);
		    }
		    {
		    int id;
		    id = g_menu_is_enabled(menub_id2, ID_TOGGLE_ALTERNATE);
		    printf("Alternate Button Enabled= %d\n", id);
		    }
		    break;

		case ID_TOGGLE_CLIPPING:
		    current_clip = g_menu_get_toggle(menub_id2,
						ID_TOGGLE_CLIPPING);
		    break;

		case ID_TOGGLE_ALTERNATE:
		    alternate_cursor = g_menu_get_toggle(menub_id2,
				    ID_TOGGLE_ALTERNATE);
		    break;

		case ID_TOGGLE_SCREENBUF:
		    g_set_windowbuffer(win_id,
				       g_menu_get_toggle(menub_id2,
							 ID_TOGGLE_SCREENBUF));
		    break;

		case ID_TOGGLE_TEXTROTATION: {
		    int textrotation = g_menu_get_toggle(menub_id2,
					    ID_TOGGLE_TEXTROTATION);
		    g_set_motif_realtextrotation(textrotation);
		    }
		    break;

		case ID_POPMENU1:
		    g_menu_select_popup(menupopup_id);
		    break;

		case ID_POPMENU2:
		    g_menu_select_popup(menupopup2_id);
		    break;

		case ID_POPMENU3:
		    g_menu_select_popup(menupopup3_id);
		    break;

		case ID_POPMENU4:
		    g_menu_hide_popup(win_id);
		    break;

		case ID_SETLABEL: {
		    char *s[] = {
			"Disable",
			"Enable"
		    };
		    static int i;
			(i==1) ? (i=0) : (i=1);
			g_menu_set_label(buttonbar_id, ID_SETLABEL,s[i]);
			g_menu_enable_item(buttonbar_id, ID_LINESTYLE, !i);
			g_menu_enable_item(buttonbar_id, ID_COLOR, !i);
			g_menu_enable_item(buttonbar_id, ID_POLY, !i);
		    }
		    break;

		case ID_MENUBAR2:
		    {
    break;
		    if (show == 0) {
/*
			//menubar2_id = g_menu_create_menubar2(win_id);
			//menub2_id1 = g_menu_append_submenu(menubar2_id, "Some Pulldowns");
*/
			menub2_id1 = g_menu_create_buttonbar2(win_id);
			g_menu_append_button(menub2_id1, "&Linestyle", ID_LINESTYLE);
			g_menu_append_button(menub2_id1, "&Clip", ID_CLIP);
			g_menu_append_button(menub2_id1, "C&olor", ID_COLOR);
			g_menu_append_separator(menub2_id1);
			g_menu_append_button(menub2_id1, "E&xit", ID_EXIT);
			g_menu_set_label(buttonbar_id, ID_MENUBAR2, "Clear MenuBar 2");
			show = 1;
		    }
		    else if (show == 1) {
gna(win_id, 1);
/*
			g_menu_destroy_menubar2_children(win_id);
*/
			g_menu_destroy_buttonbar2_children(win_id);
			g_menu_set_label(buttonbar_id, ID_MENUBAR2, "Kill MenuBar 2");
			show = 2;
		    }
		    else {
gna(win_id, 2);
/*
			g_menu_destroy_menubar2(win_id);
*/
			g_menu_destroy_buttonbar2(win_id);
			g_menu_set_label(buttonbar_id, ID_MENUBAR2, "Make MenuBar 2");
			show = 0;
		    }
		    }
		    break;

		default:
		    printf("Menu item = %d\n", ui.keycode);
		    break;
		}
		break;

	}
    }
}



#ifdef _WIN32
void genmain(int argc, char **argv)
#else
void main(int argc, char **argv)
#endif
{
g_get_xterm();
#ifdef TEST_PALETTE
	G_PALETTEENTRY pal;
	pal.r = 0;
	pal.g = 0;
	pal.b = 100;
#endif
	 buffer = (char*) malloc(1000);
	 g_init(argc, argv);
#ifdef TEST_PALETTE
	g_set_paletteentry(1, pal);
	g_set_paletteentry(2, pal);
	g_set_paletteentry(3, pal);
	g_set_paletteentry(4, pal);
	g_set_paletteentry(5, pal);
#endif
{
#include "buttons_xpm.h"
g_set_icon_pixmap(NULL, NULL, psnd_icon32_xpm, 
                     psnd_icon16_xpm);
}
	g_set_font(G_FONT_HELVETICA, G_ABSOLUTE_FONTSCALING);
	 g_push_gc();
 
#ifdef _WIN32

    // id_cons = g_open_window(G_SCREEN,10,10,400,200,
	//		"CONSOLE", G_WIN_MENUBAR |G_WIN_CONSOLE |
     //                    G_WIN_COMMANDLINE_RETURN |G_WIN_PRIMARY_SHELL);
    g_open_console(10,10,600,200,"OUTPUT");
#endif

	TestMenus();
	g_end();
	free(buffer);
/*	exit(0);*/
}


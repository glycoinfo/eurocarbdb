#-------------------------------------------------------------------------------
#
#   HTML::JsTree.pm
#   copyright 2005, Matt Harrison. all rights reserved.
#
#-------------------------------------------------------------------------------

=head1  HTML::JsTree

author:     daniel b, matt h

***** TODO *****

this is a f***** awful implementation, though it works
(for one particular use only, but that's why it's so bad).

Things which need to be done:
    a) needs to be able to be used as a standalone widget --
    currently, it prints its own html, head, and body tags
    as well as a f***** blue line across the page before
    the actual tree.
    b) images used, cookie names, and stylesheet all are
    non-cofigurable -- there's no need for this to be so.



Examples of use:

A simple example:

    use HTML::JsTree;
    use HTML::JsNode;
    use CGI;

    #   create the tree
    #
    my $tree = new HTML::JsTree ('A sexy tree widget');

    #   add some nodes to the tree
    #
    $tree->add( new HTML::JsNode ( value => 'first root node' ), 'ROOT' );
    $tree->add( new HTML::JsNode ( value => 'second root node' ), 'ROOT' );
    $tree->add( new HTML::JsNode ( value => 'third root node' ), 'ROOT' );

    #   add a branching node, and some child nodes to it
    #
    my $parent_node = new HTML::JsNode ( value => 'first parent node' );
    $tree->add( $parent_node, 'ROOT' );
    $tree->add( new HTML::JsNode ( value => 'first child node' ), $parent_node );
    $tree->add( new HTML::JsNode ( value => 'second child node' ), $parent_node );

    #   print to the browser
    #
    my $cgi = new CGI ();
    print $cgi->header('text/html'),
          $tree->as_string();

    exit;


A more elaborate example of functionality:

    use HTML::JsTree;
    use HTML::JsNode;
    use CGI;

    #   create the tree
    #
    my $tree = new HTML::JsTree ('A sexy tree widget');

    #   add some nodes to the tree
    #

    for ( my $i = 0; $i < 10; $i++ ) {
        $tree->add(
            new HTML::JsNode (
                value   =>  "tree item $i",
                id      =>  "node $i"
            ),
            'ROOT'
        );
    }

    my $cgi = new CGI ();
    print $cgi->header(
        '-type'          => 'text/html',
        '-cache-control' => 'no-cache',
        '-pragma'        => 'no-cache'
    ), $tree->as_string();

    exit;

=cut
package HTML::JsTree;

use strict;
use HTML::JsNode;
use CGI;

use constant CVS_REVISION => q!$Id:$!;



#   GLOBAL VARS   #----------------------------------------

=head1  Public class variables


=head2  $DEBUG

Integer - any value > 0 will write debugging info to STDERR.
The current value '0', is completely quiet.

=cut
our $DEBUG = 0;

our %TREE_DEFAULTS = (
    'suppress-header-hack'  =>  0,
    'print-expanded'        =>  0,
    'show_header'           =>  1,
);

our $IMAGE_ROOT = "jstree";

our $COOKIE_NAME = "jstree";

our $Stylesheet = "jstree/navtree.css";

#   METHODS   #--------------------------------------------

=head1  Constructor

=head2 new

    $tree = new HTML::JsTree ( $title )

Generic constructor.

=cut
sub new
{
    my $class = shift;
    my $this  = {};

    my $title = shift || '';

    %{$this} = (
        %$this,
        'title'                 =>  $title,
        'nodes'                 =>  {},
        'tree'                  =>  {},
        %TREE_DEFAULTS
    );

    return bless( $this, $class );
}



#   METHODS   #--------------------------------------------

=head1  Methods


=head2  add

Usage:

    $this->add( $node );
    $this->add( $tree, $parent );

Arguments:

    $node               ->  A L<HTML::JsNode> object or an object of a type which
                            inherits from L<HTML::JsNode> (eg L<HTML::JsTreeNode>)
    $tree               ->  An existing L<HTML::JsTree>.
    $parent             ->  The node to which to attach $node, or 'ROOT' if attaching
                            to the root node.  Defaults to 'ROOT' if no $parent is
                            supplied

Add a node to the tree.

=cut
sub add
{
    my $this   = shift;
    my $node   = shift || die;
    my $parent = shift || 'ROOT';

    $node = HTML::JsNode->new('value' => $node) unless ref($node);

    if ( $node->isa('HTML::JsNode') ) 
    {
        my $id = $node->get_id();
        my $parent_id = ($parent eq 'ROOT') ? 'ROOT' : $parent->get_id();
        $DEBUG && warn "adding node: '$id' to '$parent_id'";

        $this->{'nodes'}->{'ROOT'} ||= $node if ( $parent eq 'ROOT' );
        $this->{'nodes'}->{$id} = $node;
        push @{$this->{'tree'}->{$parent_id}}, $id;

        return $node;
    }
    else {
        die "Unknown node";
    }
}



=head2  as_string

Usage:

    $html = $this->as_string()

Returns:

    $html         ->  html page source

Render the current tree to html source.

=cut
sub as_string
{
    my $this = shift;
    my $title = $this->{'title'};

    my %expanded    = ();
    my $cgi         = new CGI;
    my $cookie_name = $this->{'cookie_name'} || $COOKIE_NAME;

    for ( $cgi->cookie( $cookie_name ) ) {
        $expanded{$_}++;
    }

    my $html = $this->divTreeHeader( $cookie_name )
            . qq|
    <div id="toplevel-0" ondblclick="treeToggle(this);" class="nav-tree-item">
            <img id="toplevel-0-icon" src="$IMAGE_ROOT/openfoldericon.png">
            $title
    </div>
    <div id="toplevel-0-cont" class="nav-tree-container" style="display: block;">|;

    if ( $DEBUG ) {
        eval "require Data::Dumper";
        $@ && die "Couldn't dynamically load Data::Dumper: $@";
        warn "data structure:\n" . Data::Dumper::Dumper( $this->{'tree'} ) . "\n\n";
    }

    if ( $this->{'nodes'}->{'ROOT'} ) {

        #   recurse through tree, adding page to variable $html,
        #   passed by reference.
        #
        $this->_recurse( \$html, \%expanded, 'ROOT', 0, [1], 0 );
    }
    $html .= DIVTREE_FOOTER();

    $DEBUG > 1 && warn "html\n$html\n\n";

    return $html;
}


#   PRIVATE METHODS   #------------------------------------

=head1  Private methods

=head2  _recurse

Usage:

    $this->_recurse( \$html, \$expanded, $id, $depth, $depthArray, $endchain )

Recursive routine creating div tags for the present tree.

    $html: Reference to the string which represents the page so far
    $expanded: Hashref of nodes which should be rendered already opened
    $id: The node currently being added to the tree

Magic numbers: $endchain (0,1,2) and $depthArray entries (0,1,2)
$endchain:
        0 - Just started
        1 - We are descending a single child chain
        2 - We are not descending a single child chain
$depthArray:
        0 - Not using this depth
        1 - Regular entry at this depth (ie needs a 'T')
        2 - Last entry at this depth (ie needs an 'L')

=cut
sub _recurse
{
    my($this, $html, $expanded, $id, $depth, $depthArray, $endchain) = @_;
    return unless defined $id;

    my $node = $this->{'nodes'}->{$id} || die "No node by id '$id'";
    if ( $node->is_open() ) {
        $expanded->{$id}++;
    }
    my $child_ids = $this->{'tree'}->{$id};
    my $node_value = $node->get_value();
    $id =~ tr| |-|;

    if(ref($child_ids) && $this->_has_children(scalar(@$child_ids), $endchain, $depth)) {
        my $node_icon = $node->get_icon()
            || $IMAGE_ROOT.($$expanded{$id}? "/open": "/")."foldericon.png";
        if($depth) {
            ${$html} .= qq|<div id="$id" class="nav-tree-item">|;
            for(my $count = 1; $count < $depth; $count++) {
                    ${$html} .= ($$depthArray[$count]==2?
                    qq|<img src="$IMAGE_ROOT/blank.png">|:
                    qq|<img src="$IMAGE_ROOT/I.png">|);
            }
            ${$html} .= qq|<img id="$id-plus" src="$IMAGE_ROOT/|.
            ($$depthArray[$depth]==2? 'L': 'T').
            ($$expanded{$id}? 'minus' : 'plus').
            q|.png" onclick="treeToggle(this,event);">|.
            qq|<img id="$id-icon" src="$node_icon">$node_value</div>\n|.
            qq|<div id="$id-cont" class="nav-tree-container" style="display: |.
            ($$expanded{$id}? 'block' : 'none'). q|;">|;
        }
        $$depthArray[$depth+1] = 1;
        for(my $count = 0; $count < @$child_ids; $count++) {
            $$depthArray[$depth+1] = 2 if $count==$#$child_ids;
            #set the depthArray entry to 2 on the last entry
            $this->_recurse($html, $expanded, $$child_ids[$count], $depth+1, $depthArray, $depth&&@$child_ids==1? 1:2);
        }
        ${$html} .= qq|</div>| if($depth);
    }
    else {
        my $node_icon = $node->get_icon()||"$IMAGE_ROOT/new.png";
        ${$html} .= qq|<div id="$id" class="nav-tree-item">|;
        for(my $count = 1; $count < $depth; $count++) {
            ${$html} .= ($$depthArray[$count]==2?
            qq|<img src="$IMAGE_ROOT/blank.png">|:
            qq|<img src="$IMAGE_ROOT/I.png">|);
        }
        ${$html} .= qq|<img id="$id-plus" src="$IMAGE_ROOT/|.
        (ref($child_ids)||$$depthArray[$depth]!=2? 'T': 'L').
        qq|.png">|.
        qq|<img id="$id-icon" src="$node_icon">$node_value|.
        qq|</div>|;
        $this->_recurse($html, $expanded, $$child_ids[0], $depth, $depthArray, 1) if(ref($child_ids)&&@$child_ids);
    }
}


=head2 _has_children

    $this->_has_children($num_child_ids, $endchain, $depth)

    $num_child_ids: Number of children
    $endchain: See description in L<recurse>
    $depth: Depth of node from root

    Return true if we are going to have to treat the children of this node

=cut
sub _has_children {
    shift;
    return ($_[0] > 1 || ($_[1]==2 && $_[0]==1 && $_[2] > 1) || $_[1]==0? 1: 0);
}

=head2 show_header

Show the header
=cut
sub show_header
{
    my $this = shift;
    my $val = shift;

    $this->{'show_header'} = $val if defined($val);

    return $this->{'show_header'};
}

=head1 divTreeHeader

    $header = $this->divTreeHeader($cookie_name)

Arguments:

    $cookie_name: Name of the cookie to get/set tree information

Returns:

$header: A string to use as the header for the DivTree page.

=cut
sub divTreeHeader
{
    my $this = shift;
    my $cookie_name = shift || die;

    my $html;

    $html .= qq|

<!-- begin tree -->
<link type="text/css" rel="stylesheet" href="$Stylesheet"  />

<script Language="Javascript1.2">
<!--

var image_root = '|

. $IMAGE_ROOT

. qq|/';

function setCookieArray(array)
{
    var cookies = '';
    if(array.length) {
        cookies = array[0];
        for(i = 1; i < array.length; i++) cookies += '&' + array[i];
    }
    var twoWeeks = new Date((new Date()).getTime() + 14 * 24 * 3600000);
    document.cookie = '$cookie_name=' + cookies + "; expires=" + twoWeeks.toGMTString();
}

function getCookieArray()
{
    if(!document.cookie.length) return new Array();
    var cookies = document.cookie;
    var start = cookies.indexOf(' $cookie_name=');
    if(start==-1) {
        cookies = document.cookie;
        start = cookies.indexOf('$cookie_name=');
        if(start==-1) return new Array();
    }
    start = cookies.indexOf('=', start) + 1;
    var end = cookies.indexOf(";", start);
    if(end==-1) end = cookies.length;
    return cookies.substring(start,end).split('&');
}

| . q|

function addVal(key)
{
    var keys = getCookieArray();
    for(var i = 0; i < keys.length; i++) if(keys[i]==key) return;
    keys[keys.length] = key;
    setCookieArray(keys);
}

function removeVal(key)
{
    var keys = getCookieArray();
    for ( var i = 0; i < keys.length; i++) if(keys[i]==key) {
        if(i != keys.length-1) {
            keys[i] = keys[keys.length-1];
            i = keys.length;
        }
        keys.length--;
    }
    setCookieArray(keys);
}

function testVal(key)
{
    var keys = getCookieArray();
    for(var i = 0; i < keys.length; i++) if(keys[i]==key) return false;
    return true;
}

function recurse(name,shiftPress,expand)
{
    var block = document.getElementById(name + '-cont');
    if(block) {
        block.style.display = (expand? 'block': 'none');
        var nodeList = block.childNodes;
        if(shiftPress) for(var count = 0; count < nodeList.length; count++) {
            curChildID = nodeList.item(count).id;
            if(curChildID) if(!curChildID.match(/-cont$/)) recurse(curChildID,shiftPress,expand);
        }
    }
    if(expand) {
        if(name.match(/^toplevel/)) document.getElementById(name + '-icon').src = image_root + 'openfoldericon.png';
        else {
            document.getElementById(name + '-plus').src = document.getElementById(name + '-plus').src.replace('plus', 'minus');
            if(name.match(/^folder/)) document.getElementById(name + '-icon').src = image_root + 'openfoldericon.png';
        }
        addVal(name);
    }
    else
    {
        if ( name.match(/^toplevel/) )
    document.getElementById(name + '-icon').src = image_root + 'foldericon.png';
        else {
            document.getElementById(name + '-plus').src = document.getElementById(name + '-plus').src.replace('minus', 'plus');
            if(name.match(/^folder/)) document.getElementById(name + '-icon').src = image_root + 'foldericon.png';
        }
        removeVal(name);
    }
}

function treeToggle(clicked,event) {
    var name = clicked.id.replace('-plus','');
    recurse(name,event.shiftKey,testVal(name));
}
-->
</script>
|;

    return $html;
}

use constant DIVTREE_FOOTER => q\
</div>
\;


1;

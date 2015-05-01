#-------------------------------------------------------------------------------
#
#   HTML::ExtendedJsTree.pm
#   copyright 2005, Matt Harrison. all rights reserved.
#
#-------------------------------------------------------------------------------

=head1  HTML::ExtendedJsTree

Similar to L<Labbook::GUI::Tree> but with one difference: Children are chosen
differently for trees where the last child is below vertically, as opposed
to horizontally.

=cut
package HTML::ExtendedJsTree;


use strict;
use constant CVS_REVISION => q!$Id:$!;

use HTML::JsTree;
our @ISA = qw/ HTML::JsTree /;

=head2 _has_children

    $this->_has_children($num_child_ids, $endchain, $depth)

    $num_child_ids: Number of children
    $endchain: See description in L<recurse>
    $depth: Depth of node from root

    Return true if we are going to have to treat the children of this node

=cut
sub _has_children($$$$) {
    shift;
    return  (($_[0] > 0 || $_[1] != 1)
            ?   1
            :   0 )
 ;
}


1;

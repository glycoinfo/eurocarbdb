#-------------------------------------------------------------------------------
#
#   HTML/JsNode.pm
#
#-------------------------------------------------------------------------------

=head1  HTML::JsNode

author:     matt harrison

This class defines the minimum interface (and implementation) for
objects which may be included into HTML::JsTree's.

=cut
package HTML::JsNode;

use strict;
use constant CVS_REVISION => q!$Id:$!;


#   CLASS DATA   #-----------------------------------------

our %OBJECT_VARS = (
    'value' =>  undef,
    'id'    =>  undef,
    'icon'  =>  undef,
    'href'  =>  undef,
);



#   CLASS METHODS   #--------------------------------------

=head1  Constructors



=head2 new

Usage:

    $node = new HTML::JsNode (
        'value' =>  $value,
        'id'    =>  $node_id,
        'icon'  =>  $icon_url,
        'href'  =>  $href_link,
    )

Generic constructor. Only the 'id' argument cannot be changed
after construction. Other attributes can be set here or via
the various accessors methods provided.

=cut
sub new
{
    my $class = shift;
    my $this  = {};
	
    %{$this} = (
        %$this,
        %OBJECT_VARS,
        @_
    );

	#	if they don't give us an ID, we generate one.
	unless ( $this->{id} )
	{
		my $id = "$this"; 	
		$id =~ s/^HASH\(0//;
		$id =~ s/\)$//;
		
		$this->{id} = $id;
	}
	

    return bless( $this, $class );
}





#   OBJECT METHODS   #-------------------------------------


=head1  Object methods

=cut


#   ACCESSORS   #------------------------------------------


=head2  get_href

    $href = $this->get_href()

=cut
sub get_href {
    return $_[0]->{'href'};
}


=head2  get_icon

    $icon_url = $this->get_icon()

=cut
sub get_icon {
    return $_[0]->{'icon'};
}


=head2  get_id

    $id = $this->get_id()

Node id is immutable.

=cut
sub get_id {
    return $_[0]->{'id'};
}


=head2  get_value

    $value = $this->get_value()

=cut
sub get_value {
    return $_[0]->{'value'};
}


=head2  is_open

    $boolean = $this->is_open();
    $this->is_open( $boolean );

Dictates/Reveals whether this node will start opened in
the rendered tree.

=cut
sub is_open 
{
    return @_ == 2 
        ?   ($_[0]->{'is_open'} = $_[1])
        :   $_[0]->{'is_open'};
}



#   MUTATORS   #-------------------------------------------


=head2  set_href

    $this->set_href( $href )

=cut
sub set_href {
    return $_[0]->{'href'} = $_[1];
}


=head2  set_icon

    $this->set_icon( $icon_url )

=cut
sub set_icon {
    return $_[0]->{'icon'} = $_[1];
}


=head2  set_value

    $this->set_value( $value )

=cut
sub set_value {
    $_[0]->{'value'} = $_[1];
}




#   OTHER METHODS   #--------------------------------------

=head2  as_string

    $string = $this->as_string()

Prints the value of this node.

=cut
sub as_string {
    return $_[0]->get_value();
}


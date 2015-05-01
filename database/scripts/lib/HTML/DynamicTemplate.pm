#-------------------------------------------------------------------------------
#
#   HTML/DynamicTemplate.pm
#   copyright 2002, Proteome Systems Ltd. all rights reserved.
#
#-------------------------------------------------------------------------------

package HTML::DynamicTemplate;

use 5.005;
use strict;
#use overload '""' => "render";
use Carp;

$HTML::DynamicTemplate::VERSION = "1.1";

use constant CVS_REVISION => q!$Id: DynamicTemplate.pm,v 1.15 2003/01/22 02:40:07 matt Exp $!;

#   true values make objects die on serious errors.
#
#   false values write warnings to STDERR and
#   print an error within the html page.
#
#   your choice.
#
my $STRICT = '';

=head1 NAME

HTML::DynamicTemplate - a HTML template class.

=head1 SYNOPSIS

    use HTML::DynamicTemplate;

    my $template = new HTML::DynamicTemplate ('path/to/template');
    $template->set_recursion_limit($integer);

    #   template variables can be set from perl or
    #   within the template itself
    #
    $template->set( BACKGROUND_PIC  => '../some/pic.gif' );
    $template->set(
        NAME_1   => $value_1,
        NAME_2   => $value_2,
        NAME_3   => \@some_array,
        BODY     => 'background=$BACKGROUND_PIC link=blue',
        CALLBACK => sub{ return "some html or template \$VARS" }
    );
    $template->set( \%other_vars );
    $value = $template->get('VAR');

    #   clear all variables or just clear some
    #
    $template->clear();
    $template->clear( \@variables );

    #   obtain & print the substituted template...
    #
    print $template->render();
    print $template->render(@variables);

    #   Create specialised DataTemplate objects by subclassing
    #   and overriding the special callback_* routines.
    #   who said perl doesn't have inner classes??? ;-)
    #
    {
        package DataTemplate;
        use base 'HTML::DynamicTemplate';

        sub callback_ARRAY
        {
            my( $this, $name, $array_ref ) = @_;
            #   some cool stuff here...
            return 'some html or template $VARS'
                if $name eq 'DATA';
        }
    }
    my $data_tmpl = new DataTemplate ('/templates/data.tmpl');
    $data_tmpl->set('DATA', \@my_data );
    print $data_tmpl->render();



    path/to/template
    ----------------
    <html>
    <body $BODY>
    $DEFINE(HEADING, "My Page")
    $INCLUDE(/some/common/header)

    $DEFINE(SCOPED_VAR, "temporary value")
    $INCLUDE(/some/other/template)

    $SET(AUTHOR, "Matt Harrison")
    $INCLUDE(/some/common/footer)
    ...

    some/common/header
    __________________
    <img $LOGO>
    <span class="was_once_a_font_tag">$HEADING</span>
    <br />
    ...


=head1 DESCRIPTION

HTML::DynamicTemplate is a class implementing a HTML/text
template in perl. The objective of the class is to provide a
mechanism by which to effectively decouple the design elements of a
page from the dynamic content, but also to provide for the
generalisation of dynamic content generation through the
use of extensible objects.

In this sense, the class strives to avoid the temptation
to let the swiss-knifing perl hacker introduce a word of
perl in a html page, and keep the pedantic
web-page designers out of our (my) lovely code ;-),
all the while keeping the power of perl through
the use of template variables as references to perl
code or arbitrarily-complex data structures.

Significant features include the ability to set template
variables from within perl code and the templates themselves,
the ability to recursively include/substitute other templates and
template variables, the tremendously useful ability to
define template variables as perl references and callback
subroutines, and the provision of an object-oriented
interface to promote the use of inheritance and polymorphism.

These features allow the programmer/designer to maintain a
consistent look and feel across multiple pages, while
keeping a common code base, and to change
either/both the content and layout of a site easily and
independently.

=head1 USAGE

HTML::DynamicTemplate uses 4 syntactic constructions
within HTML source files: template variables, of the
form '$VARIABLE'; the template variable-setting constructs
$SET and $DEFINE; and the template-including directive
$INCLUDE.

All variable and directive names are specified in uppercase.
This is enforced. A second concession to make is to be sure
to escape quotes (&quot;) and closing parantheses
(&#41;) as HTML entities. Lastly, anything that LOOKS like a template
variable -- it has a '$' followed by uppercase chars ( /$\w+/ ) --
will be substituted or removed. Therefore, to get a literal
$THIS in your html you have to escape the '$' as html. sorry.
Note also that the renderer doesn't care if template
variables/directives occur within HTML comments, though the
result of the substitution will still be within html comments.

=head2 Template variables

<span class="was_once_a_font_tag">  <-- This is standard HTML with an
embedded variable reference which is substituted
with the value of the template variable 'HIGHLIGHT' when
the template is rendered. Every instance of the text '$HIGHLIGHT'
in the given template, and all those recursively included
from this one will be replaced with this value.

Template variables may be set and re-set from within perl
by the method set(), or from within the template
itself by the template directives $SET() and $DEFINE(),
described below.

Variables themselves may also contain other template variable
references ie $SET(FOO, "some text $BAR") -- this works as you would
expect, though in the case of $DEFINE(), be aware of circular
references. Infinite recursion conditions are monitored
in any case.

=over 4

=item Template directive $SET()

    $SET(PAGE_TITLE, "What's New?")
    $SET(HIGHLIGHT, "color=red size=+1")
    $SET(PAGE_BODY, "background=$BACKGROUND link=$LINK_COLOUR")

Template variables are set within perl code using the set()
method or within the template itself with the $SET()
directive. This is useful when setting variables for use
by all included templates. For setting page-specific default
values which can be overriden by other templates/code,
see the $DEFINE directive.


=item Template directive $DEFINE()

    $DEFINE(TABLE_TITLE, 'Table I')
    $DEFINE(TITLE, "Page $PAGE_NO $PAGE_DESC")

In a similar fashion to $SET(), template variables may also be set
within templates using the $DEFINE directive. $DEFINE
operates like $SET, within the important exception that
variables defined with $DEFINE are <i>scoped</i> to the
template in which they were defined and to all templates
$INCLUDE-ed from this one.

The $DEFINE directive is analogous to the perl function
'local', with an important exception - $DEFINE-d variables
are overriden by variables which have been $SET or set().
The main purpose of $DEFINE is to provide page-specific
default values. In most cases it is more useful to
$DEFINE variables so they can be overriden by $SET
directives or perl code.

    eg:
        $DEFINE(TITLE, "DefaultTitle")
        <title>$TITLE</title>

If the above $DEFINE() directive were $SET(), the value of
$TITLE could never be altered from "DefaultTitle" because
it occurs immediately prior to the use of $TITLE.


=item Template directive $INCLUDE()

    $INCLUDE(templates/example.tmpl)
    $INCLUDE($PATH_TEMPLATES/$TABLE_TEMPLATE)

Additionally, templates may be recursively included by
another template by the $INCLUDE directive.

Template paths may also be variable references, as in
$INCLUDE($BANNER_AD). Any variable references found in
included templates will be substituted as in the original template.
The extension '.tmpl' for html templates is arbitrary but useful.

=back

=head2 Examples

For example, a simple but effective usage of this class from a script
may be to have a set or sets of template variables defined
in an external, centralised configuration file and to initialise
template object(s) from this -

eg:

    /main/conf/file
    _______________
    # note: valid perl!
    $DEFAULT = {
        TEXT_COLOUR     =>  'white',
        BACKGROUND      =>  'some_pic.png',
        ...
    };

    script
    ______
    {
        # load template vars into private namespace
        package Vars;
        do('/main/conf/file') or die;
    }

    my $template = new HTML::DynamicTemplate ( $main_template );
    $template->set( $Vars::DEFAULT );
    ...
    print $template->render();


As stated previously, DynamicTemplate endeavours to keep perl and
it's pseudo-guises out of html. However, all of perl's power is
avaliable through defining template variables as either references
to perl primitives or references to (callback) subroutines. A template
variable may be defined from perl by the following:

    $template->set('VAR', \&my_subroutine ); # OR
    $template->set('VAR',
        sub{
            my $template = shift;
            my $value = $template->get('SOME_VAR');
            $template->set('A_NEW_VAR', "here is a new $value");
            return 'some tasteful text, html, or $TEMPLATE_VAR\'s';
        }
    );

This subroutine will be called each time the text '$VAR' appears
in any template or included template. The return value will first be
evaluated for other template directives, and then will be substituted
in place of '$VAR'.

Template variables may also be set to a hash, array, scalar or
typeglob reference causes a special callback_* routine to be
called, where '*' is the type of reference. See the section on
the callback_* routines later in this document.

A simple but expressive use of this callback functionality creates
a RDBMS-backed dynamic web page (minus error checking and fluff):

    use DBI;
    use HTML::DynamicTemplate;

    #   init template + callbacks
    my $main_tmpl = new HTML::DynamicTemplate ('./page_template.tmpl');
    $main_tmpl->set( \%page_defaults );
    $main_tmpl->set( 'MY_CALLBACK',
        sub{
            my $tmpl = shift;
            my $value = $tmpl->get('SOME_COLUMN_VALUE');
            return reformatted( $value );
        }
    );

    #   DB code
    my $dbh = new DBI ();
    $dbh->connect( $db_connect_params );

    my $sth = $dbh->prepare("SELECT * FROM BLAH");
    $sth->execute();

    #   instantiate and print result template(s)
    while ( my $result_row = $sth->fetchrow_hashref() )
    {
        my $entry = $main_tmpl->new('./result_template.tmpl');
        $entry->set( $result_row );
        print $entry->render();
    }

Key to this approach is the use of template variables within
'./result_template' which have the same names as the column
names returned from the database. This allows each row result
to be passed directly to a template object or template objects.

Data which needs 'massaging' for presentation are defined in
terms of an extra template variable in 'result_template' which
is mapped to a callback subroutine as shown above. This allows
for clean and extensible cgi scripts where most of the ugly
html code is mapped to a series of template callback subroutines,
with the true html framework stored in a html template file.

In some ways, this model is anologous to the event-driven programming
of GUI toolkits, such as the AWT from java, where the event
of enountering a template variable in a html template (the 'gui')
'fires' the mapped response.

Note that a template object reference is passed as the
first argument to each callback subroutine (wherever it
resides), allowing the application programmer to control
other template variables through the use of get() and
set() routines, and to change other state information.

The use of closures to preserve private data is
also effective. eg:

    {
        my $counter = 0;
        $template->set( 'COUNT',
            sub{
                return ++$counter;
            }
        );
    }

With care and forethought, even your dynamic content-generation
code can be easily reused across objects, applications and
scripts.


=head2 Performance comments

DynamicTemplate I<sans> comments and pod weighs in at about 9Kb.
Parsing/Rendering of about 100Kb of a fairly complicated set of
templates (text only; complicated in the sense that they use many
variables and directives) takes about 18 msec on a moderate
(celeron 500) machine. Every effort to keep things clean
and brisk has been made - however please by all means send
me suggestions on improvements.

=cut


# Class methods /------------------------------------------

=head1 METHODS

=head2 Constructors

=over 4

=item new



    new( $template_filename )

        $template_filename  -   A path to a HTML template file.

Constructor for the template. Returns a reference to a
HTML::DynamicTemplate object based on the specified template file,
or an exception if the template file cannot be opened.

B<NOTE>: This constructor allows the use of the Class->new() syntax
as well as the $object->new() syntax to create new objects.
However, $object->new() ie: calling new() on an existing object
creates and new object and then B<clones> all aspects of the
calling object except for the HTML source given by the argument
$template_filename or \$source.

This means that the code...

    my $new_tmpl = $template->new("/some/template.tmpl");

...creates a new object and then (shallow) copies its set of template
variables into a NEW hash, as well as $template's other settings.
This is a convenience for the case where you want 2 or more copies
of an object with the same template variables defined, but want to
use them for independent html templates (As a side note, the
$SET() and $DEFINE() namespaces are completely separate - the
hash for $DEFINE() variables is dynamic and exists only at
rendering time...it can never be copied or inherited ).




    new( \$source )

        \$source    -   a scalar reference to a string containing
                        HTML with embedded template variables.

Alternative constructor, which takes a scalar reference to
HTML source. Returns an exception if passed a non-SCALAR reference.
Otherwise identical in effect to new( $template_filename ).

=cut
sub new
{
    my( $prototype, $template ) = @_;

    #   defaults for all objects
    my $this = {
        'recursion_limit'   =>  10,
        'template'          =>  undef,
        'source'            =>  $template,
        'vars'              =>  {},
    };

    #   allow $object->new() syntax as well as Class->new
    my $class = ref($prototype) || $prototype;
    bless( $this, $class );

    #   $object->new() syntax makes $this a clone of $object...
    #   ...except that 'source' is always object-specific
    if ( ref($prototype) )
    {
        $this->{'recursion_limit'} = $prototype->{'recursion_limit'};
        $this->{'vars'} = { %{$prototype->{'vars'}} }
    }

    return $this;
}



# Object methods /-----------------------------------------

=back

=head2 Object methods

=over 4

=item clear



    clear()

Clears template variables. Useful when processing table row
templates.




    clear( @variables )

        @variables      -   List of variables to clear

Clears only the template variables specified by @variables.
Note that template variable names are always given in
uppercase.




    clear( \@variables )

        \@variables     -   List of template variables to clear

Same as for clear( @variables ), but argument is passed as
a reference, which is faster if @variables
contains many elements.

=cut
sub clear
{
    my $this = shift;
    my( $arg ) = @_;

    if ( ref($arg) =~ /ARRAY/ )
    {   map { delete $this->{'vars'}->{$_} } @$arg;
    }
    elsif ( $arg )
    {   map { delete $this->{'vars'}->{$_} } @_;
    }
    else
    {   $this->{'vars'} = {};
    }

    return 1;
}

=item has_defined



    $boolean = $this->has_defined('TEMPLATE_VAR')

        $boolean    -   boolean true or false

Return a boolean value indicating whether this
template has the template variable $TEMPLATE_VAR.
<B:Note>: that boolean true is returned even if
$TEMPLATE_VAR is itself false or undefined, a la
perl exists().

=cut
sub has_defined
{
    my $this = shift;
    return exists( $this->{'vars'}->{shift} );
}

=item get



    $value = $this->get( $var_name )

        $value      -   The value of the template
                        variable $var_name.
        $var_name   -   A template variable name

Returns the current value of the template variable given
by $var_name. Returns undef if the template variable name
is non-existent and returns boolean false if the template
variable given by $var_name exists but has no value (is false
or undefined).

Note that $value may contain any valid perl scalar, including
references to CODE, HASHes, ARRAYs, SCALARs etc..



    get()

This usage returns a reference to the hash of all
currently-defined template variables. Note that
template variables are always uppercase and are true
for the regular expression /^\w+$/.

=cut
sub get
{
    my $this = shift;
    my $key = shift || return $this->{'vars'};
    return $this->{'vars'}->{$key};
}


=item render



    $HTML = $this->render()

        $HTML       -   A chunk of HTML as a string, with
                        all template variables removed or
                        substituted.

Renders the current template object to 'pure' html, performing all
template variable substitutions and template directives as found
in the source template. Template variables and directives are
described in the section 'USAGE'. Returns html with all
template variables substituted and removed.

Returns a blob of 'pure' html (Can html ever be pure? ;-) ).




    $HTML = $this->render()
    $HTML = $this->render( @templates )

        $HTML           -   A chunk of HTML as a string, with
                            all template variables removed or
                            substituted.

Renders template(s) by performing template variable substitutions.
If a starting template was not given in the constructor, then at
least one template file/scalar reference/glob reference must be
given here. If a source template was given in the constructor and
then others are also given to this method, then the templates
given here are appended to the constructor source template.

=cut
sub render
{
    my( $this, @templates ) = @_;

    unshift @templates, $this->{'source'} if defined $this->{'source'};

    local $^W = "";
    my $html;

    foreach my $template ( @templates ) {
        my $source = $this->_extract_template_source( $template );
        $html .= $this->_substitute( $source );
    }

    return $html;
}

=item set



    $overwritten = $this->set( 'NAME' => $value )
    $overwritten = $this->set( %parameters )

        $overwritten    -   number of template variables redefined.
        $value          -   Any perl scalar. see description.
        %parameters     -   Hash of template variable names
                            mapped to values, as in $value.

Sets template variable to given value. $value can be just about any
legal perl scalar. Any non-reference $value will be substituted
verbatim in the given template wherever the text '$NAME' is
found. $value may of course contain other template variables
or template directives (note that $SET() directives used
from within a template cannot contain another $SET(), though can
contain $INCLUDE and $VAR subtitutions. set() can set anything to
anything ).

If $value contains a reference to code or a subroutine, then that
code will be called for each instance of $NAME in a template. If
$value contains any other reference type, then one of the various
callback_*() routines will be called. see previous description
of template variables.




    $overwritten = $this->set( \%parameters )

        $overwritten    -   number of template variables redefined.
        %parameters     -   Hash of template variable names
                            mapped to values, as in $value.

Adds to the set of template variables given by
%variables to the current set. Otherwise identical in effect
to set( %hash ). This usage throws an exception if passed
a non-HASH reference.

=cut
sub set
{
    my $this = shift;
    return unless @_;

    my $vars = $this->{'vars'};
    my $overwritten = 0;

    if ( @_ == 1 )
    {
        my $href = shift;
        eval
        {
            map {
                my $name = uc($_);
                $vars->{$name} = $$href{$_} || '';
                $overwritten++ if exists( $vars->{$name} );
            } keys %$href;
        };
        if ( $@ )
        {
            carp "Single argument '$href' to set() " .
                 "must be a hash object or hash reference ($@)";
            croak if $STRICT;
            return;
        }
    }
    else
    {
        while ( my $name = uc( shift ) )
        {
            carp "Uneven number of arguments in hash" unless $name;
            $overwritten++ if exists( $vars->{$name} );
            $vars->{$name} = shift || '';
        }
    }

    return $overwritten;
}

=item set_recursion_limit



    set_recursion_limit( $depth )

        $depth      -   An integer value indicating
                        the depth to recursively include
                        templates (via $INCLUDE).

A default recursion limit for template includes is implemented to
prevent infinite recursions. Use this method to override the
default value (10).

=cut
sub set_recursion_limit
{
    my( $this, $limit ) = @_;
    $this->{'recursion_limit'} = $limit
        if $limit =~ /^\d+$/;
}


# Abstract methods /---------------------------------------

=back

=head2 Abstract methods

HTML::DynamicTemplate includes a set of 'abstract' methods for
application programmers to override by subclassing.
Each of these methods are called by HTML::DynamicTemplate
whenever a template variable containing a (non-CODE) reference
is encountered in a html template (during a call to render() ).
CODE references are called directly, and the returned value
substituted in place of the template variable.

In the case of other types of references, HTML::DynamicTemplate
calls the appropriate callback_* method,
where the '*' is one of SCALAR, ARRAY, HASH, and GLOB
as determined from the type of reference. There is
currently no allowance for a callback method for
objects.

For instance, setting a template variable to
an array reference eg:

    $template->set( 'SOME_VAR', \@list );

The callback_ARRAY() method will be called for
each incidence of the template variable $SOME_VAR in
the html template encapsulated by $template ( see new() ),
because it contains an ARRAY reference.

The current implementations for all of the callback_* routines
in HTML::DynamicTemplate return the empty string "".

Of course, there is no obligation to override all
of the callback_* routines, as the default
implementation for all the callback_* routines
return the empty string "".


Overriding the default implementation of one or more of
the callback_* methods may be as simple as:

    #   a perl 'inner class' ;-)
    {
        package RowTemplate;
        use base 'HTML::DynamicTemplate';

        sub callback_ARRAY
        {
            my( $this, $name, $value ) = @_;
            # your implementation here...
            return 'some html';
        }
    }
    ...
    # somewhere else...
    my $row_tmpl = new RowTemplate ($filename);

An object created from a derived class can of course be used
as any other HTML::DynamicTemplate object, except
for it's behaviour with template variables
which contain array references, thanks to the power
of object polymorphism.

B<Method arguments>

All callback methods will be called with the
following arguments:

    @_ = ( $this_template_obj, $variable_name, $value_reference )

where:

    $this_template_obj  -> 'this' or 'self' or whatever you call your object refs
    $variable_name      -> the name of the template variable
    $value_reference    -> value of the template variable,
                           a reference by definition.

The overridden method can return any valid html,
including html with other template variables. This return
value is substituted into the 'calling' html template.

=over 4

=item callback_ARRAY

    $HTML = $this->callback_ARRAY( 'VAR_NAME', \@array )

        $HTML       -   A chunk of html as a string, possibly
                        containing other embedded template variables.
        'VAR_NAME'  -   The template variable name that refers to
                        the array reference \@array.
        \@array     -   The reference to the array of data referred
                        to by the template variable 'VAR_NAME'.

Only called when a template variable contains an
ARRAY reference. Default implementation returns
the empty string "".

=cut
sub callback_ARRAY  {    return ""   }


=item callback_HASH

    $HTML = $this->callback_HASH( 'VAR_NAME', \%hash )

        $HTML       -   A chunk of html as a string, possibly
                        containing other embedded template variables.
        'VAR_NAME'  -   The template variable name that refers to the
                        hash reference \%hash.
        \%hash      -   The reference to the hash of data referred
                        to by the template variable 'VAR_NAME'.

Only called when a template variable contains a
reference to a HASH. Default implementation
returns the empty string "".

=cut
sub callback_HASH   {     return ""   }

=item callback_SCALAR

    $HTML = $this->callback_SCALAR( 'VAR_NAME', \$scalar )

        $HTML       -   A chunk of html as a string, possibly
                        containing other embedded template variables.
        'VAR_NAME'  -   The template variable name that refers to the
                        scalar reference \$scalar.
        \$scalar    -   The reference to the scalar referred
                        to by the template variable 'VAR_NAME'.

Only called when a template variable contains
a reference to a scalar. Default implementation
returns the empty string "".

=cut
sub callback_SCALAR {    return ""    }

=item callback_GLOB

    $HTML = $this->callback_GLOB( 'VAR_NAME', \*glob )

        $HTML       -   A chunk of html as a string, possibly
                        containing other embedded template variables.
        'VAR_NAME'  -   The template variable name that refers to the
                        typeglob reference \*glob.
        \*glob      -   The reference to the typeglob referred
                        to by the template variable 'VAR_NAME'.

By default, this sub assumes that the typeglob is a reference to an
open filehandle to template source. If so, it attempts to read and
substitute it.

=cut
sub callback_GLOB {
    my( $this, $var_name, $glob );
    return <$glob>;
}


####
#
#   $src = _extract_template_source( \$template_source );
#   $src = _extract_template_source( $template_source_filename );
#   $src = _extract_template_source( \*FILEHANDLE_REFERENCE );
#
#   obtains template source from a filehandle, file (name), or
#   scalar reference.
#
sub _extract_template_source
{
    my( $this, $input ) = @_;
    my $source;

    if ( ref($input) eq 'SCALAR' ) {
        $source = $$input;
    }
    elsif ( ref($input) eq 'GLOB' ) {
        local $/ = undef;
        $source = <$input>;
    }
    elsif ( $input ) {
        local $/ = undef;
        open( my $fh, "< $input") ||
            croak "Couldn't open '$input': $!";

        $source = <$fh>;
        close( $fh ) || croak $!;
    }

    return $source;
}


####
#
#   _recover_gracefully( \@variable_stack, \%scoped_vars )
#
#   only called when there are variables which point to one
#   another resulting in (otherwise) infinite recursion.
#   ie:     $DEFINE(FOO, "some text $BLAH")
#           $DEFINE(BAR, "some other text $FOO")
#
#   Provides a simple catch and recover depending on the
#   value of the class global $STRICT. If $STRICT is
#   false, then don't die, but instead send the error
#   to the browser, clear the culprit vars, and recover.
#
sub _recover_gracefully
{
    my( $this, $stack, $scoped ) = @_;

    my %seen;
    while( my $var = pop @$stack )
    {
        last if $seen{$var};
        $seen{$var}++;
    }
    my @unique = keys %seen;

    carp "\n### infinite recursion detected between vars: @unique";
    unless ( $STRICT )
    {   foreach ( @unique )
        {   if ( exists($scoped->{$_}) )
            {   $scoped->{$_} =
                    "[ infinite recursion between vars: @unique ]";
            }
            elsif ( exists($this->{'vars'}->{$_}) )
            {   $this->{'vars'}->{$_} =
                    "[ infinite recursion between vars: @unique ]"
            }
        }
        return;
    }
    croak;
}

####
#
#   _include( 'template_name', \@sub_stack, \@var_stack )
#
#   load and substitute another template from an $INCLUDE directive.
#   monitors for deep recursion of included templates with a
#   stack (LIFO) '$sub_stack'.
#
sub _include {
    my( $this, $text, $sub_stack, $var_stack, %scoped ) = @_;
    my $source = "";

    #   $INCLUDE argument can contain template vars/directives
    #
    my $template = $this->_substitute(
        $text, $sub_stack, $var_stack, %scoped
    );

    #   slurp file in one gulp;
    #   uses closure to not propagate local $/
    {
        my $fh;
        local $/ = undef;
        unless ( open( $fh, $template ) )
        {   carp
                "DynamicTemplate: Couldn't open \'$template\': $!\n" .
                "originating at: ->> $text <<-\n"
            ;
            carp join ' -=> ', @$sub_stack . "\n" if ref( $sub_stack );
            return "[ \'$template\': $! ]" unless $STRICT;
            croak;
        }
        $source = <$fh>;
        close( $fh );
    }

    #   push stack, recurse, and pop stack only when we return.
    #   disallow deep recursion.
    #
    push @$sub_stack, $template;
    if ( @$sub_stack > $this->{'recursion_limit'} )
    {   carp
            "\n\$INCLUDE recursion limit exceeded...\n" .
            ( join ' --includes--> ', @$sub_stack ) .
            "\nat: ->> $text <<-\n"
        ;
        return "[ recursion limit exceeded ]" unless $STRICT;
        croak;
    }
    $source = $this->_substitute(
        $source, $sub_stack, $var_stack, %scoped
    );
    pop @$sub_stack;

    return $source;
}

####
#
#   _replace_var( 'variable_name', \@sub_stack, \@var_stack, %scoped_vars )
#
#   recursively substitute a template variable. monitors
#   a stack (LIFO) of last-used variables to prevent infinite
#   recursion. infinite recursion exists when stack length
#   exceeds the number of defined template vars.
#
sub _replace_var
{
    my( $this, $var, $sub_stack, $var_stack, %scoped ) = @_;

    my $value = ( exists($this->{'vars'}->{$var}) )
        ?   $this->{'vars'}->{$var}
        :   $scoped{$var}
    ;

    return "" unless $value;
    my $text = "";

    if ( my $ref = ref($value) )
    {
        if ( $ref =~ /CODE/ )
        {
            $text = $value->( $this, $var );
        }
        else
        {
#            my $callback = "callback_$ref";
            my $callback = $this->get("CALLBACK_$ref")
                        || $this->can("callback_$ref")
                        || die;

            croak "CALLBACK_$ref is not a code reference"
                unless ref($callback) eq 'CODE';

#            if ( $this->can( $callback ) )
#            {
            eval {
                $text = $callback->( $this, $var, $value )
            };
            if ( $@ )
            {   carp "### Caught fatal exception from callback $callback: $@";
                croak if $STRICT;
            }
#            }
        }
        $value = $text;
    }

    push @$var_stack, $var;
    $this->_recover_gracefully( $var_stack, \%scoped )
        if ( @$var_stack > keys %{$this->{'vars'}} );
    $text = $this->_substitute(
        $value, $sub_stack, $var_stack, %scoped
    );
    pop @$var_stack;

    return $text;
}

####
#
#   _substitute( $source, \@sub_stack, \$var_stack, %scoped_vars )
#
#   the central substitution routine. uses brian's reg-exp to
#   replace template directives in situ. perhaps a better
#   implementation may be to print/output substituted source
#   in real-time from a while() loop...need to benchmark.
#
#   inlining the _replace_var() code is about 14% faster -
#   about 18 msec -> 15.5 msec for one full render of ~100K
#   worth of templates on my dual celery 500, 256M ram.
#   probably better to be able to understand the
#   code more easily, i think? the choice is yours...
#
sub _substitute
{
    my( $this, $source, $sub_stack, $var_stack, %scoped ) = @_;
    $this->{'scoped'} = \%scoped;
    return unless defined $source;

    $source =~ s{
        \$                      # must start with '$'
        ([_A-Z][0-9_A-Z]+)      # save any following alphanumerics
        (\(([^)]+)\))?          # save optional stuff in brackets
        (\n?)                   # preserve the \n if present
    }{
        if ( $1 eq 'SET' && defined $2 )
        {
            if ( $3 =~ m|^([0-9_A-Z]+)\s*,\s*"([^"]+)"$| )
            {
                $this->{'vars'}->{$1} = $this->_substitute(
                    $2, $sub_stack, $var_stack, %scoped
                );
            }
            ''
        }
        elsif ( $1 eq 'DEFINE' and defined $2 )
        {
            if ( $3 =~ m|^([_A-Z][0-9_A-Z]+)\s*,\s*"([^"]*)"$| )
            {
                $scoped{$1} = $2 || "";
            }
            ''
        }
        elsif ( $1 eq 'INCLUDE' and defined $2 )
        {
            $this->_include(
                $3, $sub_stack, $var_stack, %scoped
            ) . $4
        }
        else
        {   $this->_replace_var(
                $1, $sub_stack, $var_stack, %scoped
            ) . $4
        }
    }egx;

    return $source;
}


1;
__END__

=back

=head1 CREDITS

=head2 Authors and contacts

    Matt Harrison <mailto://mharriso@rna.bio.mq.edu.au>
    Brian Ng <mailto://brian@m80.org>
    Brian Slesinsky

This module is based on an original version of a
module by Brian Ng, which in turn was based on
an original work by Brian Slesinsky. Almost all
of Brian Ng's version was rewritten by Matt Harrison
to include a superset of the functionality present
in the original, in order to support things like
callbacks, references, and nested variables in
template variables. This new module forms the
delivery backbone of an as yet publically-unreleased
bioinformatics project. This module is fully backwardly
compatible with Brian's module as far as i am aware.

Please feel free to contact me, matt harrison, for technical comments
and/or suggestions for improvement, stupid things i've done or
whatever. Questions about usage not covered by the document are
probably also welcome. Enjoy.

=head2 Legalese

Copyright 2000 Matt Harrison; all rights reserved.
Written during the month of april 2000, Matt Harrison.

Some pieces of code were taken verbatim from Brian's module -
this code is his.

This program is free software; you are free to redistribute it
and/or modify it under the same terms as Perl itself. The authors
make no claim of warranty, express, implied or even vaguely hinted at.
This means: you use it at your own risk.

=cut

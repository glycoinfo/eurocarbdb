#
#   name: how_to_use_python_parser.py 
#   description: an example script to run ANTLR-generated python GlycoCT lexers/parsers
#   author: matt harrison
#
#   see also http://www.antlr2.org/doc/python-runtime.html for further info
#   on how to build/use ANTLR-generated python lexers/parsers
#

import antlr
import StringIO
import GlycoctLexer
import PythonGlycoctParser

a_correct_sequence = "RES 1b:a-dgro-dgal-non-2:6|1:a|2:keto|3:d;1s:n-glycolyl;LIN 1:1o(5-1)2d;"

an_incorrect_sequence = "RES 1b:a-dgro-dgal-non-2:6|1:a|2:keto|3:d;1s:n-glycolyl;LI 1:1o(5-1)2d;"


#--------------------------------------------------------------------
def main():
    print "parsing a correct sequence..."
    parse( a_correct_sequence )
    print
    
    print "parsing an incorrect sequence..."
    parse( an_incorrect_sequence )

#--------------------------------------------------------------------
def parse( sequence ):
    print "parsing sequence: " + sequence
    
    filehandle = StringIO.StringIO( sequence )
    
    #   create lexer 
    lexer = GlycoctLexer.Lexer( filehandle )
    
    #   create parser to use token stream from lexer
    parser = PythonGlycoctParser.Parser( lexer )
    
    #   invoke the top-level parsing rule.
    try:
        parser.sugar()
        
    except antlr.NoViableAltException, e:
        syntax_error( sequence, e, e.args[0].getColumn() )
        
    except antlr.RecognitionException, e:
        syntax_error( sequence, e, e.column )
    
    except antlr.TokenStreamRecognitionException, e:
        syntax_error( sequence, e, e.recog.column )
    
    except ANTLRException, e:
        syntax_error( sequence, e )
    
    else:
        print "sequence parsed ok"


#--------------------------------------------------------------------
def syntax_error( sequence, exception, position=0 ):
    print "syntax error: " + str(exception.__class__.__name__) + ": " + exception.message 
    print sequence
    
    if position > 0:
        print ' ' * (position - 1) + "^  (char " + str(position) + ")"

#--------------------------------------------------------------------

main()


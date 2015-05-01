The files in this directory are for the python versions of sugar sequence format parser 
grammars. The files are:

author: matt harrison, Jan 2008

* how_to_use_python_parser.py
an example python script of how to invoke the generated parser. note: i am a python-newbie.

* glycoct_grammar.py.g
the ANTLR (v2.7) grammar file, with some adjustments to grammar actions to make them python-friendly.

* GlycoctParser.py
the ANTLR-generated parser

* GlycoctLexer
the ANTLR-generated lexer

* PythonGlycoctParser.py
Specifies the methods that are called by the parser during the parsing of a sequence. In the Java version I have code that progressively adds linkages and residues to a quite full-featured Graph class (which underpins the Sugar object), but you would need to write your own code here as you deem appropriate.

The parser & lexer are generated from the grammar with the command:
$ java -cp /usr/local/lib/antlr.jar antlr.Tool glycoct_grammar.py.g

Antlr version 2.7 is required to generate & run the parser/lexer. It is available from http://www.antlr2.org



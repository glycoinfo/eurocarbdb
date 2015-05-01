### $ANTLR 2.7.7 (20080103): "glycoct_grammar.py.g" -> "GlycoctLexer.py"$
### import antlr and other modules ..
import sys
import antlr

version = sys.version.split()[0]
if version < '2.2.1':
    False = 0
if version < '2.3':
    True = not False
### header action >>> 

### header action <<< 
### preamble action >>> 

### preamble action <<< 
### >>>The Literals<<<
literals = {}
literals[u"pen"] = 15
literals[u"hept"] = 16
literals[u"hex"] = 14
literals[u"non"] = 17


### import antlr.Token 
from antlr import Token
### >>>The Known Token Types <<<
SKIP                = antlr.SKIP
INVALID_TYPE        = antlr.INVALID_TYPE
EOF_TYPE            = antlr.EOF_TYPE
EOF                 = antlr.EOF
NULL_TREE_LOOKAHEAD = antlr.NULL_TREE_LOOKAHEAD
MIN_USER_TYPE       = antlr.MIN_USER_TYPE
RES = 4
LIN = 5
PRO = 6
REP = 7
INTEGER = 8
SEMICOLON = 9
MONOSAC_DECLARATION = 10
SUBSTIT_DECLARATION = 11
HYPHEN = 12
IDENTIFIER = 13
LITERAL_hex = 14
LITERAL_pen = 15
LITERAL_hept = 16
LITERAL_non = 17
COLON = 18
UNKNOWN_TERMINUS = 19
PIPE = 20
COMMA = 21
LPARENTHESIS = 22
RPARENTHESIS = 23
INCHI_DECLARATION = 24
STA = 25
ISO = 26
AGL = 27
WS = 28


###/**
###*
###*   This class implements a lexer/scanner for carbohydrate
###*   sequences in Glycoct syntax. This class was auto-generated from
###*   the ANTLR lexer grammar in glycoct_grammar.g.
###*
###*   @see GlycoctParser
###*   @see glycoct_grammar.g
###*
###*   @author mjh [matt@ebi.ac.uk]
###*/
class Lexer(antlr.CharScanner) :
    ### user action >>>
    ### user action <<<
    def __init__(self, *argv, **kwargs) :
        antlr.CharScanner.__init__(self, *argv, **kwargs)
        self.caseSensitiveLiterals = True
        self.setCaseSensitive(True)
        self.literals = literals
    
    def nextToken(self):
        while True:
            try: ### try again ..
                while True:
                    _token = None
                    _ttype = INVALID_TYPE
                    self.resetText()
                    try: ## for char stream error handling
                        try: ##for lexical error handling
                            la1 = self.LA(1)
                            if False:
                                pass
                            elif la1 and la1 in u':':
                                pass
                                self.mCOLON(True)
                                theRetToken = self._returnToken
                            elif la1 and la1 in u',':
                                pass
                                self.mCOMMA(True)
                                theRetToken = self._returnToken
                            elif la1 and la1 in u'-':
                                pass
                                self.mHYPHEN(True)
                                theRetToken = self._returnToken
                            elif la1 and la1 in u'|':
                                pass
                                self.mPIPE(True)
                                theRetToken = self._returnToken
                            elif la1 and la1 in u';':
                                pass
                                self.mSEMICOLON(True)
                                theRetToken = self._returnToken
                            elif la1 and la1 in u'(':
                                pass
                                self.mLPARENTHESIS(True)
                                theRetToken = self._returnToken
                            elif la1 and la1 in u')':
                                pass
                                self.mRPARENTHESIS(True)
                                theRetToken = self._returnToken
                            elif la1 and la1 in u'?':
                                pass
                                self.mUNKNOWN_TERMINUS(True)
                                theRetToken = self._returnToken
                            elif la1 and la1 in u'0123456789':
                                pass
                                self.mINTEGER(True)
                                theRetToken = self._returnToken
                            elif la1 and la1 in u'L':
                                pass
                                self.mLIN(True)
                                theRetToken = self._returnToken
                            elif la1 and la1 in u'P':
                                pass
                                self.mPRO(True)
                                theRetToken = self._returnToken
                            elif la1 and la1 in u'S':
                                pass
                                self.mSTA(True)
                                theRetToken = self._returnToken
                            elif la1 and la1 in u'I':
                                pass
                                self.mISO(True)
                                theRetToken = self._returnToken
                            elif la1 and la1 in u'A':
                                pass
                                self.mAGL(True)
                                theRetToken = self._returnToken
                            elif la1 and la1 in u'\t\n\r ':
                                pass
                                self.mWS(True)
                                theRetToken = self._returnToken
                            else:
                                if (self.LA(1)==u'R') and (self.LA(2)==u'E') and (self.LA(3)==u'S'):
                                    pass
                                    self.mRES(True)
                                    theRetToken = self._returnToken
                                elif (self.LA(1)==u'R') and (self.LA(2)==u'E') and (self.LA(3)==u'P'):
                                    pass
                                    self.mREP(True)
                                    theRetToken = self._returnToken
                                elif (self.LA(1)==u'b') and (self.LA(2)==u':'):
                                    pass
                                    self.mMONOSAC_DECLARATION(True)
                                    theRetToken = self._returnToken
                                elif (self.LA(1)==u's') and (self.LA(2)==u':'):
                                    pass
                                    self.mSUBSTIT_DECLARATION(True)
                                    theRetToken = self._returnToken
                                elif (self.LA(1)==u'i') and (self.LA(2)==u':'):
                                    pass
                                    self.mINCHI_DECLARATION(True)
                                    theRetToken = self._returnToken
                                elif ((self.LA(1) >= u'a' and self.LA(1) <= u'z')) and (True):
                                    pass
                                    self.mIDENTIFIER(True)
                                    theRetToken = self._returnToken
                                else:
                                    self.default(self.LA(1))
                                
                            if not self._returnToken:
                                raise antlr.TryAgain ### found SKIP token
                            ### option { testLiterals=true } 
                            self.testForLiteral(self._returnToken)
                            ### return token to caller
                            return self._returnToken
                        ### handle lexical errors ....
                        except antlr.RecognitionException, e:
                            raise antlr.TokenStreamRecognitionException(e)
                    ### handle char stream errors ...
                    except antlr.CharStreamException,cse:
                        if isinstance(cse, antlr.CharStreamIOException):
                            raise antlr.TokenStreamIOException(cse.io)
                        else:
                            raise antlr.TokenStreamException(str(cse))
            except antlr.TryAgain:
                pass
        

    ###/** A literal colon ':' */
    def mCOLON(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = COLON
        _saveIndex = 0
        pass
        self.match(':')
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** A literal comma ',' */
    def mCOMMA(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = COMMA
        _saveIndex = 0
        pass
        self.match(',')
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** A literal hyphen '-' */
    def mHYPHEN(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = HYPHEN
        _saveIndex = 0
        pass
        self.match('-')
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** A literal pipe symbol '|' */
    def mPIPE(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = PIPE
        _saveIndex = 0
        pass
        self.match('|')
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** A literal semicolon ';' */
    def mSEMICOLON(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = SEMICOLON
        _saveIndex = 0
        pass
        self.match(';')
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** A literal left parenthesis '(' */
    def mLPARENTHESIS(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = LPARENTHESIS
        _saveIndex = 0
        pass
        self.match('(')
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** A literal right parenthesis ')' */
    def mRPARENTHESIS(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = RPARENTHESIS
        _saveIndex = 0
        pass
        self.match(')')
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** A literal '?', indicating an unknown terminal position. */
    def mUNKNOWN_TERMINUS(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = UNKNOWN_TERMINUS
        _saveIndex = 0
        pass
        self.match('?')
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** A literal string "b:", which declares a following monosaccharide section. */
    def mMONOSAC_DECLARATION(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = MONOSAC_DECLARATION
        _saveIndex = 0
        pass
        self.match("b:")
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** A literal string "s:", which declares a following substituent section. */
    def mSUBSTIT_DECLARATION(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = SUBSTIT_DECLARATION
        _saveIndex = 0
        pass
        self.match("s:")
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** A literal string "i:", which declares a section of INCHI code. */
    def mINCHI_DECLARATION(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = INCHI_DECLARATION
        _saveIndex = 0
        pass
        self.match("i:")
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** Rule for a (positive) integer, or zero. */
    def mINTEGER(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = INTEGER
        _saveIndex = 0
        la1 = self.LA(1)
        if False:
            pass
        elif la1 and la1 in u'123456789':
            pass
            pass
            self.matchRange(u'1', u'9')
            while True:
                if ((self.LA(1) >= u'0' and self.LA(1) <= u'9')):
                    pass
                    self.matchRange(u'0', u'9')
                else:
                    break
                
        elif la1 and la1 in u'0':
            pass
            self.match('0')
        else:
                self.raise_NoViableAlt(self.LA(1))
            
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** Rule for an identifier, which may be any alphabetic string. */
    def mIDENTIFIER(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = IDENTIFIER
        _saveIndex = 0
        pass
        _cnt52= 0
        while True:
            if ((self.LA(1) >= u'a' and self.LA(1) <= u'z')):
                pass
                self.matchRange(u'a', u'z')
            else:
                break
            
            _cnt52 += 1
        if _cnt52 < 1:
            self.raise_NoViableAlt(self.LA(1))
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** Rule for a literal string "RES", identifying a RES section. */
    def mRES(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = RES
        _saveIndex = 0
        pass
        self.match("RES")
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** Rule for a literal string "LIN", identifying a LIN section. */
    def mLIN(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = LIN
        _saveIndex = 0
        pass
        self.match("LIN")
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** Rule for a literal string "PRO", identifying a PRO section. */
    def mPRO(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = PRO
        _saveIndex = 0
        pass
        self.match("PRO")
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** Rule for a literal string "REP", identifying a REP section. */
    def mREP(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = REP
        _saveIndex = 0
        pass
        self.match("REP")
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** Rule for a literal string "STA", identifying a STA section. */
    def mSTA(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = STA
        _saveIndex = 0
        pass
        self.match("STA")
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** Rule for a literal string "ISO", identifying a ISO section. */
    def mISO(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = ISO
        _saveIndex = 0
        pass
        self.match("ISO")
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** Rule for a literal string "AGL", identifying a AGL section. */
    def mAGL(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = AGL
        _saveIndex = 0
        pass
        self.match("AGL")
        self.set_return_token(_createToken, _token, _ttype, _begin)
    

    ###/** 
    ###*   The "whitespace" rule, comprising space, tab, and return. 
    ###*   These tokens are disregarded when parsing. 
    ###*/
    def mWS(self, _createToken):    
        _ttype = 0
        _token = None
        _begin = self.text.length()
        _ttype = WS
        _saveIndex = 0
        pass
        la1 = self.LA(1)
        if False:
            pass
        elif la1 and la1 in u' ':
            pass
            self.match(' ')
        elif la1 and la1 in u'\t':
            pass
            self.match('\t')
        elif la1 and la1 in u'\n\r':
            pass
            la1 = self.LA(1)
            if False:
                pass
            elif la1 and la1 in u'\r':
                pass
                pass
                self.match('\r')
                self.match('\n')
            elif la1 and la1 in u'\n':
                pass
                self.match('\n')
            else:
                    self.raise_NoViableAlt(self.LA(1))
                
            self.newline();
        else:
                self.raise_NoViableAlt(self.LA(1))
            
        _ttype =  Token.SKIP;
        self.set_return_token(_createToken, _token, _ttype, _begin)
    
    
    
### __main__ header action >>> 
if __name__ == '__main__' :
    import sys
    import antlr
    import GlycoctLexer
    
    ### create lexer - shall read from stdin
    try:
        for token in GlycoctLexer.Lexer():
            print token
            
    except antlr.TokenStreamException, e:
        print "error: exception caught while lexing: ", e
### __main__ header action <<< 

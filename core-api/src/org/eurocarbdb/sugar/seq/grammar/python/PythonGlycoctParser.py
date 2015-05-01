
import GlycoctParser

class Parser( GlycoctParser.Parser ):
    """
    This class specifies the minimal interface required by the Python
    version of the EurocarbDB Glycoct parser
    
    author: matt harrison
    """
    
    def addResidue( self, residue_token ):
        """
        Add a residue to the current Sugar graph. In Glycoct, the 
        first residue in the residue list (and therefore the first residue
        encountered) is always the root residue.
        """
        pass
    
    
    def addLinkage( self, i, nrti, nrtt, lnrt, lrt, rti, rtt ):
        """ 
        Add a linkage token to the current Sugar graph. 
        The nrti & rti arguments specify which residues are being linked
        (by their IDs in the glycoct residue list), and the lnrt & lrt
        arguments specify the actual linkage terminus positions.
        
        arguments: 
            i    - linkage numbering order 
            nrti - non-reducing terminal residue id/index 
            nrtt - non-reducing terminal residue type 
            lnrt - the linkage non-reducing terminus position 
            lrt  - the linkage reducing terminus position 
            rti  - reducing terminal residue id/index
            rtt  - reducing terminal residue type
    
        All of these arguments are ANTLR Token objects.  
        """
        pass
    
    
    def addModification( self, modification_token, position1_token, position2_token=None ):
        """ 
        Add a modification to the current (last) residue/monosaccharide, at
        the given position. The second position token is optional and only 
        given for modifications that span more than 1 carbon of the sugar ring.
        """
        pass


    def createResidueToken( self, residue_token ):
        """ 
        Factory-style creator of residue tokens from the raw residue name token,
        eg: verification that a reisude name was valid could be done here.
        """
        return residue_token
    
    
    def setSuperclass( self, superclass_token ):
        """ Sets the superclass type of the current (last) residue """
        pass


    def setRingClosure( self, ring_close_position1_token, ring_close_position2_token ):
        """ Sets the ring closure terminal positions on the current (last) residue """
        pass
    
    

# - The excludeDirs, excludeFiles, and excludeFileMatches imported from
#   memops.scripts.license.headers are first checked.
# - Then includeDirs, excludeDirs, excludeFiles, excludeFileMatches are checked.
# - Then the elements of licenseInfo are checked in order,
#   and license headers are inserted.
# - Once a file is matched it is excluded from further checking
# - Files that are left after all licenseInfo has been processed, are
#   skipped, or cause a warning, depending on the parameters of the calling 
#   function.



# includeDirs is a list of directory names (must match exactly)
# If includeDirs are set, excludeDirs must be empty and vice versa.
# If includeDirs are not empty it means that only these directories are included
# includeDirs overrides the global memops.scripts.license.headers.excludeDirs
includeDirs = []

# excludeDirs is 'all', or a list of directory names (must match exactly)
# Unless includeDirs is set, all directories except those in excludeDirs, are treated
excludeDirs = []  # CVS and local always excluded: see memops/scripts/license/headers.py

# excludeFiles is 'all',or a list of file names (must match exactly)
excludeFiles = []

# excludeFileMatches is a list of regular expressions matching file names
excludeFileMatches = []

# licenseInfo: a list of dictionaries with information for making a header
# The elements of the list are tested in order
#
# - includeFiles is 'all', or a list of file names (must match exactly)
# - includeFileMatches is a list of regular expressions matching file names
# - format describes the format to be used when entering the license
#   (i.e. how comments are delimited). It is from an enumerated list. 
#   if format is None the format is derived from the file extension
# - author, organization, and useLicense must have a value, the rest are optional
# - author, organization, programFunction, programType, extraContact, and 
#   credits are strings that are entered directly in the license header 
# - useLicense, stdContact, and the members of the references tuple refer to
#   information defined elsewhere. The options are (were?):
#     useLicense : ('GPL','LGPL','ccpn','restricted')
#     stdContact : ('ccpn',) may be None
#     references : ('CCPN2002',) may be empty
# - extraContacts serve instead of or in addition to stdContact.
#   One of them must be given.
# - credits serve instead of or in addition to references. Both can be omitted.

licenseInfo = (
 {'includeFiles':'all',
  'includeFileMatches':[],
  'format':'xml',
  'author':'Tim J. Stevens',
  'organization':'University of Cambridge',
  'useLicense':'LGPL',
  'programFunction':'CCPN XML file with nmr reference shift information',
  'programType':None,
  'stdContact':'ccpn',
  'extraContact':None,
  'references':('CCPN2002','CCPNMR2004','MEMOPS2004'),
  'credits':'\nFile content based on reference information from the BioMagResBank.'
 },
)


PrePecan readme file (6/4/2006)

PrePecan is the chaining routine used by Pecan for aligning 
multiple sequences. Every pair of sequences is aligned using 
Exonerate in a recursive fashion. These alignments are constructed
into a partially ordered set of constraints which can be passed
to Pecan to form the pairwise alignment envelopes. Otherwise, as with 
this standalone program, they can be output as a single consistent 
alignment. Any set of exonerate models can be used with nucleotide 
sequences, providing for flexibility. It's quite fast.

----------------------------------------

Example..

java [class path options] bp.pecan.utils.PrePecan -E '(a, b);' -F HUMAN CHIMP

Would align the two sequences in an output file called 
output.mfa. The ordering of the leaves in the tree corresponds to the 
ordering of the sequences passed to PrePecan. 

----------------------------------------

User editable options...

Please see the Pecan readme which includes all the PrePecan parameters.

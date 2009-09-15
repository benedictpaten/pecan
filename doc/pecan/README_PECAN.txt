Pecan readme file (6/4/2006)

Pecan is a multiple alignment algorithm in the same vein as 
MLagan/TBA/MAvid etc. It reads in a set of co-linear sequences 
in Fasta format and a Newick tree and outputs a file 
(default output.mfa) containing a Multi-Fasta formatted alignmnent. 

----------------------------------------

Example..

java [class path options] bp.pecan.Pecan -E '((((a1, a2), a), ((a, a), (a, a))), (a, a));' -F HUMAN CHIMP BABOON DOG CAT PIG COW MOUSE RAT

Would align the given nine sequences in an output file called 
output.mfa. The ordering of the leaves in the tree corresponds to the 
ordering of the sequences passed to Pecan, hence in the example 
shown the human and chimp sequences correspond to nodes a1 and a2. 
Pecan IGNORES the contents of fasta header files.

----------------------------------------

Dependencies.

Pecan requires Exonerate, and is tested with Exonerate-1.0.0 but may 
work with other, or newer versions. 

You can download exonerate from http://www.ebi.ac.uk/~guy/exonerate/

Pecan is written in 100% Java and will work with a JVM of 1.5
(I think) and above. 

----------------------------------------

Important issues

Pecan assumes that sequences are SOFTMASKED, that is all upper case 
except for repeats which should be lower case. Whilst it will align 
hard-masked/unmasked sequences you may experience problems, especially 
at the initial chaining procedures. Sequences which are all lower case
will be ignored by Exonerate if soft-masking is on.

The ordering of the leaves in the tree corresponds to the ordering 
of the sequences passed to Pecan (see example).

----------------------------------------

Notes on Pecan algorithms.

Pecan alignment method has a close relation to the Probcons/TCoffee 
consistency methodology. Unlike these programs Pecan uses 
alignment anchors, hence it's relation to MLagan/TBA/MAvid and 
it's suitibility for aligning very large sequences. Pecan uses 
a 'sequence progressive' methodology to align sequences in a 
zip like fashion, therefore making it highly memory efficient. 
The anchoring procedure of Pecan has some relation to the TBA
partial order system. Proper details to follow.

----------------------------------------

User editable options...

	Notes : 
		Boolean parameters are flipped by specifying them.

Pecan :
This is an anchored, consistency based multiple alignment 
program written by Benedict Paten. Mail to bjp (AT) ebi.ac.uk
Options:
        -A      Set logging
      	Turn on logging at info level.
      	
        -B      Set the log file (default = %t/bp.log)
        
        -C      Set the logging level
        Default is info level.
        
        -D      Set the console log level
        Default is info level.
        
        -E      Newick tree for sequences, unspecified distances are given the value 1.0
        Example.. '((((a1:1.0, a2:2.0), a), (a, a:2.0, a, a)), (a, a)):3.0;'
        
        -F      Sequence files in fasta format
        The order must correspond to those in the tree.
        
        -G      File in which to write multi-fasta formatted output, default : output.mfa
        
        -H      Word length of Exonerate hits for recursive divide and conquer with more leniant parameters, default :  5 8 11 
        Exonerate is called recursively to lay down anchor chains, with increasiningly sensitive parameters (similar to Lagan).
        
        -I      Basic command upon which exonerate is run, default :  --showcigar true --showvulgar false --showalignment false --querytype dna --targettype dna 
        Pass as quoted string
        
        -J      Path to exonerate, default : exonerate
        
        -K      Consistency transform the chains between sequences, default : true
        TCoffee style, chains between sequences are rescored according to projected outgroup sequences.
        
        -L      Amount of edge to trim from diagonals, default : 3
        Anchor diagonals are trimmed by this amount from each end.
        
        -M      Rescore alignments, default (flip): true
        
        -N      Exonerate min scores for recursive divide and conquer with more leniant parameters, default :  100 150 200 
        Min scores of exonerate chain to be included in chaining (Exonerate default scoring scheme).
        
        -O      Tell Exonerate sequences are softmasked for recursive divide and conquer with more leniant parameters, default :  false true true 
        See exonerate.
        
        -P      Exonerate saturate threshold, default : 8
        See exonerate.
        
        -Q      Use Exonerate gapped extension mode for recursive divide and conquer with more leniant parameters, default :  false false false 
        See exonerate.
        
        -R      Max distances for recursive divide and conquer with more leniant parameters, default :  20000 664000 2147483647 
        Avoids calling exonerate on dp matrices greater than this many x+y diagonals across. When a dp matrix is larger than the max it is split into too
        maximal matrices (one at each end), potentially leaving unaligned sequences imbetween.
        
        -S      Exonerate models for recursive divide and conquer with more leniant parameters, default :  affine:local affine:local affine:local 
        See exonerate (please specify full names)
        
        -T      Min distance for exonerate, default : 200
        Minimum length of each sequence in a dynamic programming matrix to be eligible for an exonerate call.
        
        -U      Relative entropy threshold below which alignments are discarded, default : 0.65
        See Chiaromonte F., Yap V.B., Miller W. 2002. Scoring pairwise genomic sequence alignments. Pacific Symposium on Biocomputing, 115-126
        
        -V      Specify hmm file, default : null
        Supply your own HMM in Pecan format
        
        -W      Transitive anchors, (flip) default : true
        Allow pairwise alignments to be projected between one another to restrict the area of the dynamic programming matrices.
        
        -X      Width in diagonals surrounding transitive anchors, default : 10
        Number of diagonals (+x, -y and vice versa) to place around and transitive anchors.
        
        -Y      Width in diagonals surrounding standard anchors, default : 10
        Number of diagonals (+x, -y and vice versa) to place around and standard anchors.
        
        -Z      Size of diagonal sufficient to generate a potential cut point, default : 4
        Size of diagonal for a cut point. Total length of original exonerate diagonal will be equal to two times edge trim plus this number.
        
        -a      The minumum number of diagonal coordinates (x+y) between a cut point and the computed polygon, default : 15
        Gap polygon left between cut point and computed polygon.
        
        -b      The minumum number of diagonal coordinates (x+y) between a cut point and the next one, default : 500
        
        -c      Consistency transformation, default : true
        Use consistency transformation between weights. See Probcons and TCoffee for further information.
        
        -d      Threshold for weights to be used in calculations, default : 0.01
        
        -e      Use direct byte buffers (flip), default : true
        Java parameter, if a ByteBuffer exception arises you can try flipping this parameter.
        
        -f      Set a minimum capacity for the weight heap (bytes), default : 1000000
        Minimum size of weight heap for weights.
        
        -g      Pre close gaps larger than this length, default : 10000
        Gaps in a sequence of greater than length are preclosed to avoid optimisation over very large gaps.
        
        -h      Size of overhanging border (per sequence) into pre-closed gaps, default : 4500
        Block at each end of pre-closed gap to allow dynamic programming in.
        
        -i      Outgroup reordering diagonals distance (per sequence, internal parameter), default : 500
        Experimental, avoid.
        
        -j      Use HMM with junk state, default : true
        Default Pecan HMM has two sets of indel states, short and long. This is tuned specifically to avoid aligning into large indels.
        
        -k      Output confidence values, default : false
        Switch for confidence values. Similar to the Probcons method, see below.
        
        -l      Include not aligned probabilities in confidence value, default : true
        Include the probabilitiy for each residue present in a column of being aligned to gap in those rows containing a gap in determining confidence.
        
        -m      Include formatted confidence values in MFA file, default : false
        Allows viewing in place of confidence values.
        
        -n      Write out a seperate confidence values file, default : true
        Write out a file containing one floating point value per line which corrsponds to the confidence per column.
        
        -o      File to write out confidence values, default : confidence.txt
        File containing seperate confidence values. 
-----------
Boolean (yes/no) parameters can be flipped from their defaults by specifying them as command line option
Please prefix numerical values starting with a '-' in braces with a '/'
The following options to the java vm may be useful:
        java -Xmx[megabytes]m : max memory for the virtual machine
        ''   -Xms[megabytes]m : min memory to the virtual machine
        ''   -server : start the vm in server rather than client mode,
             maybe faster, may have larger memory requirements 
             and slower start up 
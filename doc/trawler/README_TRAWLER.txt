Trawler readme file (27/4/2006)

Trawler is a deterministic motif finder specialised for looking at large sequence sets. Trawler generally reads in two sequence sets, a conserved set and a background set. Hence motifs that are overrepresented in the conserved sequence set relative to the background set are reported.

Because transcription factors can bind to many motifs and have variable information content and size, Trawler evaluates a pattern alphabet of degenerate IUPAC characters to search for motifs of unlimited length but minimum occurrence number. To make the search feasible Trawler employs a fast deterministic pruning method based upon a memory efficient suffix tree. Over-representation is calculated according to an approximate binomial model where a significance cut off is determined by a robust method of repeated background randomisation.

----------------------------------------

Examples..

java [class path options] bp.trawler.Trawler -E conserved.fa -F background.fa

Would report over-occuring motifs in the conserved sequence set relative to the background set, a threshold z-score being calculated by randomisation.

java [class path options] bp.trawler.Trawler -E conserved.fa -F background.fa -Q -K 15

Would report over-occuring motifs in the conserved sequence set relative to the background set, the threshold z-score being set to 15 with randomisation disabled.

java [class path options] bp.trawler.Trawler -E conserved.fa -F background.fa -Q -K -100000000 -P 

Would report under-occuring motifs in the conserved sequence set relative to the background set, the threshold negative z-score being calculated by randomisation.

----------------------------------------

Dependencies.

Trawler is written in 100% Java and will work with a JVM of 1.5
(I think) and above. 

----------------------------------------

Important issues

Trawler only understands upper case ACTG characters, all other characters are ignored.

The variance approximation is pretty dumb, generally providing a background sequence set which is five times larger than the conserved set will produce acceptable results, although this is not a hard rule. As a rule, if no motifs are reported try adjusting the size of the background set.

By default Trawler estimates a cut-off for the sequence significance, it is also possible to manually specify it. You can also flip things around and look at under-represented signals and estimate an under-representation cut-off by adjusting the parameters.


Trawler could be fifty times faster, so please let me know if this is a desperate problem and I will speed things up.

Trawler eats memory also, and this could be radically fixed by not creating the background tree but again, I won't fix it unless you need it doing.

----------------------------------------

User editable options...

	Notes : 
		Boolean parameters are flipped by specifying them.

Trawler :
This is a suffix tree based motif finder. 
The output is of the following format.. 
[CONSERVED COUNT] [BACKGROUND COUNT] [Z SCORE] [MOTIF]
Trawler only looks at upper case IUPAC characters.
Soft masked characters are ignored as are any other characters.
The score is calculated assuming an approximately normal distribution with
expected values calculated from sequences and background count
Program written by Benedict Paten. Mail to bjp (AT) ebi.ac.uk
Options:
        -A      Set logging
        -B      Set the log file (default = %t/bp.log)
        -C      Set the logging level
        -D      Set the console log level
        -E      The fasta file containing conserved sequences
        -F      The fasta file containing background sequences
        -G      Alphabet to use, give any unspaced array of non-redundant, 
2-redundant and the wild-card IUPAC characters in the following order ACGTMRWSYKN, 
(REMINDER : A C G T [AC]:M [AG]:R [AT]:W [CG]:S [CT]:Y [GT]:K [N]:ACGT) default : ACGTN
        -H      Maximum size of motif, default : 20
        -I      Maximum number of mismatches (2 for an N, 1 for a 2-redundant character), default : 2
        -J      Minimum occurrences of motif in conserved sequence, default : 20
        -K      Minimum z-score, default : 10.0
        -L      Maximum number of motifs to report in run (to prevent filling a disk), default : 100000
        -M      Byte multiple, default : 14
        -N      Output file, default : output.motifs
        -O      Maximum z-score, default : Infinity
        -P      Estimate the minimum z-score, (flip) default : true
        -Q      Estimate the maximum z-score, (flip) default : false
        -R      Number of z-score estimation iterations default : 1
        -S      Run the actual scan function to find motifs, else no motifs reported, (flip) default : true
        -T      Number of tail values to estimate cut off using, default : 5
        -U      Use a shuffled background set for estimating the z-score, (flip) default : true
        -V      Size of fragments with which to shuffle background, default : 20
-----------
Boolean (yes/no) parameters can be flipped from their defaults by specifying them as command line option
Please prefix numerical values starting with a '-' in braces with a '/'
The following options to the java vm may be useful:
        java -Xmx[megabytes]m : max memory for the virtual machine
        ''   -Xms[megabytes]m : min memory to the virtual machine
        ''   -server : start the vm in server rather than client mode,
             maybe faster, may have larger memory requirements 
             and slower start up 
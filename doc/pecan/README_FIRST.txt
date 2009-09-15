General readme file (6/4/2006)

Running any of these programs requires having a Java virtual 
machine installed, version 1.4 or greater.

The easiest way to tell the run Pecan is to specify the classpath
on the command line. Hence..

java -cp {path to lib directory}/pecan.jar bp.pecan.Pecan

Where the -cp tells the vm to place the pecan.jar on the class path.
Alternatively the jar can be mounted on the classpath by adding it 
to the classpath environment variable.

To see what other programs are in the package try using..

jar -xf pecan.jar

Which will have the effect of converting the jar to a directory structure
which can be explored.


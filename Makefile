
all: pecan.jar
	
pecan.jar : bp/pecan/*.java bp/pecan/utils/*.java bp/common/*.java
	jar -cvf pecan.jar bp

clean:
	find . -type f -name \*.class | xargs rm -f
	rm -f pecan.jar 

.PHONY: clean

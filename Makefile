include ../../include.mk
binPath = ../../bin
libPath = ../../lib

all: bp/trawler/Trawler.class bp/pecan/Pecan.class ${libPath}/pecan.jar

bp/trawler/Trawler.class: bp/trawler/*.java
	${jcc} -cp ./ bp/trawler/Trawler.java

bp/pecan/Pecan.class: bp/pecan/*.java
	${jcc} -cp ./ bp/pecan/Pecan.java
	
${libPath}/pecan.jar : bp/pecan/*.java bp/pecan/utils/*.java bp/common/*.java
	jar -cvf pecan.jar bp
	mv pecan.jar ${libPath}/pecan.jar

clean:
	find . -type f -name \*.class | xargs rm -f
	rm -f ${libPath}/pecan.jar 

.PHONY: clean

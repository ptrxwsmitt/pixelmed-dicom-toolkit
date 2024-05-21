PATHTOROOT = ../../..

OBJS = \
Logger.class \
LoggerFactory.class

all:	${OBJS}

include ${PATHTOROOT}/Makefile.common.mk

# override javac command in Makefile.common.mk to add slf4j to classpath
.java.class:
	export JAVAVERSIONTARGETJARFILE=`/usr/libexec/java_home -v ${JAVAVERSIONTARGET} | tail -1`/jre/lib/rt.jar; javac ${JAVACOPTIONS} -Xlint:unchecked \
		-classpath ${PATHTOROOT}:${SLF4JCOMPILEADDITIONALJAR} \
		-sourcepath ${PATHTOROOT}:${SLF4JCOMPILEADDITIONALJAR} $<

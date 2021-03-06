#! /bin/sh

if [ -z "$JAVA_HOME" ]; then 
	echo "Error: JAVA_HOME var not found."
	exit -1
fi

#Check if not exists ant_home. If not, discovers (try)
if [ -z "$ANT_HOME" ]; then 
        #if here?
	if [ -d "/usr/share/ant" ]; then
		ANT_HOME="/usr/share/ant"
	elif [ -d "/usr/share/apache-ant" ]; then
		ANT_HOME="/usr/share/apache-ant"
	else
		echo "Error: ANT_HOME var not found."
		exit -1
	fi
fi


ANT_LIB="$ANT_HOME/lib"

for i in "${ANT_LIB}"/ant*.jar
do
  ANTLIBPATH="$ANTLIBPATH:$i"
done

LOCALCLASSPATH="$ANTLIBPATH:lib/js-14.jar:jar/anteater.jar"

TOOLS_LIB="$JAVA_HOME/lib/tools.jar"
if [ -f "$TOOLS_LIB" ]; then LOCALCLASSPATH=$TOOLS_LIB:$LOCALCLASSPATH;fi

$JAVA_HOME/bin/java -Dant.home=\"$ANT_HOME\" -Dant.library.dir=\"$ANT_LIB\" -cp $LOCALCLASSPATH br.com.anteater.main.Main $1 $2 $3 $4 $5
exit $?

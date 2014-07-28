#!/bin/bash

TEST_DIR='/home/ilgo/Music/Amelia Cuni'
# clean out if old stuff is found
if [[ -e $TEST_DIR ]] 
then
	rm -rf $TEST_DIR
fi

#clean the log files
rm ~/Code/workspace/zen.ilgo.music/resources/*log*
ls *~ > /dev/null
if [[ $? = 0 ]]
then
	rm *~
fi

java -jar magnatune.jar http://zen.magnatune.com/amelia-danca
#java -jar magnatune.jar http://magnatune.com/artists/albums/yoria-handshake/

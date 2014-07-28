#!/bin/bash
#
# a simple script to prepare all the sources
# so we can build an rpm from them
#
# ilgo711@gmail.com 05/25/2009
#-------------------------------
set -x

VERSION=1.12

MUSIC_ROOT=/home/ilgo/Code/workspace/zen.ilgo.music
WGET_ROOT=/home/ilgo/Code/workspace/zen.ilgo.tools
TAR_ARCHIVE=/var/www/test/MagnaDownloader-$VERSION.tar.gz
RPM_DIR=/home/makerpm/rpmbuild
TMP_DIR=/tmp/MagnaDownloader-$VERSION

mkdir $TMP_DIR
cp -r $MUSIC_ROOT/src/* $TMP_DIR
cp -r $WGET_ROOT/src/* $TMP_DIR 2> /dev/null
# clean out the svnstuff
cd $TMP_DIR
find . -name .svn -exec rm -rf {} \;
cd $OLDPWD

cd /tmp
tar czf $TAR_ARCHIVE MagnaDownloader-$VERSION
rm -rf $TMP_DIR

cp $TAR_ARCHIVE $RPM_DIR/SOURCES
cp $MUSIC_ROOT/MagnaDownloader.spec $RPM_DIR/SPECS/


#!/bin/bash

##
# Configure these variables to match your environment
##

if [ -z "$WEBLOUNGE_HOME" ]; then
  PWD=`pwd`
  if [ -f "$PWD/bin/felix.jar" ]; then
	WEBLOUNGE_HOME="$PWD"
  else
	echo "WEBLOUNGE_HOME is not set"
	exit 1
  fi
fi

# Main Weblounge path configuration. Make sure that the user executing Weblounge
# has write access to WEBLOUNGE_WORK_DIR.

# This setting is fine if you want everything to be in the same directory.
# Change work directory to something else if you care about writable versus
# non-writable directory
WEBLOUNGE_WORK_DIR="$WEBLOUNGE_HOME"

# Memory settings
MEMORY_OPTS="-Xmx2048m -XX:MaxPermSize=256m"

# Debug options
DEBUG_PORT="8000"
DEBUG_SUSPEND="n"

# Detail configuration for weblounge directories. Usually, it is fine to simply
# adjust the two paths above, namely $WEBLOUNGE_HOME and $WEBLOUNGE_WORK_DIR.

WEBLOUNGE_CONFIG_DIR="$WEBLOUNGE_HOME/etc"
WEBLOUNGE_LOG_DIR="$WEBLOUNGE_WORK_DIR/logs"
WEBLOUNGE_TEMP_DIR="$WEBLOUNGE_WORK_DIR/work"
WEBLOUNGE_CACHE_DIR="$WEBLOUNGE_TEMP_DIR/felix"
WEBLOUNGE_SITES_DIR="$WEBLOUNGE_WORK_DIR/sites"
WEBLOUNGE_SITESDATA_DIR="$WEBLOUNGE_WORK_DIR/sites-data"
WEBLOUNGE_LIB_DIR="$WEBLOUNGE_HOME/lib/weblounge"
WEBLOUNGE_LIBEXEC_DIR="$WEBLOUNGE_HOME/lib/libexec"
WEBLOUNGE_EXT_DIR="$WEBLOUNGE_HOME/lib/ext"

##
# Only change the line below if you want to customize the server
##

WEBLOUNGE_HOME_OPTS="-Dweblounge.home=$WEBLOUNGE_HOME"
WEBLOUNGE_SITES_OPTS="-Dweblounge.sitesdir=$WEBLOUNGE_SITES_DIR"
WEBLOUNGE_SITES_DATA_OPTS="-Dweblounge.sitesdatadir=$WEBLOUNGE_SITESDATA_DIR"
WEBLOUNGE_FILEINSTALL_OPTS="-Dfelix.fileinstall.dir=$WEBLOUNGE_CONFIG_DIR/load"
WEBLOUNGE_LIB_OPTS="-Dweblounge.libdir=$WEBLOUNGE_LIB_DIR"
WEBLOUNGE_EXT_OPTS="-Dweblounge.extdir=$WEBLOUNGE_EXT_DIR"
FELIX_CONFIG_OPTS="-Dfelix.config.properties=file:${WEBLOUNGE_CONFIG_DIR}/config.properties -Dfelix.system.properties=file:${WEBLOUNGE_CONFIG_DIR}/system.properties"
PAX_CONFMAN_OPTS="-Dbundles.configuration.location=$WEBLOUNGE_CONFIG_DIR"
PAX_LOGGING_OPTS="-Dorg.ops4j.pax.logging.DefaultServiceLog.level=WARN"
WEBLOUNGE_LOGGING_OPTS="-Dweblounge.logdir=$WEBLOUNGE_LOG_DIR"
GRAPHICS_OPTS="-Djava.awt.headless=true"
TEMP_DIR_OPTS="-Djava.io.tmpdir=$WEBLOUNGE_TEMP_DIR"

# Create the directories
mkdir -p "$WEBLOUNGE_LOG_DIR"
mkdir -p "$WEBLOUNGE_CACHE_DIR"
mkdir -p "$WEBLOUNGE_TEMP_DIR"
mkdir -p "$WEBLOUNGE_SITES_DIR"
mkdir -p "$WEBLOUNGE_SITESDATA_DIR"

# Add the libexec directory to the PATH directive
export PATH=$PATH:$WEBLOUNGE_LIBEXEC_DIR

# Reload weblounge bundles
if [ -d "$WEBLOUNGE_CACHE_DIR" ]; then
  cd "$WEBLOUNGE_CACHE_DIR"
  for bundle in `find . -type f -name "bundle.location" | xargs grep --files-with-match -e "file:" | sed -e 's/.\/\(.*\)\/bundle.location/\1/'`; do
    rm -r "$WEBLOUNGE_CACHE_DIR/$bundle"
  done
fi

# Create the debug config
DEBUG_OPTS="-Xdebug -Xnoagent -Xrunjdwp:transport=dt_socket,address=$DEBUG_PORT,server=y,suspend=$DEBUG_SUSPEND"

#Create the java runtime options
RUNTIME_OPTS="$WEBLOUNGE_HOME_OPTS $WEBLOUNGE_SITES_OPTS $WEBLOUNGE_SITES_DATA_OPTS $WEBLOUNGE_LOGGING_OPTS $WEBLOUNGE_LIB_OPTS $WEBLOUNGE_EXT_OPTS $TEMP_DIR_OPTS $GRAPHICS_OPTS $WEBLOUNGE_FILEINSTALL_OPTS $FELIX_CONFIG_OPTS $PAX_CONFMAN_OPTS $PAX_LOGGING_OPTS"

# Finally start Weblounge
cd "$WEBLOUNGE_HOME"
java $MEMORY_OPTS $DEBUG_OPTS $RUNTIME_OPTS -jar "$WEBLOUNGE_HOME/bin/felix.jar" "$WEBLOUNGE_CACHE_DIR"
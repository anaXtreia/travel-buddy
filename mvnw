#!/bin/sh
# ----------------------------------------------------------------------------
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
#    https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.
# ----------------------------------------------------------------------------

# ----------------------------------------------------------------------------
# Maven Start Up Batch script
#
# Required ENV vars:
# ------------------
#   JAVA_HOME - location of a JDK home dir
#
# Optional ENV vars
# -----------------
#   MAVEN_OPTS - parameters passed to the Java VM when running Maven
#     e.g. to debug Maven itself, use
#       set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000
#   MAVEN_SKIP_RC - flag to disable loading of mavenrc files
# ----------------------------------------------------------------------------

if [ -z "$MAVEN_SKIP_RC" ] ; then
  if [ -f /usr/local/etc/mavenrc ] ; then
    . /usr/local/etc/mavenrc
  fi
  if [ -f /etc/mavenrc ] ; then
    . /etc/mavenrc
  fi
  if [ -f "$HOME/.mavenrc" ] ; then
    . "$HOME/.mavenrc"
  fi
fi

# OS specific support. $var _must_ be set to either true or false.
cygwin=false;
darwin=false;
mingw=false
case "$(uname)" in
  CYGWIN*) cygwin=true ;;
  MINGW*) mingw=true;;
  Darwin*) darwin=true
    if [ -z "$JAVA_VERSION" ] ; then
      JAVA_VERSION="CurrentJDK"
    else
      echo "Using Java version: $JAVA_VERSION"
    fi
    if [ -z "$JAVA_HOME" ]; then
      if [ -x "/usr/libexec/java_home" ]; then
        JAVA_HOME="$(/usr/libexec/java_home)"
      else
        JAVA_HOME="/Library/Java/Home"
      fi
    fi
    ;;
esac

if [ -z "$JAVA_HOME" ] ; then
  if [ -r /etc/gentoo-release ] ; then
    JAVA_HOME=$(java-config --jre-home)
  fi
fi

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=$(cygpath --unix "$JAVA_HOME")
  [ -n "$CLASSPATH" ] &&
    CLASSPATH=$(cygpath --path --unix "$CLASSPATH")
fi

# For Mingw, ensure paths are in UNIX format before anything is touched
if $mingw ; then
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME="$(cd "$JAVA_HOME" || (echo "cannot cd into $JAVA_HOME."; exit 1); pwd)"
fi

if [ -z "$JAVA_HOME" ]; then
  javaExecutable="$(which javaw)"
  if [ -n "$javaExecutable" ] && ! [ "$(expr "$javaExecutable" : '\([^ ]*\)')" = "no" ]; then
    # readlink(1) is not available as standard on Solaris 10.
    readLink=$(which readlink)
    if [ ! $(expr "$readLink" : '\([^ ]*\)')" = "no" ]; then
      if $darwin ; then
        javaHome="$(dirname "$javaExecutable")"
        javaExecutable="$(cd "$javaHome" && pwd -P)/javaw"
      else
        javaExecutable="$(readlink -f "$javaExecutable")"
      fi
      javaHome="$(dirname "$javaExecutable")"
      javaHome=$(expr "$javaHome" : '\(.*\)/bin'\ )
      JAVA_HOME="$javaHome"
      export JAVA_HOME
    fi
  fi
fi

if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD="$(which java)"
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly." >&2
  echo "  We cannot execute $JAVACMD" >&2
  exit 1
fi

if [ -z "$JAVA_HOME" ] ; then
  echo "Warning: JAVA_HOME environment variable is not set."
fi

CLASSWORLDS_LAUNCHER=org.codehaus.plexus.classworlds.launcher.Launcher

# traverses directory structure from process work directory to filesystem root,
# preventing infinite loop by checking if we're at the root by comparing
# temporary folder where script resides with its parent.
find_maven_basedir() {
  if [ -z "$1" ]
  then
    echo "Path not specified to find_maven_basedir"
    return 1
  fi

  basedir="$1"
  wdir="$1"
  while [ "$wdir" != '/' ] ; do
    if [ -d "$wdir"/.mvn ] ; then
      basedir=$wdir
      break
    fi
    # workaround for JBEAP-8937 (on Solaris 10/Sparc)
    if [ -d "${wdir}" ]; then
      wdir=$(cd "$wdir/.." || exit 1; pwd)
    fi
    # end of workaround
  done
  printf '%s' "$(cd "$basedir" || exit 1; pwd)"
}

# concatenates all lines of a file
concat_lines() {
  if [ -f "$1" ]; then
    # Remove \r in case we run on Windows within Git Bash
    # and check out the repository with auto CRLF transform
    tr -s '\r\n' ' ' < "$1"
  fi
}

BASE_DIR=$(find_maven_basedir "$(dirname "$0")")
if [ -z "$BASE_DIR" ]; then
  exit 1;
fi

MAVEN_PROJECTBASEDIR=${MAVEN_BASEDIR:-"$BASE_DIR"}
export MAVEN_PROJECTBASEDIR
if [ "$MVNW_VERBOSE" = true ]; then
  echo $MAVEN_PROJECTBASEDIR
fi

##########################################################################################
# Extension to allow automatically downloading the maven-wrapper.jar from Maven-central
# This allows using the maven wrapper in projects that prohibit checking in binary data.
##########################################################################################
wrapperJarPath="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
if [ -r "$wrapperJarPath" ]; then
    if [ "$MVNW_VERBOSE" = true ]; then
      echo "Found $wrapperJarPath"
    fi
else
    if [ "$MVNW_VERBOSE" = true ]; then
        echo "Couldn't find $wrapperJarPath, downloading it ..."
    fi
    if [ -n "$MVNW_REPOURL" ]; then
      wrapperUrl="$MVNW_REPOURL/org/apache/maven/wrapper/maven-wrapper/3.3.1/maven-wrapper-3.3.1.jar"
    else
      wrapperUrl="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.3.1/maven-wrapper-3.3.1.jar"
    fi
    while IFS="=" read key value; do
      case "$key" in (wrapperUrl) wrapperUrl="$value"; break ;;
      esac
    done < "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.properties"
    if [ "$MVNW_VERBOSE" = true ]; then
      echo "Downloading from: $wrapperUrl"
    fi
    wrapperJarPath="$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar"
    if $cygwin; then
      wrapperJarPath=$(cygpath --path --windows "$wrapperJarPath")
    fi
    if command -v wget > /dev/null; then
        QUIET="--quiet"
        if [ "$MVNW_VERBOSE" = true ]; then
          QUIET=""
        fi
        wget $QUIET -O "$wrapperJarPath" "$wrapperUrl" || rm -f "$wrapperJarPath"
    elif command -v curl > /dev/null; then
        QUIET="--silent"
        if [ "$MVNW_VERBOSE" = true ]; then
          QUIET=""
        fi
        curl $QUIET -o "$wrapperJarPath" "$wrapperUrl" -f || rm -f "$wrapperJarPath"
    else
        echo "No wget or curl found, please download $wrapperUrl and place it under $wrapperJarPath" >&2
    fi
fi
##########################################################################################
# End of extension
##########################################################################################

MAVEN_OPTS="$(concat_lines "$MAVEN_PROJECTBASEDIR/.mvn/jvm.config") $MAVEN_OPTS"

# Provide a "standardized" way to retrieve the CLI args that will
# activate and allow access to the profiles and properties specified in
# .mvn/maven.config
# Using "${@:-}" to allow the "--" argument
MAVEN_CMD_LINE_ARGS="${MAVEN_CMD_LINE_ARGS:-}"
export MAVEN_CMD_LINE_ARGS

WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

exec "$JAVACMD" \
  $MAVEN_OPTS \
  $MAVEN_DEBUG_OPTS \
  -classpath "$MAVEN_PROJECTBASEDIR/.mvn/wrapper/maven-wrapper.jar" \
  "-Dmaven.multiModuleProjectDirectory=${MAVEN_PROJECTBASEDIR}" \
  ${WRAPPER_LAUNCHER} "$@"

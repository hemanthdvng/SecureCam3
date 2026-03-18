#!/bin/sh
# Gradle wrapper
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
CLASSPATH="$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar"
if [ -f "$CLASSPATH" ]; then
  exec java -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
else
  exec gradle "$@"
fi
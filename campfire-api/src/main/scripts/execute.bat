@echo off
setlocal
echo.

set campfire.version=1.0-SNAPSHOT
set campfire.libpath=%CD%\lib

if exist %campfire.libpath% (
   rd %campfire.libpath% /s/q
)

for /f %%i in ('call mvn help:evaluate -Dexpression^=settings.localRepository ^| findstr /V "[INFO]"') do set maven.repo=%%i
echo Found local repository: %maven.repo%

call mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:get -DgroupId=com.emprovise.api -DartifactId=campfire-api -Dversion=%campfire.version% -Dtype=jar -DrepoUrl=http://ldxapp52.dx.deere.com:8080/nexus/content/groups/public
call mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:copy-dependencies -DoutputDirectory=%campfire.libpath% -f %maven.repo%/com/emprovise/api/campfire-api/%campfire.version%/campfire-api-%campfire.version%.pom
call mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:copy -Dartifact=com.emprovise.api:campfire-api:%campfire.version%:jar -DoutputDirectory=%campfire.libpath% -f %maven.repo%/com/emprovise/api/campfire-api/%campfire.version%/campfire-api-%campfire.version%.pom

if NOT %ERRORLEVEL%==0 (
   echo.
   echo !! error getting campfire-speaker:%campfire.version% ... error level = %ERRORLEVEL%
   exit /B %ERRORLEVEL%
)

java -classpath .;%campfire.libpath%/* com.emprovise.utility.CampfireUtility --domain jdisg --user PatilPranav@JohnDeere.com --password jdlink001 --room Test --proxydomain proxyv.dpn.deere.com --proxyport 80 --proxyuser a903825 --proxypassword rojpp11z --post "message2"

endlocal
echo on

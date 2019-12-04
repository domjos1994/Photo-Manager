SET pathToLaunch4j="C:\Program Files (x86)\Launch4j"
SET pathToInno="C:\Program Files (x86)\Inno Setup 6"
SET JVM8="C:\Program Files (x86)\Entwicklung\Java\JRE-8-64"

%JVM8%\bin\java.exe -jar %pathToLaunch4j%\launch4j.jar launch4j.xml
%pathToInno%\ISCC.exe setup.iss
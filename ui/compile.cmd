call mvn install -DskipTests
xcopy target\hop-*.jar %HOP_CLIENT%\lib\core /y

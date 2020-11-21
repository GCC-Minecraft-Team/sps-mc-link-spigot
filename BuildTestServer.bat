IF NOT EXIST TestServer\tuinity.jar (
    mkdir BuildTools
    cd BuildTools
    curl -o tuinity-paperclip.jar https://ci.codemc.io/job/Spottedleaf/job/Tuinity/lastSuccessfulBuild/artifact/tuinity-paperclip.jar

    cd ..
    mkdir TestServer
    copy BuildTools\tuinity-paperclip.jar TestServer\tuinity.jar

    cd TestServer
    echo eula=true>eula.txt
    cd ..
)

cd TestServer
mkdir plugins
cd ..
copy target\sps-mc-link-spigot-1.0-SNAPSHOT-jar-with-dependencies.jar TestServer\plugins\sps-mc-link-spigot-latest.jar

cd TestServer
java -Xms8g -Xmx24g -XX:ActiveProcessorCount=8 -XX:+UseG1GC -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9585 -jar yatopia-1.16.4-paperclip-b3.jar nogui
pause
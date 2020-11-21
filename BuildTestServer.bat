IF NOT EXIST TestServer\yatopia.jar (
    mkdir BuildTools
    cd BuildTools
    curl -L -o yatopia-latest.jar https://api.yatopia.net/v2/build/8/download?branch=ver/1.16.4

    cd ..
    mkdir TestServer
    copy BuildTools\yatopia-latest.jar TestServer\yatopia.jar

    cd TestServer
    echo eula=true>eula.txt
    cd ..
)

cd TestServer
mkdir plugins
cd ..
copy target\sps-mc-link-spigot-1.0-SNAPSHOT-jar-with-dependencies.jar TestServer\plugins\sps-mc-link-spigot-latest.jar

cd TestServer
java -Xms8g -Xmx24g -XX:ActiveProcessorCount=8 -XX:+UseG1GC -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9585 -jar yatopia.jar nogui
pause
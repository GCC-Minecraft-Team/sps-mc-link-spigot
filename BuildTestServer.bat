IF NOT EXIST TestServer\spigot.jar (
    mkdir BuildTools
    cd BuildTools
    curl -z BuildTools.jar -o BuildTools.jar https://hub.spigotmc.org/jenkins/job/BuildTools/lastSuccessfulBuild/artifact/target/BuildTools.jar
    java -jar BuildTools.jar

    cd ..
    mkdir TestServer
    copy BuildTools\spigot-1.16.4.jar TestServer\spigot.jar

    cd TestServer
    echo eula=true>eula.txt
    cd ..
)

cd TestServer
mkdir plugins
cd ..
copy target\sps-mc-link-spigot-1.0-SNAPSHOT-jar-with-dependencies.jar TestServer\plugins\sps-mc-link-spigot-latest.jar

cd TestServer
java -Xms8g -Xmx8g -XX:ActiveProcessorCount=8 -XX:+UseG1GC -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9585 -jar spigot.jar nogui
pause
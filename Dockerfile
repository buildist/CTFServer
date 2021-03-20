FROM gradle:jdk-alpine AS builder

ENV PROJ_DIR /ctf
WORKDIR $PROJ_DIR

COPY lib $PROJ_DIR/lib
COPY src $PROJ_DIR/src
COPY build.gradle $PROJ_DIR

RUN gradle build

FROM openjdk:17-jdk-alpine

ENV PROJ_DIR /ctf
WORKDIR $PROJ_DIR

RUN adduser -DHg '' -u 1000 ctf && chown -R ctf:ctf $PROJ_DIR

COPY --chown=ctf:ctf lib $PROJ_DIR/lib
COPY --chown=ctf:ctf --from=builder $PROJ_DIR/build/libs/ctf.jar $PROJ_DIR/CTFServer.jar

USER ctf

VOLUME ["opencraft.properties", "maps", "savedGames", "mapratings", "texturepacks", "texturepack_patch", "blocks.xml", "ipbans.txt", "messages.txt", "moves.txt", "packetHandlers.xml", "packets.xml", "quotes.txt", "rules.txt"]

CMD ["java", "-cp", "lib/*:CTFServer.jar", "org.opencraft.server.Server"]

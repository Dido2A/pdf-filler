FROM amazoncorretto:17-alpine-jdk
RUN addgroup -S spring && adduser -S spring -G spring && id -u spring && id -g spring
RUN mkdir -p /home/spring/upload-dir
COPY upload-dir/* /home/spring/upload-dir/
RUN chown -R 100:101 /home/spring/upload-dir
COPY *.ttf ./
USER spring:spring
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} pdf-filler.jar
ENTRYPOINT ["java","-jar","/pdf-filler.jar"]
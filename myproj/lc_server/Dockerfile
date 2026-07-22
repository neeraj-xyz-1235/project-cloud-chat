#pull amazon's jdk 21
FROM amazoncorretto:21 

#set working directory
WORKDIR /app    

#copy server and lib to dockerfile
COPY server/ /app/server/
COPY lib/ /app/lib/

#chnge workdir to server folder
WORKDIR /app/server

#remove all class files
RUN rm -f *.class

#complie java files 
RUN curl -L -o ../lib/bytes-1.5.0.jar "https://repo1.maven.org/maven2/at/favre/lib/bytes/1.5.0/bytes-1.5.0.jar" && javac -cp ".:../lib/*" *.java

EXPOSE 10100

#run the server
CMD ["java", "-cp", ".:../lib/*", "Server"]

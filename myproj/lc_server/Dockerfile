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
RUN javac -cp ".:../lib/*" *.java

EXPOSE 10100

#run the server
CMD ["java", "-cp", ".:../lib/*", "Server"]

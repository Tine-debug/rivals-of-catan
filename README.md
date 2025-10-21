to compile, run all of your testfiles and get a report run

mvn  verify

TO PLAY THE GAME:
first compile with:

mvn compile

and then execute:

mvn exec:java -Dexec.mainClass="Server"

and then this one in another window of the terminal (also in this folder)

mvn exec:java -Dexec.mainClass="Server" -Dexec.args="online"




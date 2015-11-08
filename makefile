SRCDIR=src
SOURCES=$(wildcard $(SRCDIR)/*.java)
SOURCES += $(wildcard $(SRCDIR)/*/*.java)
SOURCES += $(wildcard $(SRCDIR)/*/*/*.java)
OUTDIR=bin

build:
	mkdir -p $(OUTDIR)
	javac $(SOURCES) -d $(OUTDIR)

run-server:
	java -classpath $(OUTDIR) ipopprojekt.server.Server

run-client:
	java -classpath $(OUTDIR) ipopprojekt.client.ClientGUI

SOURCES = $(wildcard src/shapechanger/*.java src/shapechanger/*/*.java)
#JFXP = $() # insert the location of jftrt.jar if it's not in JAVA_PATH

all: compile doc

compile: $(SOURCES)
	@@rm -rf bin >> /dev/null
	@@ mkdir bin
	@@javac -d bin -cp .:$(JFXP) $(SOURCES)

doc: $(SOURCES)
	@@javadoc @doc_args $(SOURCES)

run: compile
	@@java -cp bin shapechanger.ShapeChanger  &

clean:
	@@echo "Deleting all generated files"
	@@rm -rf bin/*
	@@rm -rf docs/*

diag:
	@@echo $(SOURCES)
